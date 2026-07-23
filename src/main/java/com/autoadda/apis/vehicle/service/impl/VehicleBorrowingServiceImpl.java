package com.autoadda.apis.vehicle.service.impl;

import com.autoadda.apis.vehicle.clients.GarageClient;
import com.autoadda.apis.vehicle.dao.BorrowingTransferRepository;
import com.autoadda.apis.vehicle.dao.MediaFileRepository;
import com.autoadda.apis.vehicle.dao.TrustedBorrowerRepository;
import com.autoadda.apis.vehicle.dao.VehicleBorrowingRepository;
import com.autoadda.apis.vehicle.dao.VehicleDetailsRepository;
import com.autoadda.apis.vehicle.entity.BorrowingTransfer;
import com.autoadda.apis.vehicle.entity.MediaFile;
import com.autoadda.apis.vehicle.entity.VehicleBorrowing;
import com.autoadda.apis.vehicle.entity.VehicleDetails;
import com.autoadda.apis.vehicle.enums.BorrowingStatus;
import com.autoadda.apis.vehicle.enums.TransferStatus;
import com.autoadda.apis.vehicle.mapper.VehicleBorrowingMapper;
import com.autoadda.apis.vehicle.clients.AuthClient;
import com.autoadda.apis.vehicle.request.BorrowingRejectionRequest;
import com.autoadda.apis.vehicle.request.BorrowingReturnRequest;
import com.autoadda.apis.vehicle.request.BorrowingTransferRequest;
import com.autoadda.apis.vehicle.request.ConfirmReturnRequest;
import com.autoadda.apis.vehicle.request.OwnerInitiatedBorrowingRequest;
import com.autoadda.apis.vehicle.request.QuickBorrowRequest;
import com.autoadda.apis.vehicle.request.QuickReturnRequest;
import com.autoadda.apis.vehicle.request.TransferRejectionRequest;
import com.autoadda.apis.vehicle.request.VehicleBorrowingRequest;
import com.autoadda.apis.vehicle.response.UserResponse;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.response.VehicleServiceHistoryResponse;
import com.autoadda.apis.vehicle.service.VehicleBorrowingService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Collections;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleBorrowingServiceImpl implements VehicleBorrowingService {

    private static final String ERROR = "error";
    private static final String SUCCESS = "success";
    private static final String BORROWING_NOT_FOUND = "Borrowing record not found";
    private static final Set<String> TERMINAL_WORK_ORDER_STATUSES = Set.of(
            "DELIVERED_TO_CUSTOMER", "COMPLETED", "CANCELLED", "REJECTED"
    );
    public static final String BORROWINGS_RETRIEVED_SUCCESSFULLY = "Borrowings retrieved successfully";

    private final VehicleBorrowingRepository borrowingRepository;
    private final BorrowingTransferRepository transferRepository;
    private final VehicleDetailsRepository vehicleRepository;
    private final MediaFileRepository mediaFileRepository;
    private final GarageClient garageClient;
    private final AuthClient authClient;
    private final VehicleBorrowingMapper mapper;
    private final BorrowingNotificationService notificationService;
    private final TrustedBorrowerRepository trustedBorrowerRepository;

    @Override
    @Transactional
    public GenericResponse createBorrowingRequest(VehicleBorrowingRequest request, Integer borrowerId) {
        log.info("Creating borrowing request for vehicle {} by user {}", request.getVehicleId(), borrowerId);

        Optional<VehicleDetails> vehicleOpt = vehicleRepository.findById(request.getVehicleId());
        if (vehicleOpt.isEmpty()) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("404")
                    .message("Vehicle not found")
                    .build();
        }

        VehicleDetails vehicle = vehicleOpt.get();

        if (vehicle.getCustomerId().equals(borrowerId)) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("400")
                    .message("Cannot borrow your own vehicle")
                    .build();
        }

        if (borrowingRepository.hasActiveBorrowing(request.getVehicleId())) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("400")
                    .message("Vehicle is already borrowed or has a pending borrowing request")
                    .build();
        }

        if (!isVehicleNotInGarage(request.getVehicleId())) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("400")
                    .message("Vehicle is currently in service at a garage")
                    .build();
        }

        if (request.getExpectedReturnTime().isBefore(LocalDateTime.now())) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("400")
                    .message("Expected return time must be in the future")
                    .build();
        }

        List<MediaFile> borrowingImages = resolveMediaFiles(request.getBorrowingImageIds());

        VehicleBorrowing borrowing = mapper.toEntity(request, borrowerId, vehicle.getCustomerId(), borrowingImages);
        VehicleBorrowing savedBorrowing = borrowingRepository.save(borrowing);

        log.info("Borrowing request created successfully with ID: {}", savedBorrowing.getId());

        notificationService.sendBorrowingRequestNotification(savedBorrowing, vehicle);

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("201")
                .message("Borrowing request created successfully. Awaiting owner approval.")
                .body(mapper.toResponse(savedBorrowing))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse approveBorrowingRequest(Long borrowingId, Integer ownerId) {
        log.info("Approving borrowing request {} by owner {}", borrowingId, ownerId);

        Optional<VehicleBorrowing> borrowingOpt = borrowingRepository.findById(borrowingId);
        if (borrowingOpt.isEmpty()) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("404")
                    .message(BORROWING_NOT_FOUND)
                    .build();
        }

        VehicleBorrowing borrowing = borrowingOpt.get();

        if (!borrowing.getOwnerId().equals(ownerId)) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("403")
                    .message("You are not authorized to approve this request")
                    .build();
        }

        if (borrowing.getStatus() != BorrowingStatus.PENDING) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("400")
                    .message("Only pending requests can be approved")
                    .build();
        }

        borrowing.setStatus(BorrowingStatus.APPROVED);
        VehicleBorrowing savedBorrowing = borrowingRepository.save(borrowing);

        log.info("Borrowing request {} approved by owner {}", borrowingId, ownerId);

        Optional<VehicleDetails> vehicleOpt = vehicleRepository.findById(borrowing.getVehicleId());
        vehicleOpt.ifPresent(vehicle -> notificationService.sendApprovalNotification(savedBorrowing, vehicle));

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Borrowing request approved. Borrower can now pick up the vehicle.")
                .body(mapper.toResponse(savedBorrowing))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse rejectBorrowingRequest(Long borrowingId, Integer ownerId, BorrowingRejectionRequest request) {
        log.info("Rejecting borrowing request {} by owner {}", borrowingId, ownerId);

        Optional<VehicleBorrowing> borrowingOpt = borrowingRepository.findById(borrowingId);
        if (borrowingOpt.isEmpty()) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("404")
                    .message(BORROWING_NOT_FOUND)
                    .build();
        }

        VehicleBorrowing borrowing = borrowingOpt.get();

        if (!borrowing.getOwnerId().equals(ownerId)) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("403")
                    .message("You are not authorized to reject this request")
                    .build();
        }

        if (borrowing.getStatus() != BorrowingStatus.PENDING) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("400")
                    .message("Only pending requests can be rejected")
                    .build();
        }

        borrowing.setStatus(BorrowingStatus.REJECTED);
        if (request != null) {
            borrowing.setRejectionReason(request.getRejectionReason());
        }
        VehicleBorrowing savedBorrowing = borrowingRepository.save(borrowing);

        log.info("Borrowing request {} rejected by owner {}", borrowingId, ownerId);

        Optional<VehicleDetails> vehicleOpt = vehicleRepository.findById(borrowing.getVehicleId());
        vehicleOpt.ifPresent(vehicle -> notificationService.sendRejectionNotification(savedBorrowing, vehicle));

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Borrowing request rejected")
                .body(mapper.toResponse(savedBorrowing))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse startBorrowing(Long borrowingId, Integer borrowerId) {
        log.info("Starting borrowing {} by borrower {}", borrowingId, borrowerId);

        Optional<VehicleBorrowing> borrowingOpt = borrowingRepository.findById(borrowingId);
        if (borrowingOpt.isEmpty()) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("404")
                    .message(BORROWING_NOT_FOUND)
                    .build();
        }

        VehicleBorrowing borrowing = borrowingOpt.get();

        if (!borrowing.getBorrowerId().equals(borrowerId)) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("403")
                    .message("You are not authorized to start this borrowing")
                    .build();
        }

        if (borrowing.getStatus() != BorrowingStatus.APPROVED) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("400")
                    .message("Only approved requests can be started")
                    .build();
        }

        borrowing.setStatus(BorrowingStatus.ACTIVE);
        borrowing.setBorrowingTime(LocalDateTime.now());
        VehicleBorrowing savedBorrowing = borrowingRepository.save(borrowing);

        log.info("Borrowing {} started by borrower {}", borrowingId, borrowerId);

        Optional<VehicleDetails> vehicleOpt = vehicleRepository.findById(borrowing.getVehicleId());
        vehicleOpt.ifPresent(vehicle -> notificationService.sendBorrowingStartedNotification(savedBorrowing, vehicle));

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Borrowing started successfully")
                .body(mapper.toResponse(savedBorrowing))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse initiateReturn(Long borrowingId, Integer borrowerId, BorrowingReturnRequest request) {
        log.info("Initiating return for borrowing {} by borrower {}", borrowingId, borrowerId);

        Optional<VehicleBorrowing> borrowingOpt = borrowingRepository.findById(borrowingId);
        if (borrowingOpt.isEmpty()) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("404")
                    .message(BORROWING_NOT_FOUND)
                    .build();
        }

        VehicleBorrowing borrowing = borrowingOpt.get();

        if (!borrowing.getBorrowerId().equals(borrowerId)) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("403")
                    .message("You are not authorized to initiate return for this borrowing")
                    .build();
        }

        if (borrowing.getStatus() != BorrowingStatus.ACTIVE) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("400")
                    .message("Only active borrowings can initiate return")
                    .build();
        }

        borrowing.setStatus(BorrowingStatus.RETURN_INITIATED);
        if (request != null) {
            borrowing.setReturnNotes(request.getReturnNotes());
            borrowing.setKeyReturned(request.getKeyReturned());
            List<MediaFile> returnImages = resolveMediaFiles(request.getReturnImageIds());
            if (!returnImages.isEmpty()) {
                if (borrowing.getReturnImages() == null) {
                    borrowing.setReturnImages(new ArrayList<>());
                }
                borrowing.getReturnImages().clear();
                borrowing.getReturnImages().addAll(returnImages);
            }
        }
        borrowing.setReturnConfirmedByBorrower(true);
        borrowing.setReturnInitiatedAt(LocalDateTime.now());
        borrowing.setReturnRejectionReason(null);
        borrowing.setReturnConfirmedByOwner(false);
        borrowing.setKeyReturnedConfirmedByOwner(null);
        VehicleBorrowing savedBorrowing = borrowingRepository.save(borrowing);

        log.info("Return initiated for borrowing {} by borrower {}", borrowingId, borrowerId);

        Optional<VehicleDetails> vehicleOpt = vehicleRepository.findById(borrowing.getVehicleId());
        vehicleOpt.ifPresent(vehicle -> notificationService.sendReturnInitiatedNotification(savedBorrowing, vehicle));

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Return initiated. Both parties need to confirm the return.")
                .body(mapper.toResponse(savedBorrowing))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse confirmReturn(Long borrowingId, Integer userId, ConfirmReturnRequest request) {
        log.info("Confirming return for borrowing {} by user {}", borrowingId, userId);

        Optional<VehicleBorrowing> borrowingOpt = borrowingRepository.findById(borrowingId);
        if (borrowingOpt.isEmpty()) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("404")
                    .message(BORROWING_NOT_FOUND)
                    .build();
        }

        VehicleBorrowing borrowing = borrowingOpt.get();

        if (borrowing.getStatus() != BorrowingStatus.RETURN_INITIATED) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("400")
                    .message("Return must be initiated before confirmation")
                    .build();
        }

        if (userId.equals(borrowing.getBorrowerId())) {
            if (Boolean.TRUE.equals(borrowing.getReturnConfirmedByBorrower())) {
                return GenericResponse.builder()
                        .status(ERROR)
                        .code("400")
                        .message("You have already confirmed the return")
                        .build();
            }
            borrowing.setReturnConfirmedByBorrower(true);
            log.info("Return confirmed by borrower {} for borrowing {}", userId, borrowingId);
        } else if (userId.equals(borrowing.getOwnerId())) {
            if (Boolean.TRUE.equals(borrowing.getReturnConfirmedByOwner())) {
                return GenericResponse.builder()
                        .status(ERROR)
                        .code("400")
                        .message("You have already confirmed the return")
                        .build();
            }
            borrowing.setReturnConfirmedByOwner(true);
            if (request != null && request.getKeyReturned() != null) {
                borrowing.setKeyReturnedConfirmedByOwner(request.getKeyReturned());
            }
            log.info("Return confirmed by owner {} for borrowing {}", userId, borrowingId);
        } else {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("403")
                    .message("You are not authorized to confirm return for this borrowing")
                    .build();
        }

        if (Boolean.TRUE.equals(borrowing.getReturnConfirmedByBorrower()) &&
                Boolean.TRUE.equals(borrowing.getReturnConfirmedByOwner())) {
            borrowing.setStatus(BorrowingStatus.RETURNED);
            borrowing.setActualReturnTime(LocalDateTime.now());
            log.info("Borrowing {} completed - both parties confirmed return", borrowingId);
        }

        VehicleBorrowing savedBorrowing = borrowingRepository.save(borrowing);

        Optional<VehicleDetails> vehicleOpt = vehicleRepository.findById(borrowing.getVehicleId());
        if (savedBorrowing.getStatus() == BorrowingStatus.RETURNED) {
            vehicleOpt.ifPresent(vehicle -> notificationService.sendReturnConfirmedNotification(savedBorrowing, vehicle));
        }

        String message = savedBorrowing.getStatus() == BorrowingStatus.RETURNED
                ? "Vehicle return completed successfully"
                : "Return confirmation recorded. Waiting for other party to confirm.";

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message(message)
                .body(mapper.toResponse(savedBorrowing))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse rejectReturn(Long borrowingId, Integer ownerId, BorrowingRejectionRequest request) {
        log.info("Rejecting return for borrowing {} by owner {}", borrowingId, ownerId);

        Optional<VehicleBorrowing> borrowingOpt = borrowingRepository.findById(borrowingId);
        if (borrowingOpt.isEmpty()) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("404")
                    .message(BORROWING_NOT_FOUND)
                    .build();
        }

        VehicleBorrowing borrowing = borrowingOpt.get();

        if (!borrowing.getOwnerId().equals(ownerId)) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("403")
                    .message("You are not authorized to reject this return")
                    .build();
        }

        if (borrowing.getStatus() != BorrowingStatus.RETURN_INITIATED) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("400")
                    .message("Only returns in RETURN_INITIATED status can be rejected")
                    .build();
        }

        borrowing.setStatus(BorrowingStatus.ACTIVE);
        if (request != null) {
            borrowing.setReturnRejectionReason(request.getRejectionReason());
            if (request.getKeyReturned() != null) {
                borrowing.setKeyReturnedConfirmedByOwner(request.getKeyReturned());
            }
        }
        borrowing.setReturnConfirmedByBorrower(false);
        borrowing.setReturnConfirmedByOwner(false);
        borrowing.setReturnInitiatedAt(null);

        VehicleBorrowing savedBorrowing = borrowingRepository.save(borrowing);
        log.info("Return rejected for borrowing {} by owner {}", borrowingId, ownerId);

        Optional<VehicleDetails> vehicleOpt = vehicleRepository.findById(borrowing.getVehicleId());
        vehicleOpt.ifPresent(vehicle -> notificationService.sendReturnRejectedNotification(savedBorrowing, vehicle));

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Return rejected. Vehicle remains with the borrower.")
                .body(mapper.toResponse(savedBorrowing))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse cancelBorrowingRequest(Long borrowingId, Integer borrowerId) {
        log.info("Cancelling borrowing request {} by borrower {}", borrowingId, borrowerId);

        Optional<VehicleBorrowing> borrowingOpt = borrowingRepository.findById(borrowingId);
        if (borrowingOpt.isEmpty()) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("404")
                    .message(BORROWING_NOT_FOUND)
                    .build();
        }

        VehicleBorrowing borrowing = borrowingOpt.get();

        if (!borrowing.getBorrowerId().equals(borrowerId)) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("403")
                    .message("You are not authorized to cancel this request")
                    .build();
        }

        if (borrowing.getStatus() != BorrowingStatus.PENDING &&
                borrowing.getStatus() != BorrowingStatus.APPROVED) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("400")
                    .message("Only pending or approved requests can be cancelled")
                    .build();
        }

        borrowing.setStatus(BorrowingStatus.CANCELLED);
        VehicleBorrowing savedBorrowing = borrowingRepository.save(borrowing);

        log.info("Borrowing request {} cancelled by borrower {}", borrowingId, borrowerId);

        Optional<VehicleDetails> vehicleOpt = vehicleRepository.findById(borrowing.getVehicleId());
        vehicleOpt.ifPresent(vehicle -> notificationService.sendCancellationNotification(savedBorrowing, vehicle));

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Borrowing request cancelled successfully")
                .body(mapper.toResponse(savedBorrowing))
                .build();
    }

    @Override
    public GenericResponse getBorrowingById(Long borrowingId) {
        log.info("Fetching borrowing by ID: {}", borrowingId);

        Optional<VehicleBorrowing> borrowingOpt = borrowingRepository.findById(borrowingId);
        if (borrowingOpt.isEmpty()) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("404")
                    .message(BORROWING_NOT_FOUND)
                    .build();
        }

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Borrowing retrieved successfully")
                .body(mapper.toResponse(borrowingOpt.get()))
                .build();
    }

    @Override
    public GenericResponse getBorrowingsByVehicle(Integer vehicleId) {
        log.info("Fetching borrowings for vehicle: {}", vehicleId);

        List<VehicleBorrowing> borrowings = borrowingRepository
                .findByVehicleIdAndIsDeletedFalseOrderByCreatedAtDesc(vehicleId);

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message(BORROWINGS_RETRIEVED_SUCCESSFULLY)
                .body(mapper.toResponseList(borrowings))
                .build();
    }

    @Override
    public GenericResponse getBorrowingsByBorrower(Integer borrowerId) {
        log.info("Fetching borrowings for borrower: {}", borrowerId);

        List<VehicleBorrowing> borrowings = borrowingRepository
                .findByBorrowerIdAndIsDeletedFalseOrderByCreatedAtDesc(borrowerId);

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message(BORROWINGS_RETRIEVED_SUCCESSFULLY)
                .body(mapper.toResponseList(borrowings))
                .build();
    }

    @Override
    public GenericResponse getBorrowingsByOwner(Integer ownerId) {
        log.info("Fetching borrowings for owner: {}", ownerId);

        List<VehicleBorrowing> borrowings = borrowingRepository
                .findByOwnerIdAndIsDeletedFalseOrderByCreatedAtDesc(ownerId);

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message(BORROWINGS_RETRIEVED_SUCCESSFULLY)
                .body(mapper.toResponseList(borrowings))
                .build();
    }

    @Override
    public GenericResponse getPendingBorrowingsForOwner(Integer ownerId) {
        log.info("Fetching pending borrowings for owner: {}", ownerId);

        List<VehicleBorrowing> borrowings = borrowingRepository.findPendingBorrowingsForOwner(ownerId);

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Pending borrowings retrieved successfully")
                .body(mapper.toResponseList(borrowings))
                .build();
    }

    @Override
    public GenericResponse getActiveBorrowingForVehicle(Integer vehicleId) {
        log.info("Fetching active borrowing for vehicle: {}", vehicleId);

        List<BorrowingStatus> activeStatuses = Arrays.asList(
                BorrowingStatus.PENDING,
                BorrowingStatus.APPROVED,
                BorrowingStatus.ACTIVE,
                BorrowingStatus.TRANSFER_REQUESTED,
                BorrowingStatus.RETURN_INITIATED
        );

        Optional<VehicleBorrowing> borrowingOpt = borrowingRepository
                .findByVehicleIdAndStatusInAndIsDeletedFalse(vehicleId, activeStatuses);

        if (borrowingOpt.isEmpty()) {
            return GenericResponse.builder()
                    .status(SUCCESS)
                    .code("200")
                    .message("No active borrowing found for this vehicle")
                    .body(null)
                    .build();
        }

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Active borrowing retrieved successfully")
                .body(mapper.toResponse(borrowingOpt.get()))
                .build();
    }

    @Override
    public boolean isVehicleAvailableForBorrowing(Integer vehicleId) {
        boolean hasActiveBorrowing = borrowingRepository.hasActiveBorrowing(vehicleId);
        boolean notInGarage = isVehicleNotInGarage(vehicleId);
        return !hasActiveBorrowing && notInGarage;
    }

    @Override
    @Transactional
    public GenericResponse createOwnerInitiatedBorrowing(OwnerInitiatedBorrowingRequest request, Integer ownerId) {
        log.info("Owner {} initiating borrowing for vehicle {} to user {}",
                ownerId, request.getVehicleId(), request.getBorrowerId());

        // 1. Validate vehicle exists
        Optional<VehicleDetails> vehicleOpt = vehicleRepository.findById(request.getVehicleId());
        if (vehicleOpt.isEmpty()) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("404")
                    .message("Vehicle not found")
                    .build();
        }

        VehicleDetails vehicle = vehicleOpt.get();

        // 2. Verify caller is the vehicle owner
        if (!vehicle.getCustomerId().equals(ownerId)) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("403")
                    .message("You can only assign your own vehicles")
                    .build();
        }

        // 3. Verify borrower is not the owner
