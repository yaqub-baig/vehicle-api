package com.autoadda.apis.vehicle.service;

import com.autoadda.apis.vehicle.clients.AuthClient;
import com.autoadda.apis.vehicle.clients.GarageClient;
import com.autoadda.apis.vehicle.dao.BorrowingTransferRepository;
import com.autoadda.apis.vehicle.dao.MediaFileRepository;
import com.autoadda.apis.vehicle.dao.TrustedBorrowerRepository;
import com.autoadda.apis.vehicle.dao.VehicleBorrowingRepository;
import com.autoadda.apis.vehicle.dao.VehicleDetailsRepository;
import com.autoadda.apis.vehicle.entity.VehicleBorrowing;
import com.autoadda.apis.vehicle.entity.VehicleDetails;
import com.autoadda.apis.vehicle.enums.BorrowingStatus;
import com.autoadda.apis.vehicle.mapper.VehicleBorrowingMapper;
import com.autoadda.apis.vehicle.request.QuickBorrowRequest;
import com.autoadda.apis.vehicle.request.QuickReturnRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.response.UserResponse;
import com.autoadda.apis.vehicle.response.VehicleBorrowingResponse;
import com.autoadda.apis.vehicle.service.impl.BorrowingNotificationService;
import com.autoadda.apis.vehicle.service.impl.VehicleBorrowingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleBorrowingServiceTest {

    @Mock
    private VehicleBorrowingRepository borrowingRepository;

    @Mock
    private BorrowingTransferRepository transferRepository;

    @Mock
    private VehicleDetailsRepository vehicleRepository;

    @Mock
    private MediaFileRepository mediaFileRepository;

    @Mock
    private GarageClient garageClient;

    @Mock
    private AuthClient authClient;

    @Mock
    private VehicleBorrowingMapper mapper;

    @Mock
    private BorrowingNotificationService notificationService;

    @Mock
    private TrustedBorrowerRepository trustedBorrowerRepository;

    private VehicleBorrowingServiceImpl service;

    private static final Integer OWNER_ID = 1;
    private static final Integer BORROWER_ID = 2;
    private static final Integer OTHER_USER_ID = 3;
    private static final Integer VEHICLE_ID = 10;

    private VehicleDetails vehicle;

    @BeforeEach
    void setUp() {
        service = new VehicleBorrowingServiceImpl(borrowingRepository, transferRepository,
                vehicleRepository, mediaFileRepository, garageClient, authClient, mapper, notificationService,
                trustedBorrowerRepository);

        vehicle = VehicleDetails.builder()
                .id(VEHICLE_ID)
                .customerId(OWNER_ID)
                .brand("Toyota")
                .model("Corolla")
                .build();

        lenient().when(vehicleRepository.findById(VEHICLE_ID)).thenReturn(Optional.of(vehicle));
        lenient().when(garageClient.getWorkOrdersByVehicleId(VEHICLE_ID))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));
        lenient().when(borrowingRepository.hasActiveBorrowing(VEHICLE_ID)).thenReturn(false);
        lenient().when(mapper.toResponse(any(VehicleBorrowing.class)))
                .thenReturn(VehicleBorrowingResponse.builder().build());
        lenient().when(borrowingRepository.save(any(VehicleBorrowing.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(trustedBorrowerRepository.existsByOwnerIdAndBorrowerIdAndIsDeletedFalse(any(), any()))
                .thenReturn(true);
    }

    // ---- quickBorrow ----

    @Test
    void quickBorrow_ownerAssignsToBorrower_shouldGoStraightToActive() {
        QuickBorrowRequest request = new QuickBorrowRequest();
        request.setVehicleId(VEHICLE_ID);
        request.setBorrowerId(BORROWER_ID);

        when(authClient.getUser(BORROWER_ID)).thenReturn(UserResponse.builder().id(BORROWER_ID).build());

        GenericResponse response = service.quickBorrow(request, OWNER_ID);

        assertEquals("success", response.getStatus());
        assertEquals("201", response.getCode());

        ArgumentCaptor<VehicleBorrowing> captor = ArgumentCaptor.forClass(VehicleBorrowing.class);
        verify(borrowingRepository).save(captor.capture());
        VehicleBorrowing saved = captor.getValue();
        assertEquals(BorrowingStatus.ACTIVE, saved.getStatus());
        assertEquals(BORROWER_ID, saved.getBorrowerId());
        assertEquals(OWNER_ID, saved.getOwnerId());
        assertNotNull(saved.getBorrowingTime());
        verify(notificationService).sendQuickBorrowNotification(any(), any());
    }

    @Test
    void quickBorrow_selfRequest_shouldForceBorrowerToCallerAndIgnoreSuppliedBorrowerId() {
        QuickBorrowRequest request = new QuickBorrowRequest();
        request.setVehicleId(VEHICLE_ID);
        request.setBorrowerId(999); // should be ignored since caller is not the owner

        GenericResponse response = service.quickBorrow(request, OTHER_USER_ID);

        assertEquals("success", response.getStatus());

        ArgumentCaptor<VehicleBorrowing> captor = ArgumentCaptor.forClass(VehicleBorrowing.class);
        verify(borrowingRepository).save(captor.capture());
        VehicleBorrowing saved = captor.getValue();
        assertEquals(BorrowingStatus.ACTIVE, saved.getStatus());
        assertEquals(OTHER_USER_ID, saved.getBorrowerId());
        assertEquals(OWNER_ID, saved.getOwnerId());
        verify(authClient, never()).getUser(anyInt());
    }

    @Test
    void quickBorrow_selfRequestNotTrusted_shouldReturn403() {
        when(trustedBorrowerRepository.existsByOwnerIdAndBorrowerIdAndIsDeletedFalse(OWNER_ID, OTHER_USER_ID))
                .thenReturn(false);
        QuickBorrowRequest request = new QuickBorrowRequest();
        request.setVehicleId(VEHICLE_ID);

        GenericResponse response = service.quickBorrow(request, OTHER_USER_ID);

        assertEquals("error", response.getStatus());
        assertEquals("403", response.getCode());
        verify(borrowingRepository, never()).save(any());
    }

    @Test
    void quickBorrow_ownerBranch_shouldNotCheckTrustedBorrowerList() {
        QuickBorrowRequest request = new QuickBorrowRequest();
        request.setVehicleId(VEHICLE_ID);
        request.setBorrowerId(BORROWER_ID);

        when(authClient.getUser(BORROWER_ID)).thenReturn(UserResponse.builder().id(BORROWER_ID).build());

        GenericResponse response = service.quickBorrow(request, OWNER_ID);

        assertEquals("success", response.getStatus());
        verify(trustedBorrowerRepository, never()).existsByOwnerIdAndBorrowerIdAndIsDeletedFalse(any(), any());
    }

    @Test
    void quickBorrow_vehicleNotFound_shouldReturn404() {
        when(vehicleRepository.findById(VEHICLE_ID)).thenReturn(Optional.empty());
        QuickBorrowRequest request = new QuickBorrowRequest();
        request.setVehicleId(VEHICLE_ID);

        GenericResponse response = service.quickBorrow(request, OTHER_USER_ID);

        assertEquals("error", response.getStatus());
        assertEquals("404", response.getCode());
        verify(borrowingRepository, never()).save(any());
    }

    @Test
    void quickBorrow_ownerMissingBorrowerId_shouldReturn400() {
        QuickBorrowRequest request = new QuickBorrowRequest();
        request.setVehicleId(VEHICLE_ID);

        GenericResponse response = service.quickBorrow(request, OWNER_ID);

        assertEquals("error", response.getStatus());
        assertEquals("400", response.getCode());
        verify(borrowingRepository, never()).save(any());
    }

    @Test
    void quickBorrow_vehicleAlreadyBorrowed_shouldReturn400() {
        when(borrowingRepository.hasActiveBorrowing(VEHICLE_ID)).thenReturn(true);
        QuickBorrowRequest request = new QuickBorrowRequest();
        request.setVehicleId(VEHICLE_ID);

        GenericResponse response = service.quickBorrow(request, OTHER_USER_ID);

        assertEquals("error", response.getStatus());
        assertEquals("400", response.getCode());
        verify(borrowingRepository, never()).save(any());
    }

    @Test
    void quickBorrow_vehicleInGarage_shouldReturn400() {
        mockActiveWorkOrder();
        QuickBorrowRequest request = new QuickBorrowRequest();
        request.setVehicleId(VEHICLE_ID);

        GenericResponse response = service.quickBorrow(request, OTHER_USER_ID);

        assertEquals("error", response.getStatus());
        assertEquals("400", response.getCode());
        verify(borrowingRepository, never()).save(any());
    }

    @Test
    void quickBorrow_ownerBranchBorrowerNotFound_shouldReturn404() {
        when(authClient.getUser(BORROWER_ID)).thenReturn(null);
        QuickBorrowRequest request = new QuickBorrowRequest();
        request.setVehicleId(VEHICLE_ID);
        request.setBorrowerId(BORROWER_ID);

        GenericResponse response = service.quickBorrow(request, OWNER_ID);

        assertEquals("error", response.getStatus());
        assertEquals("404", response.getCode());
        verify(borrowingRepository, never()).save(any());
    }

    @Test
    void quickBorrow_nullExpectedReturnTime_shouldDefaultTo24Hours() {
        QuickBorrowRequest request = new QuickBorrowRequest();
        request.setVehicleId(VEHICLE_ID);

        GenericResponse response = service.quickBorrow(request, OTHER_USER_ID);

        assertEquals("success", response.getStatus());
        ArgumentCaptor<VehicleBorrowing> captor = ArgumentCaptor.forClass(VehicleBorrowing.class);
        verify(borrowingRepository).save(captor.capture());
        LocalDateTime expected = captor.getValue().getExpectedReturnTime();
        assertNotNull(expected);
        assertEquals(true, expected.isAfter(LocalDateTime.now().plusHours(23)));
    }

    @Test
    void quickBorrow_expectedReturnTimeInPast_shouldReturn400() {
        QuickBorrowRequest request = new QuickBorrowRequest();
        request.setVehicleId(VEHICLE_ID);
        request.setExpectedReturnTime(LocalDateTime.now().minusDays(1));

        GenericResponse response = service.quickBorrow(request, OTHER_USER_ID);

        assertEquals("error", response.getStatus());
        assertEquals("400", response.getCode());
        verify(borrowingRepository, never()).save(any());
    }

    private void mockActiveWorkOrder() {
        com.autoadda.apis.vehicle.response.VehicleServiceHistoryResponse workOrder =
                mock(com.autoadda.apis.vehicle.response.VehicleServiceHistoryResponse.class);
        when(workOrder.getStatus()).thenReturn("IN_PROGRESS");
        when(garageClient.getWorkOrdersByVehicleId(VEHICLE_ID))
                .thenReturn(ResponseEntity.ok(List.of(workOrder)));
    }

    // ---- quickReturn ----

    private VehicleBorrowing activeBorrowing() {
        return VehicleBorrowing.builder()
                .id(100L)
                .borrowerId(BORROWER_ID)
                .ownerId(OWNER_ID)
                .vehicleId(VEHICLE_ID)
                .status(BorrowingStatus.ACTIVE)
                .returnConfirmedByBorrower(false)
                .returnConfirmedByOwner(false)
                .isDeleted(false)
                .build();
    }

    @Test
    void quickReturn_fromActiveByBorrower_shouldComplete() {
        VehicleBorrowing borrowing = activeBorrowing();
        when(borrowingRepository.findById(100L)).thenReturn(Optional.of(borrowing));

        GenericResponse response = service.quickReturn(100L, BORROWER_ID, null);

        assertEquals("success", response.getStatus());
        assertEquals("200", response.getCode());

        ArgumentCaptor<VehicleBorrowing> captor = ArgumentCaptor.forClass(VehicleBorrowing.class);
        verify(borrowingRepository).save(captor.capture());
        VehicleBorrowing saved = captor.getValue();
        assertEquals(BorrowingStatus.RETURNED, saved.getStatus());
        assertEquals(Boolean.TRUE, saved.getReturnConfirmedByBorrower());
        assertEquals(Boolean.TRUE, saved.getReturnConfirmedByOwner());
        assertNotNull(saved.getActualReturnTime());
        verify(notificationService).sendReturnConfirmedNotification(any(), any());
    }

    @Test
    void quickReturn_fromActiveByOwner_shouldComplete() {
        VehicleBorrowing borrowing = activeBorrowing();
        when(borrowingRepository.findById(100L)).thenReturn(Optional.of(borrowing));

        GenericResponse response = service.quickReturn(100L, OWNER_ID, null);

        assertEquals("success", response.getStatus());
        assertEquals(BorrowingStatus.RETURNED, borrowing.getStatus());
    }

    @Test
    void quickReturn_fromReturnInitiated_shouldRescueStuckRow() {
        VehicleBorrowing borrowing = activeBorrowing();
        borrowing.setStatus(BorrowingStatus.RETURN_INITIATED);
        borrowing.setReturnInitiatedAt(LocalDateTime.now().minusHours(1));
        borrowing.setReturnConfirmedByBorrower(true);
        when(borrowingRepository.findById(100L)).thenReturn(Optional.of(borrowing));

        GenericResponse response = service.quickReturn(100L, OWNER_ID, null);

        assertEquals("success", response.getStatus());
        assertEquals(BorrowingStatus.RETURNED, borrowing.getStatus());
        assertNotNull(borrowing.getReturnInitiatedAt());
    }

    @Test
    void quickReturn_borrowingNotFound_shouldReturn404() {
        when(borrowingRepository.findById(100L)).thenReturn(Optional.empty());

        GenericResponse response = service.quickReturn(100L, OWNER_ID, null);

        assertEquals("error", response.getStatus());
        assertEquals("404", response.getCode());
    }

    @Test
    void quickReturn_callerNotBorrowerOrOwner_shouldReturn403() {
        VehicleBorrowing borrowing = activeBorrowing();
        when(borrowingRepository.findById(100L)).thenReturn(Optional.of(borrowing));

        GenericResponse response = service.quickReturn(100L, OTHER_USER_ID, null);

        assertEquals("error", response.getStatus());
        assertEquals("403", response.getCode());
        verify(borrowingRepository, never()).save(any());
    }

    @Test
    void quickReturn_ineligibleStatus_shouldReturn400() {
        VehicleBorrowing borrowing = activeBorrowing();
        borrowing.setStatus(BorrowingStatus.PENDING);
        when(borrowingRepository.findById(100L)).thenReturn(Optional.of(borrowing));

        GenericResponse response = service.quickReturn(100L, OWNER_ID, null);

        assertEquals("error", response.getStatus());
        assertEquals("400", response.getCode());
        verify(borrowingRepository, never()).save(any());
    }

    @Test
    void quickReturn_borrowerNotTrusted_shouldReturn403() {
        VehicleBorrowing borrowing = activeBorrowing();
        when(borrowingRepository.findById(100L)).thenReturn(Optional.of(borrowing));
        when(trustedBorrowerRepository.existsByOwnerIdAndBorrowerIdAndIsDeletedFalse(OWNER_ID, BORROWER_ID))
                .thenReturn(false);

        GenericResponse response = service.quickReturn(100L, BORROWER_ID, null);

        assertEquals("error", response.getStatus());
        assertEquals("403", response.getCode());
        verify(borrowingRepository, never()).save(any());
    }

    @Test
    void quickReturn_ownerAlwaysAllowed_evenWhenBorrowerNotTrusted() {
        VehicleBorrowing borrowing = activeBorrowing();
        when(borrowingRepository.findById(100L)).thenReturn(Optional.of(borrowing));
        when(trustedBorrowerRepository.existsByOwnerIdAndBorrowerIdAndIsDeletedFalse(OWNER_ID, BORROWER_ID))
                .thenReturn(false);

        GenericResponse response = service.quickReturn(100L, OWNER_ID, null);

        assertEquals("success", response.getStatus());
        assertEquals(BorrowingStatus.RETURNED, borrowing.getStatus());
        verify(trustedBorrowerRepository, never())
                .existsByOwnerIdAndBorrowerIdAndIsDeletedFalse(OWNER_ID, OWNER_ID);
    }

    @Test
    void quickReturn_withOptionalFields_shouldApplyThem() {
        VehicleBorrowing borrowing = activeBorrowing();
        when(borrowingRepository.findById(100L)).thenReturn(Optional.of(borrowing));

        QuickReturnRequest request = new QuickReturnRequest();
        request.setReturnNotes("all good");
        request.setKeyReturned(true);

        GenericResponse response = service.quickReturn(100L, BORROWER_ID, request);

        assertEquals("success", response.getStatus());
        assertEquals("all good", borrowing.getReturnNotes());
        assertEquals(Boolean.TRUE, borrowing.getKeyReturned());
        assertEquals(Boolean.TRUE, borrowing.getKeyReturnedConfirmedByOwner());
    }
}
