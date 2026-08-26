package com.autoadda.apis.vehicle.service;

import com.autoadda.apis.vehicle.dao.CarRentalListingRepository;
import com.autoadda.apis.vehicle.dao.RentalCycleRepository;
import com.autoadda.apis.vehicle.dao.RentalRequestRepository;
import com.autoadda.apis.vehicle.entity.CarRentalListing;
import com.autoadda.apis.vehicle.entity.RentalCycle;
import com.autoadda.apis.vehicle.entity.RentalRequest;
import com.autoadda.apis.vehicle.enums.RentalListingStatus;
import com.autoadda.apis.vehicle.enums.RentalRateType;
import com.autoadda.apis.vehicle.enums.RentalRequestStatus;
import com.autoadda.apis.vehicle.enums.RentalType;
import com.autoadda.apis.vehicle.mapper.RentalCycleMapper;
import com.autoadda.apis.vehicle.mapper.RentalRequestMapper;
import com.autoadda.apis.vehicle.request.RentalRequestCreateRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.response.RentalRequestResponse;
import com.autoadda.apis.vehicle.service.impl.RentalNotificationService;
import com.autoadda.apis.vehicle.service.impl.RentalRequestServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RentalRequestServiceTest {

    @Mock
    private RentalRequestRepository requestRepository;

    @Mock
    private CarRentalListingRepository listingRepository;

    @Mock
    private RentalCycleRepository cycleRepository;

    @Mock
    private RentalRequestMapper requestMapper;

    @Mock
    private RentalCycleMapper cycleMapper;

    @Mock
    private RentalNotificationService notificationService;

    private RentalRequestServiceImpl service;

    private static final Integer OWNER_ID = 1;
    private static final Integer RENTER_ID = 2;
    private static final Long LISTING_ID = 100L;
    private static final Integer VEHICLE_ID = 10;

    private CarRentalListing listing;

    @BeforeEach
    void setUp() {
        service = new RentalRequestServiceImpl(requestRepository, listingRepository, cycleRepository,
                requestMapper, cycleMapper, notificationService);

        listing = CarRentalListing.builder()
                .id(LISTING_ID).vehicleId(VEHICLE_ID).ownerId(OWNER_ID)
                .status(RentalListingStatus.ACTIVE).build();

        lenient().when(listingRepository.findByIdAndIsDeletedFalse(LISTING_ID)).thenReturn(Optional.of(listing));
        lenient().when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing));
        lenient().when(requestRepository.hasActiveRequest(any(), any())).thenReturn(false);
        lenient().when(cycleRepository.existsByVehicleIdAndStatusInAndIsDeletedFalse(any(), any())).thenReturn(false);
        lenient().when(requestMapper.toResponse(any(RentalRequest.class), any()))
                .thenReturn(RentalRequestResponse.builder().build());
        lenient().when(requestRepository.save(any(RentalRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private RentalRequestCreateRequest buildCreateRequest() {
        RentalRequestCreateRequest request = new RentalRequestCreateRequest();
        request.setListingId(LISTING_ID);
        request.setRequestedRateType(RentalRateType.DAILY);
        request.setRentalType(RentalType.UNLIMITED);
        return request;
    }

    @Test
    void createRequest_selfRent_shouldReturn400() {
        GenericResponse response = service.createRequest(buildCreateRequest(), OWNER_ID);

        assertEquals("error", response.getStatus());
        assertEquals("400", response.getCode());
    }

    @Test
    void createRequest_success_shouldSaveAndNotify() {
        when(requestMapper.toEntity(any(RentalRequestCreateRequest.class), any(CarRentalListing.class), any()))
                .thenReturn(RentalRequest.builder().listingId(LISTING_ID).vehicleId(VEHICLE_ID)
                        .renterId(RENTER_ID).ownerId(OWNER_ID)
                        .requestedRateType(RentalRateType.DAILY)
                        .status(RentalRequestStatus.PENDING).isDeleted(false).build());

        GenericResponse response = service.createRequest(buildCreateRequest(), RENTER_ID);

        assertEquals("success", response.getStatus());
        assertEquals("201", response.getCode());
        verify(notificationService).sendRequestCreatedNotification(any(), any());
    }

    @Test
    void createRequest_limitedTypeMissingDuration_shouldReturn400() {
        RentalRequestCreateRequest request = buildCreateRequest();
        request.setRentalType(RentalType.LIMITED);

        GenericResponse response = service.createRequest(request, RENTER_ID);

        assertEquals("error", response.getStatus());
        assertEquals("400", response.getCode());
    }

    @Test
    void createRequest_limitedTypeZeroOrNegativeDuration_shouldReturn400() {
        RentalRequestCreateRequest request = buildCreateRequest();
        request.setRentalType(RentalType.LIMITED);
        request.setDurationValue(0);

        GenericResponse response = service.createRequest(request, RENTER_ID);

        assertEquals("error", response.getStatus());
        assertEquals("400", response.getCode());
    }

    @Test
    void createRequest_limitedTypeValidDuration_shouldSucceed() {
        RentalRequestCreateRequest request = buildCreateRequest();
        request.setRentalType(RentalType.LIMITED);
        request.setDurationValue(3);

        when(requestMapper.toEntity(any(RentalRequestCreateRequest.class), any(CarRentalListing.class), any()))
                .thenReturn(RentalRequest.builder().listingId(LISTING_ID).vehicleId(VEHICLE_ID)
                        .renterId(RENTER_ID).ownerId(OWNER_ID)
                        .requestedRateType(RentalRateType.DAILY)
                        .rentalType(RentalType.LIMITED).durationValue(3)
                        .status(RentalRequestStatus.PENDING).isDeleted(false).build());

        GenericResponse response = service.createRequest(request, RENTER_ID);

        assertEquals("success", response.getStatus());
        assertEquals("201", response.getCode());
    }

    @Test
    void createRequest_unlimitedTypeWithDuration_shouldNullifyDuration() {
        RentalRequestCreateRequest request = buildCreateRequest();
        request.setRentalType(RentalType.UNLIMITED);
        request.setDurationValue(5);

        when(requestMapper.toEntity(any(RentalRequestCreateRequest.class), any(CarRentalListing.class), any()))
                .thenReturn(RentalRequest.builder().listingId(LISTING_ID).vehicleId(VEHICLE_ID)
                        .renterId(RENTER_ID).ownerId(OWNER_ID)
                        .requestedRateType(RentalRateType.DAILY)
                        .status(RentalRequestStatus.PENDING).isDeleted(false).build());

        service.createRequest(request, RENTER_ID);

        ArgumentCaptor<RentalRequestCreateRequest> captor = ArgumentCaptor.forClass(RentalRequestCreateRequest.class);
        verify(requestMapper).toEntity(captor.capture(), any(CarRentalListing.class), any());
        assertNull(captor.getValue().getDurationValue());
    }

    @Test
    void acceptRequest_notOwner_shouldReturn403() {
        RentalRequest request = RentalRequest.builder()
                .id(1L).listingId(LISTING_ID).vehicleId(VEHICLE_ID)
                .renterId(RENTER_ID).ownerId(OWNER_ID).status(RentalRequestStatus.PENDING).build();
        when(requestRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(request));

        GenericResponse response = service.acceptRequest(1L, RENTER_ID);

        assertEquals("error", response.getStatus());
        assertEquals("403", response.getCode());
    }

    @Test
    void acceptRequest_success_shouldCreateRentalCycle() {
        RentalRequest request = RentalRequest.builder()
                .id(1L).listingId(LISTING_ID).vehicleId(VEHICLE_ID)
                .renterId(RENTER_ID).ownerId(OWNER_ID).status(RentalRequestStatus.PENDING).build();
        when(requestRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(request));
        when(cycleMapper.toEntity(any(RentalRequest.class), any(CarRentalListing.class)))
                .thenReturn(RentalCycle.builder().rentalRequestId(1L).build());

        GenericResponse response = service.acceptRequest(1L, OWNER_ID);

        assertEquals("success", response.getStatus());
        assertEquals("200", response.getCode());

        ArgumentCaptor<RentalRequest> requestCaptor = ArgumentCaptor.forClass(RentalRequest.class);
        verify(requestRepository).save(requestCaptor.capture());
        assertEquals(RentalRequestStatus.ACCEPTED, requestCaptor.getValue().getStatus());

        verify(cycleRepository).save(any(RentalCycle.class));
        verify(notificationService).sendRequestAcceptedNotification(any(), any());
    }
}
