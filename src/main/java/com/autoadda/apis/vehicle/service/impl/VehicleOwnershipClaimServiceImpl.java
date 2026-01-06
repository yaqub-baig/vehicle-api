package com.autoadda.apis.vehicle.service.impl;

import com.autoadda.apis.vehicle.dao.VehicleDetailsRepository;
import com.autoadda.apis.vehicle.dao.VehicleOwnershipClaimRepository;
import com.autoadda.apis.vehicle.entity.VehicleDetails;
import com.autoadda.apis.vehicle.entity.VehicleOwnershipClaim;
import com.autoadda.apis.vehicle.enums.ClaimStatusEnum;
import com.autoadda.apis.vehicle.mapper.OwnershipClaimMapper;
import com.autoadda.apis.vehicle.request.ClaimActionRequest;
import com.autoadda.apis.vehicle.request.OwnershipClaimRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.VehicleOwnershipClaimService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleOwnershipClaimServiceImpl implements VehicleOwnershipClaimService {

    public static final String ERROR = "error";
    public static final String SUCCESS = "success";
    public static final String OWNERSHIP_CLAIM_NOT_FOUND = "Ownership claim not found";
    private final VehicleOwnershipClaimRepository claimRepository;
    private final VehicleDetailsRepository vehicleRepository;
    private final OwnershipClaimMapper claimMapper;
    private final OwnershipClaimNotificationService notificationService;

    @Override
    @Transactional
    public GenericResponse createOwnershipClaim(OwnershipClaimRequest request, Integer requesterId) {
        log.info("Creating ownership claim for chassis: {} by user: {}", request.getChassisNo(), requesterId);

        // 1. Validate chassis number exists
        Optional<VehicleDetails> vehicleOpt = vehicleRepository.findByChassisNo(request.getChassisNo());
        if (vehicleOpt.isEmpty()) {
            return GenericResponse.builder()
                .status(ERROR)
                .code("404")
                .message("Vehicle with chassis number " + request.getChassisNo() + " not found")
                .build();
        }

        VehicleDetails vehicle = vehicleOpt.get();

        // 2. Check if requester is already the owner
        if (vehicle.getCustomerId().equals(requesterId)) {
            return GenericResponse.builder()
                .status(ERROR)
                .code("400")
                .message("You already own this vehicle")
                .build();
        }

        // 3. Check if there's already a pending claim by this user
        boolean hasActiveClaim = claimRepository.hasActiveClaim(request.getChassisNo(), requesterId);
        if (hasActiveClaim) {
            return GenericResponse.builder()
                .status(ERROR)
                .code("400")
                .message("You already have a pending claim for this vehicle")
                .build();
        }

        // 4. Create the ownership claim
        VehicleOwnershipClaim claim = VehicleOwnershipClaim.builder()
            .chassisNo(request.getChassisNo())
            .vehicleId(vehicle.getId())
            .requesterId(requesterId)
            .currentOwnerId(vehicle.getCustomerId())
            .reason(request.getReason())
            .status(ClaimStatusEnum.PENDING)
            .requestDate(LocalDateTime.now())
            .isDeleted(false)
            .build();

        VehicleOwnershipClaim savedClaim = claimRepository.save(claim);

        log.info("Ownership claim created successfully with ID: {}", savedClaim.getId());

        notificationService.sendClaimCreatedNotifications(savedClaim, vehicle);

        return GenericResponse.builder()
            .status(SUCCESS)
            .code("201")
            .message("Ownership claim created successfully. Awaiting owner approval.")
            .body(claimMapper.toResponse(savedClaim))
            .build();
    }

    @Override
    @Transactional
    public GenericResponse approveOwnershipClaim(Long claimId, ClaimActionRequest request, Integer ownerId) {
        log.info("Approving ownership claim ID: {} by owner: {}", claimId, ownerId);

        // 1. Find the claim
        Optional<VehicleOwnershipClaim> claimOpt = claimRepository.findById(claimId);
        if (claimOpt.isEmpty()) {
            return GenericResponse.builder()
                .status(ERROR)
                .code("404")
                .message(OWNERSHIP_CLAIM_NOT_FOUND)
                .build();
        }

        VehicleOwnershipClaim claim = claimOpt.get();

        // 2. Verify the current user is the owner
        if (!claim.getCurrentOwnerId().equals(ownerId)) {
            return GenericResponse.builder()
                .status(ERROR)
                .code("403")
                .message("You are not authorized to approve this claim")
                .build();
        }

        // 3. Verify claim is still pending
        if (claim.getStatus() != ClaimStatusEnum.PENDING) {
            return GenericResponse.builder()
                .status(ERROR)
                .code("400")
                .message("Claim has already been " + claim.getStatus().toString().toLowerCase())
                .build();
        }

        // 4. Find the vehicle
        Optional<VehicleDetails> vehicleOpt = vehicleRepository.findById(claim.getVehicleId());
        if (vehicleOpt.isEmpty()) {
            return GenericResponse.builder()
                .status(ERROR)
                .code("404")
                .message("Vehicle not found")
                .build();
        }

        VehicleDetails vehicle = vehicleOpt.get();

        // 5. Transfer ownership
        vehicle.setPreviousOwnerId(vehicle.getCustomerId()); // Store current owner as previous
        vehicle.setCustomerId(claim.getRequesterId()); // Transfer to requester
        vehicle.setUpdatedAt(LocalDateTime.now());
        vehicleRepository.save(vehicle);

        // 6. Update claim status
        claim.setStatus(ClaimStatusEnum.APPROVED);
        claim.setResponseDate(LocalDateTime.now());
        claim.setResponseNotes(request.getNotes());
        claim.setUpdatedAt(LocalDateTime.now());
        VehicleOwnershipClaim savedClaim = claimRepository.save(claim);

        log.info("Ownership claim approved. Vehicle {} transferred from {} to {}",
            vehicle.getId(), ownerId, claim.getRequesterId());

        notificationService.sendClaimApprovedNotifications(savedClaim, vehicle);

        return GenericResponse.builder()
            .status(SUCCESS)
            .code("200")
            .message("Ownership claim approved. Vehicle ownership transferred successfully.")
            .body(claimMapper.toResponse(savedClaim))
            .build();
    }

    @Override
    @Transactional
    public GenericResponse rejectOwnershipClaim(Long claimId, ClaimActionRequest request, Integer ownerId) {
        log.info("Rejecting ownership claim ID: {} by owner: {}", claimId, ownerId);

        // 1. Find the claim
        Optional<VehicleOwnershipClaim> claimOpt = claimRepository.findById(claimId);
        if (claimOpt.isEmpty()) {
            return GenericResponse.builder()
                .status(ERROR)
                .code("404")
                .message(OWNERSHIP_CLAIM_NOT_FOUND)
                .build();
        }

        VehicleOwnershipClaim claim = claimOpt.get();

        // 2. Verify the current user is the owner
        if (!claim.getCurrentOwnerId().equals(ownerId)) {
            return GenericResponse.builder()
                .status(ERROR)
                .code("403")
                .message("You are not authorized to reject this claim")
                .build();
        }

        // 3. Verify claim is still pending
        if (claim.getStatus() != ClaimStatusEnum.PENDING) {
            return GenericResponse.builder()
                .status(ERROR)
                .code("400")
                .message("Claim has already been " + claim.getStatus().toString().toLowerCase())
                .build();
        }

        // 4. Update claim status
        claim.setStatus(ClaimStatusEnum.REJECTED);
        claim.setResponseDate(LocalDateTime.now());
        claim.setResponseNotes(request.getNotes());
        claim.setUpdatedAt(LocalDateTime.now());
        VehicleOwnershipClaim savedClaim = claimRepository.save(claim);

        log.info("Ownership claim rejected by owner: {}", ownerId);

        Optional<VehicleDetails> vehicleOpt = vehicleRepository.findById(claim.getVehicleId());
        vehicleOpt.ifPresent(vehicle -> notificationService.sendClaimRejectedNotifications(savedClaim, vehicle));

        return GenericResponse.builder()
            .status(SUCCESS)
            .code("200")
            .message("Ownership claim rejected")
            .body(claimMapper.toResponse(savedClaim))
            .build();
    }

    @Override
    @Transactional
    public GenericResponse cancelOwnershipClaim(Long claimId, Integer requesterId) {
        log.info("Cancelling ownership claim ID: {} by requester: {}", claimId, requesterId);

        Optional<VehicleOwnershipClaim> claimOpt = claimRepository.findById(claimId);
        if (claimOpt.isEmpty()) {
            return GenericResponse.builder()
                .status(ERROR)
                .code("404")
                .message(OWNERSHIP_CLAIM_NOT_FOUND)
                .build();
        }

        VehicleOwnershipClaim claim = claimOpt.get();

        // Verify the current user is the requester
        if (!claim.getRequesterId().equals(requesterId)) {
            return GenericResponse.builder()
                .status(ERROR)
                .code("403")
                .message("You are not authorized to cancel this claim")
                .build();
        }

        // Verify claim is still pending
        if (claim.getStatus() != ClaimStatusEnum.PENDING) {
            return GenericResponse.builder()
                .status(ERROR)
                .code("400")
                .message("Only pending claims can be cancelled")
                .build();
        }

        claim.setStatus(ClaimStatusEnum.CANCELLED);
        claim.setResponseDate(LocalDateTime.now());
        claim.setUpdatedAt(LocalDateTime.now());
        VehicleOwnershipClaim savedClaim = claimRepository.save(claim);

        Optional<VehicleDetails> vehicleOpt = vehicleRepository.findById(claim.getVehicleId());
        vehicleOpt.ifPresent(vehicle -> notificationService.sendClaimCancelledNotifications(savedClaim, vehicle));

        return GenericResponse.builder()
            .status(SUCCESS)
            .code("200")
            .message("Ownership claim cancelled successfully")
            .build();
    }

    @Override
    public GenericResponse getClaimsByRequester(Integer requesterId) {
        log.info("Fetching claims for requester: {}", requesterId);

        List<VehicleOwnershipClaim> claims = claimRepository.findByRequesterId(requesterId);

        return GenericResponse.builder()
            .status(SUCCESS)
            .code("200")
            .message("Claims retrieved successfully")
            .body(claimMapper.toResponseList(claims))
            .build();
    }

    @Override
    public GenericResponse getPendingClaimsForOwner(Integer ownerId) {
        log.info("Fetching pending claims for owner: {}", ownerId);

        List<VehicleOwnershipClaim> pendingClaims = claimRepository.findPendingClaimsForOwner(ownerId);

        return GenericResponse.builder()
            .status(SUCCESS)
            .code("200")
            .message("Pending claims retrieved successfully")
            .body(claimMapper.toResponseList(pendingClaims))
            .build();
    }

    @Override
    public GenericResponse getClaimById(Long claimId) {
        log.info("Fetching claim by ID: {}", claimId);

        Optional<VehicleOwnershipClaim> claimOpt = claimRepository.findById(claimId);
        if (claimOpt.isEmpty()) {
            return GenericResponse.builder()
                .status(ERROR)
                .code("404")
                .message(OWNERSHIP_CLAIM_NOT_FOUND)
                .build();
        }

        return GenericResponse.builder()
            .status(SUCCESS)
            .code("200")
            .message("Claim retrieved successfully")
            .body(claimMapper.toResponse(claimOpt.get()))
            .build();
    }

    @Override
    public GenericResponse getClaimHistoryByVehicle(Integer vehicleId) {
        log.info("Fetching claim history for vehicle: {}", vehicleId);

        List<VehicleOwnershipClaim> claims = claimRepository.findClaimHistoryByVehicleId(vehicleId);

        return GenericResponse.builder()
            .status(SUCCESS)
            .code("200")
            .message("Claim history retrieved successfully")
            .body(claimMapper.toResponseList(claims))
            .build();
    }
}
