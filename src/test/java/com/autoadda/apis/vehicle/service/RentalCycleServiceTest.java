package com.autoadda.apis.vehicle.service;

import com.autoadda.apis.vehicle.dao.RentalCycleRepository;
import com.autoadda.apis.vehicle.entity.RentalCycle;
import com.autoadda.apis.vehicle.enums.RentalCycleStatus;
import com.autoadda.apis.vehicle.enums.RentalRateType;
import com.autoadda.apis.vehicle.enums.RentalType;
import com.autoadda.apis.vehicle.mapper.RentalCycleMapper;
import com.autoadda.apis.vehicle.request.RentalCycleReturnRequest;
import com.autoadda.apis.vehicle.request.RentalCycleStartRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.response.RentalCycleResponse;
import com.autoadda.apis.vehicle.service.impl.RentalCycleServiceImpl;
import com.autoadda.apis.vehicle.service.impl.RentalNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RentalCycleServiceTest {

    @Mock
    private RentalCycleRepository cycleRepository;

    @Mock
    private RentalCycleMapper mapper;

    @Mock
    private RentalNotificationService notificationService;

    private RentalCycleServiceImpl service;

    private static final Integer OWNER_ID = 1;
    private static final Integer RENTER_ID = 2;
    private static final Long CYCLE_ID = 1L;

    @BeforeEach
    void setUp() {
        service = new RentalCycleServiceImpl(cycleRepository, mapper, notificationService);

        lenient().when(mapper.toResponse(any(RentalCycle.class))).thenReturn(RentalCycleResponse.builder().build());
        lenient().when(cycleRepository.save(any(RentalCycle.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void returnCycle_appliesDiscountAndBothPenalties() {
        // The service stamps actualReturnAt with its own LocalDateTime.now() call, so these
        // timestamps are chosen with generous margin (30+ minutes) from every rounding boundary
        // (24h/48h for duration, whole-hour for lateness) to stay stable despite test execution jitter.
        LocalDateTime startedAt = LocalDateTime.now().minusHours(47).minusMinutes(30);
        LocalDateTime expectedReturnAt = startedAt.plusHours(45); // ~2.5h before the actual ~47.5h return

        RentalCycle cycle = RentalCycle.builder()
                .id(CYCLE_ID)
                .renterId(RENTER_ID)
                .ownerId(OWNER_ID)
                .rateType(RentalRateType.DAILY)
                .rateAtStart(BigDecimal.valueOf(150))
                .kmLimitPerDayAtStart(200)
                .excessKmPenaltyRateAtStart(BigDecimal.valueOf(1.5))
                .lateReturnPenaltyRateAtStart(BigDecimal.valueOf(20))
                .discountAmount(BigDecimal.valueOf(20))
                .startOdometer(10000)
                .startedAt(startedAt)
                .expectedReturnAt(expectedReturnAt)
                .status(RentalCycleStatus.ACTIVE)
                .build();
        when(cycleRepository.findByIdAndIsDeletedFalse(CYCLE_ID)).thenReturn(Optional.of(cycle));

        RentalCycleReturnRequest request = new RentalCycleReturnRequest();
        request.setEndOdometer(10450);

        // Freeze actualReturnAt via the entity itself isn't possible since the service uses
        // LocalDateTime.now() internally; instead verify the saved cycle's computed fields are
        // consistent with the ~48h elapsed window set up above (tolerant of test execution time).
        GenericResponse response = service.returnCycle(CYCLE_ID, request, RENTER_ID);

        assertEquals("success", response.getStatus());
        assertEquals("200", response.getCode());

        ArgumentCaptor<RentalCycle> captor = ArgumentCaptor.forClass(RentalCycle.class);
        verify(cycleRepository).save(captor.capture());
        RentalCycle saved = captor.getValue();

        assertEquals(RentalCycleStatus.COMPLETED, saved.getStatus());
        assertEquals(0, BigDecimal.valueOf(300).compareTo(saved.getBasePrice()));
        assertEquals(0, BigDecimal.valueOf(75).compareTo(saved.getExcessKmPenalty()));
        assertEquals(0, BigDecimal.valueOf(60).compareTo(saved.getLateReturnPenalty()));
        assertEquals(0, BigDecimal.valueOf(415).compareTo(saved.getFinalPrice()));
    }

    @Test
    void returnCycle_notRenter_shouldReturn403() {
        RentalCycle cycle = RentalCycle.builder()
                .id(CYCLE_ID).renterId(RENTER_ID).ownerId(OWNER_ID)
                .status(RentalCycleStatus.ACTIVE).build();
        when(cycleRepository.findByIdAndIsDeletedFalse(CYCLE_ID)).thenReturn(Optional.of(cycle));

        RentalCycleReturnRequest request = new RentalCycleReturnRequest();
        request.setEndOdometer(10450);

        GenericResponse response = service.returnCycle(CYCLE_ID, request, OWNER_ID);

        assertEquals("error", response.getStatus());
        assertEquals("403", response.getCode());
    }

    @Test
    void startCycle_success_shouldSetActiveAndNotify() {
        RentalCycle cycle = RentalCycle.builder()
                .id(CYCLE_ID).renterId(RENTER_ID).ownerId(OWNER_ID)
                .status(RentalCycleStatus.AWAITING_START).build();
        when(cycleRepository.findByIdAndIsDeletedFalse(CYCLE_ID)).thenReturn(Optional.of(cycle));

        RentalCycleStartRequest request = new RentalCycleStartRequest();
        request.setStartOdometer(10000);

        GenericResponse response = service.startCycle(CYCLE_ID, request, RENTER_ID);

        assertEquals("success", response.getStatus());

        ArgumentCaptor<RentalCycle> captor = ArgumentCaptor.forClass(RentalCycle.class);
        verify(cycleRepository).save(captor.capture());
        assertEquals(RentalCycleStatus.ACTIVE, captor.getValue().getStatus());
        assertEquals(10000, captor.getValue().getStartOdometer());
        verify(notificationService).sendCycleStartedNotification(any());
    }

    @Test
    void startCycle_limitedType_shouldComputeExpectedReturnAtFromDaily() {
        RentalCycle cycle = RentalCycle.builder()
                .id(CYCLE_ID).renterId(RENTER_ID).ownerId(OWNER_ID)
                .rateType(RentalRateType.DAILY)
                .rentalType(RentalType.LIMITED)
                .durationValue(3)
                .status(RentalCycleStatus.AWAITING_START).build();
        when(cycleRepository.findByIdAndIsDeletedFalse(CYCLE_ID)).thenReturn(Optional.of(cycle));

        RentalCycleStartRequest request = new RentalCycleStartRequest();
        request.setStartOdometer(10000);

        GenericResponse response = service.startCycle(CYCLE_ID, request, RENTER_ID);

        assertEquals("success", response.getStatus());

        ArgumentCaptor<RentalCycle> captor = ArgumentCaptor.forClass(RentalCycle.class);
        verify(cycleRepository).save(captor.capture());
        RentalCycle saved = captor.getValue();
        assertEquals(saved.getStartedAt().plusDays(3), saved.getExpectedReturnAt());
    }

    @Test
    void startCycle_limitedType_shouldComputeExpectedReturnAtFromHourly() {
        RentalCycle cycle = RentalCycle.builder()
                .id(CYCLE_ID).renterId(RENTER_ID).ownerId(OWNER_ID)
                .rateType(RentalRateType.HOURLY)
                .rentalType(RentalType.LIMITED)
                .durationValue(6)
                .status(RentalCycleStatus.AWAITING_START).build();
        when(cycleRepository.findByIdAndIsDeletedFalse(CYCLE_ID)).thenReturn(Optional.of(cycle));

        RentalCycleStartRequest request = new RentalCycleStartRequest();
        request.setStartOdometer(10000);

        GenericResponse response = service.startCycle(CYCLE_ID, request, RENTER_ID);

        assertEquals("success", response.getStatus());

        ArgumentCaptor<RentalCycle> captor = ArgumentCaptor.forClass(RentalCycle.class);
        verify(cycleRepository).save(captor.capture());
        RentalCycle saved = captor.getValue();
        assertEquals(saved.getStartedAt().plusHours(6), saved.getExpectedReturnAt());
    }

    @Test
    void startCycle_limitedType_shouldIgnoreClientProvidedExpectedReturnAt() {
        RentalCycle cycle = RentalCycle.builder()
                .id(CYCLE_ID).renterId(RENTER_ID).ownerId(OWNER_ID)
                .rateType(RentalRateType.DAILY)
                .rentalType(RentalType.LIMITED)
                .durationValue(2)
                .status(RentalCycleStatus.AWAITING_START).build();
        when(cycleRepository.findByIdAndIsDeletedFalse(CYCLE_ID)).thenReturn(Optional.of(cycle));

        RentalCycleStartRequest request = new RentalCycleStartRequest();
        request.setStartOdometer(10000);
        request.setExpectedReturnAt(LocalDateTime.now().plusDays(30)); // far-off client-supplied value

        GenericResponse response = service.startCycle(CYCLE_ID, request, RENTER_ID);

        assertEquals("success", response.getStatus());

        ArgumentCaptor<RentalCycle> captor = ArgumentCaptor.forClass(RentalCycle.class);
        verify(cycleRepository).save(captor.capture());
        RentalCycle saved = captor.getValue();
        assertEquals(saved.getStartedAt().plusDays(2), saved.getExpectedReturnAt());
    }

    @Test
    void startCycle_unlimitedType_shouldUseClientProvidedExpectedReturnAt() {
        RentalCycle cycle = RentalCycle.builder()
                .id(CYCLE_ID).renterId(RENTER_ID).ownerId(OWNER_ID)
                .rentalType(RentalType.UNLIMITED)
                .status(RentalCycleStatus.AWAITING_START).build();
        when(cycleRepository.findByIdAndIsDeletedFalse(CYCLE_ID)).thenReturn(Optional.of(cycle));

        LocalDateTime clientExpectedReturnAt = LocalDateTime.now().plusDays(5);
        RentalCycleStartRequest request = new RentalCycleStartRequest();
        request.setStartOdometer(10000);
        request.setExpectedReturnAt(clientExpectedReturnAt);

        GenericResponse response = service.startCycle(CYCLE_ID, request, RENTER_ID);

        assertEquals("success", response.getStatus());

        ArgumentCaptor<RentalCycle> captor = ArgumentCaptor.forClass(RentalCycle.class);
        verify(cycleRepository).save(captor.capture());
        assertEquals(clientExpectedReturnAt, captor.getValue().getExpectedReturnAt());
    }

    @Test
    void cancelCycle_afterStart_shouldReturn400() {
        RentalCycle cycle = RentalCycle.builder()
                .id(CYCLE_ID).renterId(RENTER_ID).ownerId(OWNER_ID)
                .status(RentalCycleStatus.ACTIVE).build();
        when(cycleRepository.findByIdAndIsDeletedFalse(CYCLE_ID)).thenReturn(Optional.of(cycle));

        GenericResponse response = service.cancelCycle(CYCLE_ID, RENTER_ID);

        assertEquals("error", response.getStatus());
        assertEquals("400", response.getCode());
    }
}