//        if (request.getBorrowerId().equals(ownerId)) {
//            return GenericResponse.builder()
//                    .status(ERROR)
//                    .code("400")
//                    .message("Cannot assign vehicle to yourself")
//                    .build();
//        }

        // 4. Check vehicle is not already borrowed
        if (borrowingRepository.hasActiveBorrowing(request.getVehicleId())) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("400")
                    .message("Vehicle is already borrowed or has a pending borrowing request")
                    .build();
        }

        // 5. Check vehicle is not in garage
        if (!isVehicleNotInGarage(request.getVehicleId())) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("400")
                    .message("Vehicle is currently in service at a garage")
                    .build();
        }

        // 6. Validate borrower exists
        UserResponse borrower;
        try {
            borrower = authClient.getUser(request.getBorrowerId());
            if (borrower == null) {
                return GenericResponse.builder()
                        .status(ERROR)
                        .code("404")
                        .message("Borrower not found")
                        .build();
            }
        } catch (Exception e) {
            log.error("Error fetching borrower {}: {}", request.getBorrowerId(), e.getMessage());
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("404")
                    .message("Borrower not found")
                    .build();
        }

        // 7. Validate expectedReturnTime
//        if (request.getExpectedReturnTime().isBefore(LocalDateTime.now())) {
//            return GenericResponse.builder()
//                    .status(ERROR)
//                    .code("400")
//                    .message("Expected return time must be in the future")
//                    .build();
//        }

        // 8. Create borrowing with APPROVED status (skips PENDING)
        VehicleBorrowing borrowing = VehicleBorrowing.builder()
                .borrowerId(request.getBorrowerId())
                .vehicleId(request.getVehicleId())
                .ownerId(ownerId)
                .expectedReturnTime(request.getExpectedReturnTime())
                .borrowingPurpose(request.getBorrowingPurpose())
                .status(BorrowingStatus.APPROVED)
                .returnConfirmedByBorrower(false)
                .returnConfirmedByOwner(false)
                .isDeleted(false)
                .build();

        List<MediaFile> borrowingImages = resolveMediaFiles(request.getBorrowingImageIds());
        if (!borrowingImages.isEmpty()) {
            borrowing.setBorrowingImages(borrowingImages);
        }

        VehicleBorrowing savedBorrowing = borrowingRepository.save(borrowing);

        log.info("Owner-initiated borrowing created with ID: {} in APPROVED status", savedBorrowing.getId());

        // 9. Notify borrower they've been assigned a vehicle
        notificationService.sendOwnerAssignmentNotification(savedBorrowing, vehicle);

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("201")
                .message("Vehicle assigned successfully. Borrower can now pick up the vehicle.")
                .body(mapper.toResponse(savedBorrowing))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse quickBorrow(QuickBorrowRequest request, Integer callerId) {
        log.info("Quick-borrow requested for vehicle {} by caller {}", request.getVehicleId(), callerId);

        Optional<VehicleDetails> vehicleOpt = vehicleRepository.findById(request.getVehicleId());
        if (vehicleOpt.isEmpty()) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("404")
                    .message("Vehicle not found")
                    .build();
        }

        VehicleDetails vehicle = vehicleOpt.get();
        Integer borrowerId;
        Integer ownerId;

        if (vehicle.getCustomerId().equals(callerId)) {
            // Owner branch: owner is assigning the vehicle to a specific borrower.
            if (request.getBorrowerId() == null) {
                return GenericResponse.builder()
                        .status(ERROR)
                        .code("400")
                        .message("Borrower ID is required when assigning as owner")
                        .build();
            }
            borrowerId = request.getBorrowerId();
            ownerId = callerId;

            try {
                UserResponse borrower = authClient.getUser(borrowerId);
                if (borrower == null) {
                    return GenericResponse.builder()
                            .status(ERROR)
                            .code("404")
                            .message("Borrower not found")
                            .build();
                }
            } catch (Exception e) {
                log.error("Error fetching borrower {}: {}", borrowerId, e.getMessage());
                return GenericResponse.builder()
                        .status(ERROR)
                        .code("404")
                        .message("Borrower not found")
                        .build();
            }
        } else {
            // Self-request branch: caller is borrowing for themselves, auto-approved
            // only if the owner has explicitly trusted this caller beforehand.
            if (request.getBorrowerId() != null && !request.getBorrowerId().equals(callerId)) {
                log.info("Ignoring borrowerId {} in self-request quick-borrow; forcing caller {}",
                        request.getBorrowerId(), callerId);
            }
            borrowerId = callerId;
            ownerId = vehicle.getCustomerId();

            if (!trustedBorrowerRepository.existsByOwnerIdAndBorrowerIdAndIsDeletedFalse(ownerId, borrowerId)) {
                return GenericResponse.builder()
                        .status(ERROR)
                        .code("403")
                        .message("You are not a trusted borrower for this owner's vehicles. "
                                + "Ask the owner to add you as a trusted borrower first.")
                        .build();
            }
        }

        if (borrowingRepository.hasActiveBorrowing(request.getVehicleId())) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("400")
                    .message("Vehicle is already borrowed or has a pending borrowing request")
                    .build();
        }

        if (!isVehicleNotInGarage(request.getVehicleId())) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("400")
                    .message("Vehicle is currently in service at a garage")
                    .build();
        }

        LocalDateTime expectedReturnTime = request.getExpectedReturnTime() != null
                ? request.getExpectedReturnTime()
                : LocalDateTime.now().plusHours(24);

        if (expectedReturnTime.isBefore(LocalDateTime.now())) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("400")
                    .message("Expected return time must be in the future")
                    .build();
        }

        List<MediaFile> borrowingImages = resolveMediaFiles(request.getBorrowingImageIds());

        VehicleBorrowing borrowing = VehicleBorrowing.builder()
                .borrowerId(borrowerId)
                .originalBorrowerId(borrowerId)
                .vehicleId(request.getVehicleId())
                .ownerId(ownerId)
                .borrowingTime(LocalDateTime.now())
                .expectedReturnTime(expectedReturnTime)
                .borrowingPurpose(request.getBorrowingPurpose())
                .status(BorrowingStatus.ACTIVE)
                .returnConfirmedByBorrower(false)
                .returnConfirmedByOwner(false)
                .isDeleted(false)
                .build();

        if (!borrowingImages.isEmpty()) {
            borrowing.setBorrowingImages(borrowingImages);
        }

        VehicleBorrowing savedBorrowing = borrowingRepository.save(borrowing);

        log.info("Quick-borrow created with ID: {} in ACTIVE status", savedBorrowing.getId());

        notificationService.sendQuickBorrowNotification(savedBorrowing, vehicle);

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("201")
                .message("Vehicle borrowing started immediately (quick-borrow).")
                .body(mapper.toResponse(savedBorrowing))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse quickReturn(Long borrowingId, Integer callerId, QuickReturnRequest request) {
        log.info("Quick-return requested for borrowing {} by caller {}", borrowingId, callerId);

        Optional<VehicleBorrowing> borrowingOpt = borrowingRepository.findById(borrowingId);
        if (borrowingOpt.isEmpty()) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("404")
                    .message(BORROWING_NOT_FOUND)
                    .build();
        }

        VehicleBorrowing borrowing = borrowingOpt.get();

        if (!callerId.equals(borrowing.getBorrowerId()) && !callerId.equals(borrowing.getOwnerId())) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("403")
                    .message("You are not authorized to return this borrowing")
                    .build();
        }

        if (borrowing.getStatus() != BorrowingStatus.ACTIVE
                && borrowing.getStatus() != BorrowingStatus.RETURN_INITIATED) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("400")
                    .message("Only active or return-initiated borrowings can be quick-returned")
                    .build();
        }

        if (request != null) {
            if (request.getReturnNotes() != null) {
                borrowing.setReturnNotes(request.getReturnNotes());
            }
            if (request.getKeyReturned() != null) {
                borrowing.setKeyReturned(request.getKeyReturned());
                borrowing.setKeyReturnedConfirmedByOwner(request.getKeyReturned());
            }
            List<MediaFile> returnImages = resolveMediaFiles(request.getReturnImageIds());
            if (!returnImages.isEmpty()) {
                if (borrowing.getReturnImages() == null) {
                    borrowing.setReturnImages(new ArrayList<>());
                }
                borrowing.getReturnImages().clear();
                borrowing.getReturnImages().addAll(returnImages);
            }
        }

        borrowing.setStatus(BorrowingStatus.RETURNED);
        borrowing.setActualReturnTime(LocalDateTime.now());
        borrowing.setReturnConfirmedByBorrower(true);
        borrowing.setReturnConfirmedByOwner(true);
        borrowing.setReturnRejectionReason(null);
        if (borrowing.getReturnInitiatedAt() == null) {
            borrowing.setReturnInitiatedAt(LocalDateTime.now());
        }

        VehicleBorrowing savedBorrowing = borrowingRepository.save(borrowing);

        log.info("Quick-return completed for borrowing {} by caller {}", borrowingId, callerId);

        Optional<VehicleDetails> vehicleOpt = vehicleRepository.findById(borrowing.getVehicleId());
        vehicleOpt.ifPresent(vehicle -> notificationService.sendReturnConfirmedNotification(savedBorrowing, vehicle));

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Vehicle return completed successfully (quick-return).")
                .body(mapper.toResponse(savedBorrowing))
                .build();
    }

    @Override
    public GenericResponse searchUserById(Integer userId) {
           log.info("Searching for user by autoAddaId: {}", userId);

            try {
                var response = authClient.getUser(userId);
                if (response != null) {
                    return GenericResponse.builder()
                            .status(SUCCESS)
                            .code("200")
                            .message("User found")
                            .body(response)
                            .build();
                }
                return GenericResponse.builder()
                        .status(ERROR)
                        .code("404")
                        .message("User not found")
                        .build();
            } catch (Exception e) {
                log.error("Error searching for user by Id {}: {}", userId, e.getMessage());
                return GenericResponse.builder()
                        .status(ERROR)
                        .code("404")
                        .message("User not found")
                        .build();
            }


    }

    @Override
    public GenericResponse getBorrowingsByCompany(Long companyId) {
        log.info("Fetching borrowings for company: {}", companyId);

        List<VehicleDetails> companyVehicles = vehicleRepository.findByCompanyId(companyId);
        if (companyVehicles.isEmpty()) {
            return GenericResponse.builder()
                    .status(SUCCESS)
                    .code("200")
                    .message(BORROWINGS_RETRIEVED_SUCCESSFULLY)
                    .body(Collections.emptyList())
                    .build();
        }

        List<Integer> vehicleIds = companyVehicles.stream()
                .map(VehicleDetails::getId)
                .toList();

        List<VehicleBorrowing> borrowings = borrowingRepository
                .findByVehicleIdInAndIsDeletedFalseOrderByCreatedAtDesc(vehicleIds);

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message(BORROWINGS_RETRIEVED_SUCCESSFULLY)
                .body(mapper.toResponseList(borrowings))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse initiateTransfer(Long borrowingId, Integer fromBorrowerId,
                                            BorrowingTransferRequest request) {
        log.info("Initiating transfer on borrowing {} from {} to {}",
                borrowingId, fromBorrowerId, request != null ? request.getToBorrowerId() : null);

        if (request == null || request.getToBorrowerId() == null) {
            return GenericResponse.builder()
                    .status(ERROR).code("400").message("Recipient borrower ID is required").build();
        }

        Optional<VehicleBorrowing> borrowingOpt = borrowingRepository.findById(borrowingId);
        if (borrowingOpt.isEmpty()) {
            return GenericResponse.builder()
                    .status(ERROR).code("404").message(BORROWING_NOT_FOUND).build();
        }

        VehicleBorrowing borrowing = borrowingOpt.get();

        if (!borrowing.getBorrowerId().equals(fromBorrowerId)) {
            return GenericResponse.builder()
                    .status(ERROR).code("403")
                    .message("Only the current borrower can initiate a transfer")
                    .build();
        }

        if (borrowing.getStatus() != BorrowingStatus.ACTIVE) {
            return GenericResponse.builder()
                    .status(ERROR).code("400")
                    .message("Transfers can only be initiated on active borrowings")
                    .build();
        }

        if (transferRepository.hasPendingTransfer(borrowingId)) {
            return GenericResponse.builder()
                    .status(ERROR).code("400")
                    .message("A transfer is already pending on this borrowing")
                    .build();
        }

        Integer toBorrowerId = request.getToBorrowerId();
        if (toBorrowerId.equals(fromBorrowerId)) {
            return GenericResponse.builder()
                    .status(ERROR).code("400").message("Cannot transfer to yourself").build();
        }
        if (toBorrowerId.equals(borrowing.getOwnerId())) {
            return GenericResponse.builder()
                    .status(ERROR).code("400")
                    .message("Cannot transfer to the vehicle owner; use the return flow instead")
                    .build();
        }

        try {
            UserResponse recipient = authClient.getUser(toBorrowerId);
            if (recipient == null) {
                return GenericResponse.builder()
                        .status(ERROR).code("404").message("Recipient user not found").build();
            }
        } catch (Exception e) {
            log.error("Error fetching recipient {}: {}", toBorrowerId, e.getMessage());
            return GenericResponse.builder()
                    .status(ERROR).code("404").message("Recipient user not found").build();
        }

        MediaFile transferImage = null;
        if (request.getTransferImageId() != null) {
            transferImage = mediaFileRepository.findById(request.getTransferImageId()).orElse(null);
        }

        BorrowingTransfer transfer = BorrowingTransfer.builder()
                .borrowingId(borrowingId)
                .fromBorrowerId(fromBorrowerId)
                .toBorrowerId(toBorrowerId)
                .status(TransferStatus.PENDING)
                .reason(request.getReason())
                .notes(request.getNotes())
                .transferImage(transferImage)
                .requestedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();
        BorrowingTransfer savedTransfer = transferRepository.save(transfer);

        borrowing.setStatus(BorrowingStatus.TRANSFER_REQUESTED);
        borrowingRepository.save(borrowing);

        log.info("Transfer {} created on borrowing {} (PENDING)", savedTransfer.getId(), borrowingId);

        Optional<VehicleDetails> vehicleOpt = vehicleRepository.findById(borrowing.getVehicleId());
        vehicleOpt.ifPresent(vehicle ->
                notificationService.sendTransferRequestedNotification(borrowing, savedTransfer, vehicle));

        return GenericResponse.builder()
                .status(SUCCESS).code("201")
                .message("Transfer request created. Awaiting recipient acceptance.")
                .body(mapper.toTransferResponse(savedTransfer))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse acceptTransfer(Long borrowingId, Long transferId, Integer toBorrowerId) {
        log.info("Accepting transfer {} on borrowing {} by {}", transferId, borrowingId, toBorrowerId);

        Optional<BorrowingTransfer> transferOpt = transferRepository.findById(transferId);
        if (transferOpt.isEmpty() || !transferOpt.get().getBorrowingId().equals(borrowingId)) {
            return GenericResponse.builder()
                    .status(ERROR).code("404").message("Transfer not found").build();
        }
        BorrowingTransfer transfer = transferOpt.get();

        if (!transfer.getToBorrowerId().equals(toBorrowerId)) {
            return GenericResponse.builder()
                    .status(ERROR).code("403")
                    .message("Only the intended recipient can accept this transfer")
                    .build();
        }

        if (transfer.getStatus() != TransferStatus.PENDING) {
            return GenericResponse.builder()
                    .status(ERROR).code("400")
                    .message("Only pending transfers can be accepted")
                    .build();
        }

        VehicleBorrowing borrowing = borrowingRepository.findById(borrowingId).orElseThrow();

        transfer.setStatus(TransferStatus.ACCEPTED);
        transfer.setResolvedAt(LocalDateTime.now());
        BorrowingTransfer savedTransfer = transferRepository.save(transfer);

        borrowing.setBorrowerId(toBorrowerId);
        borrowing.setStatus(BorrowingStatus.ACTIVE);
        borrowingRepository.save(borrowing);

        log.info("Transfer {} accepted; borrowing {} rotated to borrower {}",
                transferId, borrowingId, toBorrowerId);

        Optional<VehicleDetails> vehicleOpt = vehicleRepository.findById(borrowing.getVehicleId());
        vehicleOpt.ifPresent(vehicle ->
                notificationService.sendTransferAcceptedNotification(borrowing, savedTransfer, vehicle));

        return GenericResponse.builder()
                .status(SUCCESS).code("200")
                .message("Transfer accepted. You are now the current borrower.")
                .body(mapper.toTransferResponse(savedTransfer))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse rejectTransfer(Long borrowingId, Long transferId, Integer toBorrowerId,
                                           TransferRejectionRequest request) {
        log.info("Rejecting transfer {} on borrowing {} by {}", transferId, borrowingId, toBorrowerId);

        Optional<BorrowingTransfer> transferOpt = transferRepository.findById(transferId);
        if (transferOpt.isEmpty() || !transferOpt.get().getBorrowingId().equals(borrowingId)) {
            return GenericResponse.builder()
                    .status(ERROR).code("404").message("Transfer not found").build();
        }
        BorrowingTransfer transfer = transferOpt.get();

        if (!transfer.getToBorrowerId().equals(toBorrowerId)) {
            return GenericResponse.builder()
                    .status(ERROR).code("403")
                    .message("Only the intended recipient can reject this transfer")
                    .build();
        }

        if (transfer.getStatus() != TransferStatus.PENDING) {
            return GenericResponse.builder()
                    .status(ERROR).code("400")
                    .message("Only pending transfers can be rejected")
                    .build();
        }

        VehicleBorrowing borrowing = borrowingRepository.findById(borrowingId).orElseThrow();

        transfer.setStatus(TransferStatus.REJECTED);
        transfer.setResolvedAt(LocalDateTime.now());
        if (request != null && request.getReason() != null) {
            transfer.setReason(request.getReason());
        }
        BorrowingTransfer savedTransfer = transferRepository.save(transfer);

        borrowing.setStatus(BorrowingStatus.ACTIVE);
        borrowingRepository.save(borrowing);

        log.info("Transfer {} rejected; borrowing {} remains with {}",
                transferId, borrowingId, borrowing.getBorrowerId());

        Optional<VehicleDetails> vehicleOpt = vehicleRepository.findById(borrowing.getVehicleId());
        vehicleOpt.ifPresent(vehicle ->
                notificationService.sendTransferRejectedNotification(borrowing, savedTransfer, vehicle));

        return GenericResponse.builder()
                .status(SUCCESS).code("200")
                .message("Transfer declined")
                .body(mapper.toTransferResponse(savedTransfer))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse cancelTransfer(Long borrowingId, Long transferId, Integer fromBorrowerId) {
        log.info("Cancelling transfer {} on borrowing {} by {}", transferId, borrowingId, fromBorrowerId);

        Optional<BorrowingTransfer> transferOpt = transferRepository.findById(transferId);
        if (transferOpt.isEmpty() || !transferOpt.get().getBorrowingId().equals(borrowingId)) {
            return GenericResponse.builder()
                    .status(ERROR).code("404").message("Transfer not found").build();
        }
        BorrowingTransfer transfer = transferOpt.get();

        if (!transfer.getFromBorrowerId().equals(fromBorrowerId)) {
            return GenericResponse.builder()
                    .status(ERROR).code("403")
                    .message("Only the requesting borrower can cancel this transfer")
                    .build();
        }

        if (transfer.getStatus() != TransferStatus.PENDING) {
            return GenericResponse.builder()
                    .status(ERROR).code("400")
                    .message("Only pending transfers can be cancelled")
                    .build();
        }

        VehicleBorrowing borrowing = borrowingRepository.findById(borrowingId).orElseThrow();

        transfer.setStatus(TransferStatus.CANCELLED);
        transfer.setResolvedAt(LocalDateTime.now());
        BorrowingTransfer savedTransfer = transferRepository.save(transfer);

        borrowing.setStatus(BorrowingStatus.ACTIVE);
        borrowingRepository.save(borrowing);

        log.info("Transfer {} cancelled by requester {}", transferId, fromBorrowerId);

        Optional<VehicleDetails> vehicleOpt = vehicleRepository.findById(borrowing.getVehicleId());
        vehicleOpt.ifPresent(vehicle ->
                notificationService.sendTransferCancelledNotification(borrowing, savedTransfer, vehicle));

        return GenericResponse.builder()
                .status(SUCCESS).code("200")
                .message("Transfer cancelled")
                .body(mapper.toTransferResponse(savedTransfer))
                .build();
    }

    @Override
    public GenericResponse getTransfersForBorrowing(Long borrowingId) {
        log.info("Fetching transfer history for borrowing {}", borrowingId);
        List<BorrowingTransfer> transfers = transferRepository
                .findByBorrowingIdAndIsDeletedFalseOrderByCreatedAtAsc(borrowingId);
        return GenericResponse.builder()
                .status(SUCCESS).code("200")
                .message("Transfer history retrieved successfully")
                .body(transfers.stream().map(mapper::toTransferResponse).toList())
                .build();
    }

    private List<MediaFile> resolveMediaFiles(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(mediaFileRepository.findAllById(ids));
    }

    private boolean isVehicleNotInGarage(Integer vehicleId) {
        try {
            List<VehicleServiceHistoryResponse> workOrders =
                    garageClient.getWorkOrdersByVehicleId(vehicleId).getBody();

            if (workOrders == null || workOrders.isEmpty()) {
                return true;
            }

            boolean hasActiveWorkOrder = workOrders.stream()
                    .anyMatch(wo -> wo.getStatus() != null &&
                            !TERMINAL_WORK_ORDER_STATUSES.contains(wo.getStatus()));

            return !hasActiveWorkOrder;
        } catch (FeignException e) {
            log.warn("Could not verify garage status for vehicle {}: {}", vehicleId, e.getMessage());
            return true;
        }
    }
}
