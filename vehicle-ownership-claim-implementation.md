# Vehicle Ownership Claim Mechanism - Implementation Guide

## Overview

This document provides a comprehensive implementation guide for the Vehicle Ownership Claim mechanism in the AutoAdda Vehicle API. This feature allows users to request ownership of vehicles by chassis number, enabling scenarios such as vehicle sales, transfers, and ownership disputes.

### Use Cases
- **Vehicle Sale**: When a vehicle is sold, the buyer can request ownership transfer
- **Ownership Transfer**: Transfer ownership between family members, companies, or other parties
- **Lost Ownership**: Request ownership if a vehicle was previously owned but access was lost
- **Multiple Claims**: Handle scenarios where chassis number is already registered by another user

---

## Feature Requirements

Based on user requirements:
- **Approval Flow**: Manual approval by current owner
- **Claim Data**: Basic request with chassis number and reason
- **Claim Type**: Single generic claim type for all scenarios
- **Data Handling**: Keep all vehicle history when ownership is transferred

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    Ownership Claim Flow                      │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  1. User A requests ownership of chassis #ABC123            │
│     └─> System validates chassis exists                     │
│     └─> System identifies current owner (User B)            │
│     └─> Creates PENDING claim                               │
│                                                              │
│  2. User B (current owner) receives notification            │
│     └─> Views pending claims for their vehicles             │
│     └─> Reviews claim details and reason                    │
│                                                              │
│  3. User B approves/rejects the claim                       │
│     └─> If APPROVED: Transfer ownership, update vehicle     │
│     └─> If REJECTED: Update claim status, notify requester  │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## Database Schema

### 1. Entity: VehicleOwnershipClaim

**File**: `src/main/java/com/autoadda/apis/vehicle/entity/VehicleOwnershipClaim.java`

```java
package com.autoadda.apis.vehicle.entity;

import com.autoadda.apis.vehicle.enums.ClaimStatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_ownership_claim")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleOwnershipClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // Vehicle Identification
    @Column(name = "chassis_no", nullable = false)
    private String chassisNo;

    @Column(name = "vehicle_id")
    private Integer vehicleId; // Reference to VehicleDetails.id

    // Ownership Details
    @Column(name = "requester_id", nullable = false)
    private Integer requesterId; // User requesting ownership

    @Column(name = "current_owner_id", nullable = false)
    private Integer currentOwnerId; // Current owner at time of request

    // Claim Information
    @Column(name = "reason", length = 1000)
    private String reason; // Why ownership is being requested

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ClaimStatusEnum status; // PENDING, APPROVED, REJECTED, CANCELLED

    // Action Details
    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestDate;

    @Column(name = "response_date")
    private LocalDateTime responseDate; // When owner approved/rejected

    @Column(name = "response_notes", length = 1000)
    private String responseNotes; // Owner's notes when approving/rejecting

    // Audit Fields
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted")
    private boolean isDeleted; // Soft delete

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        requestDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

### 2. Enum: ClaimStatusEnum

**File**: `src/main/java/com/autoadda/apis/vehicle/enums/ClaimStatusEnum.java`

```java
package com.autoadda.apis.vehicle.enums;

public enum ClaimStatusEnum {
    PENDING,    // Awaiting owner approval
    APPROVED,   // Owner approved, ownership transferred
    REJECTED,   // Owner rejected the claim
    CANCELLED   // Requester cancelled the claim
}
```

### 3. Update VehicleDetails Entity

**File**: `src/main/java/com/autoadda/apis/vehicle/entity/VehicleDetails.java`

Add this field to track ownership history:

```java
@Column(name = "previous_owner_id")
private Integer previousOwnerId; // Stores previous owner after transfer
```

---

## Repository Layer

### VehicleOwnershipClaimRepository

**File**: `src/main/java/com/autoadda/apis/vehicle/dao/VehicleOwnershipClaimRepository.java`

```java
package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.VehicleOwnershipClaim;
import com.autoadda.apis.vehicle.enums.ClaimStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleOwnershipClaimRepository extends JpaRepository<VehicleOwnershipClaim, Long> {

    // Find claims by chassis number
    List<VehicleOwnershipClaim> findByChassisNo(String chassisNo);

    // Find claims by requester
    List<VehicleOwnershipClaim> findByRequesterId(Integer requesterId);

    // Find claims by current owner
    List<VehicleOwnershipClaim> findByCurrentOwnerId(Integer currentOwnerId);

    // Find pending claims for a specific owner
    List<VehicleOwnershipClaim> findByCurrentOwnerIdAndStatus(Integer currentOwnerId, ClaimStatusEnum status);

    // Find pending claims by chassis number
    Optional<VehicleOwnershipClaim> findByChassisNoAndStatus(String chassisNo, ClaimStatusEnum status);

    // Check if requester has pending claim for chassis
    @Query("SELECT COUNT(c) > 0 FROM VehicleOwnershipClaim c WHERE c.chassisNo = :chassisNo AND c.requesterId = :requesterId AND c.status = 'PENDING'")
    boolean hasActiveClaim(@Param("chassisNo") String chassisNo, @Param("requesterId") Integer requesterId);

    // Find all pending claims for vehicles owned by user
    @Query("SELECT c FROM VehicleOwnershipClaim c WHERE c.currentOwnerId = :ownerId AND c.status = 'PENDING' AND c.isDeleted = false ORDER BY c.requestDate DESC")
    List<VehicleOwnershipClaim> findPendingClaimsForOwner(@Param("ownerId") Integer ownerId);

    // Find claim history for a vehicle
    @Query("SELECT c FROM VehicleOwnershipClaim c WHERE c.vehicleId = :vehicleId ORDER BY c.requestDate DESC")
    List<VehicleOwnershipClaim> findClaimHistoryByVehicleId(@Param("vehicleId") Integer vehicleId);
}
```

---

## Service Layer

### 1. Interface: VehicleOwnershipClaimService

**File**: `src/main/java/com/autoadda/apis/vehicle/service/VehicleOwnershipClaimService.java`

```java
package com.autoadda.apis.vehicle.service;

import com.autoadda.apis.vehicle.request.ClaimActionRequest;
import com.autoadda.apis.vehicle.request.OwnershipClaimRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;

public interface VehicleOwnershipClaimService {

    /**
     * Create a new ownership claim for a vehicle
     */
    GenericResponse createOwnershipClaim(OwnershipClaimRequest request, Integer requesterId);

    /**
     * Approve an ownership claim (transfers ownership)
     */
    GenericResponse approveOwnershipClaim(Long claimId, ClaimActionRequest request, Integer ownerId);

    /**
     * Reject an ownership claim
     */
    GenericResponse rejectOwnershipClaim(Long claimId, ClaimActionRequest request, Integer ownerId);

    /**
     * Cancel a claim by requester
     */
    GenericResponse cancelOwnershipClaim(Long claimId, Integer requesterId);

    /**
     * Get all claims made by a requester
     */
    GenericResponse getClaimsByRequester(Integer requesterId);

    /**
     * Get all pending claims for vehicles owned by user
     */
    GenericResponse getPendingClaimsForOwner(Integer ownerId);

    /**
     * Get claim details by ID
     */
    GenericResponse getClaimById(Long claimId);

    /**
     * Get claim history for a specific vehicle
     */
    GenericResponse getClaimHistoryByVehicle(Integer vehicleId);
}
```

### 2. Implementation: VehicleOwnershipClaimServiceImpl

**File**: `src/main/java/com/autoadda/apis/vehicle/service/impl/VehicleOwnershipClaimServiceImpl.java`

```java
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

    private final VehicleOwnershipClaimRepository claimRepository;
    private final VehicleDetailsRepository vehicleRepository;
    private final OwnershipClaimMapper claimMapper;

    @Override
    @Transactional
    public GenericResponse createOwnershipClaim(OwnershipClaimRequest request, Integer requesterId) {
        log.info("Creating ownership claim for chassis: {} by user: {}", request.getChassisNo(), requesterId);

        // 1. Validate chassis number exists
        Optional<VehicleDetails> vehicleOpt = vehicleRepository.findByChassisNo(request.getChassisNo());
        if (vehicleOpt.isEmpty()) {
            return GenericResponse.builder()
                .status("error")
                .code("404")
                .message("Vehicle with chassis number " + request.getChassisNo() + " not found")
                .build();
        }

        VehicleDetails vehicle = vehicleOpt.get();

        // 2. Check if requester is already the owner
        if (vehicle.getCustomerId().equals(requesterId)) {
            return GenericResponse.builder()
                .status("error")
                .code("400")
                .message("You already own this vehicle")
                .build();
        }

        // 3. Check if there's already a pending claim by this user
        boolean hasActiveClaim = claimRepository.hasActiveClaim(request.getChassisNo(), requesterId);
        if (hasActiveClaim) {
            return GenericResponse.builder()
                .status("error")
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

        return GenericResponse.builder()
            .status("success")
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
                .status("error")
                .code("404")
                .message("Ownership claim not found")
                .build();
        }

        VehicleOwnershipClaim claim = claimOpt.get();

        // 2. Verify the current user is the owner
        if (!claim.getCurrentOwnerId().equals(ownerId)) {
            return GenericResponse.builder()
                .status("error")
                .code("403")
                .message("You are not authorized to approve this claim")
                .build();
        }

        // 3. Verify claim is still pending
        if (claim.getStatus() != ClaimStatusEnum.PENDING) {
            return GenericResponse.builder()
                .status("error")
                .code("400")
                .message("Claim has already been " + claim.getStatus().toString().toLowerCase())
                .build();
        }

        // 4. Find the vehicle
        Optional<VehicleDetails> vehicleOpt = vehicleRepository.findById(claim.getVehicleId());
        if (vehicleOpt.isEmpty()) {
            return GenericResponse.builder()
                .status("error")
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
        claimRepository.save(claim);

        log.info("Ownership claim approved. Vehicle {} transferred from {} to {}",
            vehicle.getId(), ownerId, claim.getRequesterId());

        return GenericResponse.builder()
            .status("success")
            .code("200")
            .message("Ownership claim approved. Vehicle ownership transferred successfully.")
            .body(claimMapper.toResponse(claim))
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
                .status("error")
                .code("404")
                .message("Ownership claim not found")
                .build();
        }

        VehicleOwnershipClaim claim = claimOpt.get();

        // 2. Verify the current user is the owner
        if (!claim.getCurrentOwnerId().equals(ownerId)) {
            return GenericResponse.builder()
                .status("error")
                .code("403")
                .message("You are not authorized to reject this claim")
                .build();
        }

        // 3. Verify claim is still pending
        if (claim.getStatus() != ClaimStatusEnum.PENDING) {
            return GenericResponse.builder()
                .status("error")
                .code("400")
                .message("Claim has already been " + claim.getStatus().toString().toLowerCase())
                .build();
        }

        // 4. Update claim status
        claim.setStatus(ClaimStatusEnum.REJECTED);
        claim.setResponseDate(LocalDateTime.now());
        claim.setResponseNotes(request.getNotes());
        claim.setUpdatedAt(LocalDateTime.now());
        claimRepository.save(claim);

        log.info("Ownership claim rejected by owner: {}", ownerId);

        return GenericResponse.builder()
            .status("success")
            .code("200")
            .message("Ownership claim rejected")
            .body(claimMapper.toResponse(claim))
            .build();
    }

    @Override
    @Transactional
    public GenericResponse cancelOwnershipClaim(Long claimId, Integer requesterId) {
        log.info("Cancelling ownership claim ID: {} by requester: {}", claimId, requesterId);

        Optional<VehicleOwnershipClaim> claimOpt = claimRepository.findById(claimId);
        if (claimOpt.isEmpty()) {
            return GenericResponse.builder()
                .status("error")
                .code("404")
                .message("Ownership claim not found")
                .build();
        }

        VehicleOwnershipClaim claim = claimOpt.get();

        // Verify the current user is the requester
        if (!claim.getRequesterId().equals(requesterId)) {
            return GenericResponse.builder()
                .status("error")
                .code("403")
                .message("You are not authorized to cancel this claim")
                .build();
        }

        // Verify claim is still pending
        if (claim.getStatus() != ClaimStatusEnum.PENDING) {
            return GenericResponse.builder()
                .status("error")
                .code("400")
                .message("Only pending claims can be cancelled")
                .build();
        }

        claim.setStatus(ClaimStatusEnum.CANCELLED);
        claim.setResponseDate(LocalDateTime.now());
        claim.setUpdatedAt(LocalDateTime.now());
        claimRepository.save(claim);

        return GenericResponse.builder()
            .status("success")
            .code("200")
            .message("Ownership claim cancelled successfully")
            .build();
    }

    @Override
    public GenericResponse getClaimsByRequester(Integer requesterId) {
        log.info("Fetching claims for requester: {}", requesterId);

        List<VehicleOwnershipClaim> claims = claimRepository.findByRequesterId(requesterId);

        return GenericResponse.builder()
            .status("success")
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
            .status("success")
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
                .status("error")
                .code("404")
                .message("Ownership claim not found")
                .build();
        }

        return GenericResponse.builder()
            .status("success")
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
            .status("success")
            .code("200")
            .message("Claim history retrieved successfully")
            .body(claimMapper.toResponseList(claims))
            .build();
    }
}
```

---

## Controller Layer

### VehicleOwnershipClaimController

**File**: `src/main/java/com/autoadda/apis/vehicle/controller/VehicleOwnershipClaimController.java`

```java
package com.autoadda.apis.vehicle.controller;

import com.autoadda.apis.vehicle.request.ClaimActionRequest;
import com.autoadda.apis.vehicle.request.OwnershipClaimRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.VehicleOwnershipClaimService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/ownership-claims")
@RequiredArgsConstructor
public class VehicleOwnershipClaimController {

    private final VehicleOwnershipClaimService claimService;

    /**
     * Request ownership of a vehicle by chassis number
     * POST /ownership-claims/request
     */
    @PostMapping("/request")
    public GenericResponse requestOwnership(
        @RequestBody OwnershipClaimRequest request,
        @RequestHeader(value = "X-Auto-Adda-Id") String userId) {

        Integer requesterId = Integer.parseInt(userId);
        return claimService.createOwnershipClaim(request, requesterId);
    }

    /**
     * Get all ownership claims made by the current user
     * GET /ownership-claims/my-requests
     */
    @GetMapping("/my-requests")
    public GenericResponse getMyRequests(
        @RequestHeader(value = "X-Auto-Adda-Id") String userId) {

        Integer requesterId = Integer.parseInt(userId);
        return claimService.getClaimsByRequester(requesterId);
    }

    /**
     * Get all pending ownership claims for vehicles owned by current user
     * GET /ownership-claims/pending
     */
    @GetMapping("/pending")
    public GenericResponse getPendingClaims(
        @RequestHeader(value = "X-Auto-Adda-Id") String userId) {

        Integer ownerId = Integer.parseInt(userId);
        return claimService.getPendingClaimsForOwner(ownerId);
    }

    /**
     * Get ownership claim details by ID
     * GET /ownership-claims/{claimId}
     */
    @GetMapping("/{claimId}")
    public GenericResponse getClaimById(@PathVariable Long claimId) {
        return claimService.getClaimById(claimId);
    }

    /**
     * Approve an ownership claim (current owner only)
     * PUT /ownership-claims/{claimId}/approve
     */
    @PutMapping("/{claimId}/approve")
    public GenericResponse approveClaim(
        @PathVariable Long claimId,
        @RequestBody(required = false) ClaimActionRequest request,
        @RequestHeader(value = "X-Auto-Adda-Id") String userId) {

        Integer ownerId = Integer.parseInt(userId);
        ClaimActionRequest actionRequest = request != null ? request : new ClaimActionRequest();
        return claimService.approveOwnershipClaim(claimId, actionRequest, ownerId);
    }

    /**
     * Reject an ownership claim (current owner only)
     * PUT /ownership-claims/{claimId}/reject
     */
    @PutMapping("/{claimId}/reject")
    public GenericResponse rejectClaim(
        @PathVariable Long claimId,
        @RequestBody ClaimActionRequest request,
        @RequestHeader(value = "X-Auto-Adda-Id") String userId) {

        Integer ownerId = Integer.parseInt(userId);
        return claimService.rejectOwnershipClaim(claimId, request, ownerId);
    }

    /**
     * Cancel an ownership claim (requester only)
     * PUT /ownership-claims/{claimId}/cancel
     */
    @PutMapping("/{claimId}/cancel")
    public GenericResponse cancelClaim(
        @PathVariable Long claimId,
        @RequestHeader(value = "X-Auto-Adda-Id") String userId) {

        Integer requesterId = Integer.parseInt(userId);
        return claimService.cancelOwnershipClaim(claimId, requesterId);
    }

    /**
     * Get ownership claim history for a vehicle
     * GET /ownership-claims/vehicle/{vehicleId}/history
     */
    @GetMapping("/vehicle/{vehicleId}/history")
    public GenericResponse getVehicleClaimHistory(@PathVariable Integer vehicleId) {
        return claimService.getClaimHistoryByVehicle(vehicleId);
    }
}
```

---

## Request/Response DTOs

### 1. OwnershipClaimRequest

**File**: `src/main/java/com/autoadda/apis/vehicle/request/OwnershipClaimRequest.java`

```java
package com.autoadda.apis.vehicle.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnershipClaimRequest {

    @NotBlank(message = "Chassis number is required")
    private String chassisNo;

    private String reason; // Why ownership is being requested
}
```

### 2. ClaimActionRequest

**File**: `src/main/java/com/autoadda/apis/vehicle/request/ClaimActionRequest.java`

```java
package com.autoadda.apis.vehicle.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimActionRequest {

    private String notes; // Owner's notes when approving/rejecting
}
```

### 3. OwnershipClaimResponse

**File**: `src/main/java/com/autoadda/apis/vehicle/response/OwnershipClaimResponse.java`

```java
package com.autoadda.apis.vehicle.response;

import com.autoadda.apis.vehicle.enums.ClaimStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnershipClaimResponse {

    private Long id;
    private String chassisNo;
    private Integer vehicleId;

    // User Information (can be populated from Identity Service)
    private Integer requesterId;
    private String requesterName; // Fetched from User service

    private Integer currentOwnerId;
    private String currentOwnerName; // Fetched from User service

    // Claim Details
    private String reason;
    private ClaimStatusEnum status;

    // Vehicle Basic Info (optional, can be populated)
    private String vehicleBrand;
    private String vehicleModel;
    private String vehicleYear;

    // Timestamps
    private LocalDateTime requestDate;
    private LocalDateTime responseDate;
    private String responseNotes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

---

## Mapper

### OwnershipClaimMapper

**File**: `src/main/java/com/autoadda/apis/vehicle/mapper/OwnershipClaimMapper.java`

```java
package com.autoadda.apis.vehicle.mapper;

import com.autoadda.apis.vehicle.entity.VehicleOwnershipClaim;
import com.autoadda.apis.vehicle.response.OwnershipClaimResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OwnershipClaimMapper {

    public OwnershipClaimResponse toResponse(VehicleOwnershipClaim claim) {
        if (claim == null) {
            return null;
        }

        return OwnershipClaimResponse.builder()
            .id(claim.getId())
            .chassisNo(claim.getChassisNo())
            .vehicleId(claim.getVehicleId())
            .requesterId(claim.getRequesterId())
            .currentOwnerId(claim.getCurrentOwnerId())
            .reason(claim.getReason())
            .status(claim.getStatus())
            .requestDate(claim.getRequestDate())
            .responseDate(claim.getResponseDate())
            .responseNotes(claim.getResponseNotes())
            .createdAt(claim.getCreatedAt())
            .updatedAt(claim.getUpdatedAt())
            .build();

        // Note: requesterName, currentOwnerName, and vehicle details
        // can be populated by calling Identity Service if needed
    }

    public List<OwnershipClaimResponse> toResponseList(List<VehicleOwnershipClaim> claims) {
        if (claims == null) {
            return List.of();
        }

        return claims.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
}
```

---

## API Endpoints Reference

### Ownership Claim Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/ownership-claims/request` | Request ownership of a vehicle | Yes (X-Auto-Adda-Id) |
| GET | `/ownership-claims/my-requests` | Get all claims made by user | Yes (X-Auto-Adda-Id) |
| GET | `/ownership-claims/pending` | Get pending claims for user's vehicles | Yes (X-Auto-Adda-Id) |
| GET | `/ownership-claims/{claimId}` | Get claim details by ID | No |
| PUT | `/ownership-claims/{claimId}/approve` | Approve ownership claim | Yes (X-Auto-Adda-Id) |
| PUT | `/ownership-claims/{claimId}/reject` | Reject ownership claim | Yes (X-Auto-Adda-Id) |
| PUT | `/ownership-claims/{claimId}/cancel` | Cancel ownership claim | Yes (X-Auto-Adda-Id) |
| GET | `/ownership-claims/vehicle/{vehicleId}/history` | Get claim history for vehicle | No |

---

## Business Logic Flow

### 1. Request Ownership Flow

```
User A wants to claim vehicle with chassis #ABC123
    ↓
POST /ownership-claims/request
    ↓
Validate chassis exists in database
    ↓
Check if User A is already owner → If YES: Return error
    ↓
Check if User A has pending claim → If YES: Return error
    ↓
Get current owner (User B) from vehicle.customerId
    ↓
Create VehicleOwnershipClaim with status=PENDING
    ↓
Return success response
    ↓
[Notification sent to User B - implemented separately]
```

### 2. Approve Ownership Flow

```
User B (current owner) approves claim
    ↓
PUT /ownership-claims/{claimId}/approve
    ↓
Verify User B is the current owner → If NO: Return 403 error
    ↓
Verify claim status is PENDING → If NO: Return error
    ↓
Transfer ownership:
    - Set vehicle.previousOwnerId = current customerId
    - Set vehicle.customerId = requesterId
    ↓
Update claim:
    - Set status = APPROVED
    - Set responseDate = now
    - Set responseNotes
    ↓
Save both vehicle and claim
    ↓
Return success response
    ↓
[Notification sent to User A - implemented separately]
```

### 3. Reject Ownership Flow

```
User B (current owner) rejects claim
    ↓
PUT /ownership-claims/{claimId}/reject
    ↓
Verify User B is the current owner → If NO: Return 403 error
    ↓
Verify claim status is PENDING → If NO: Return error
    ↓
Update claim:
    - Set status = REJECTED
    - Set responseDate = now
    - Set responseNotes (reason for rejection)
    ↓
Save claim (vehicle ownership remains unchanged)
    ↓
Return success response
    ↓
[Notification sent to User A - implemented separately]
```

---

## Database Migration

### SQL Script for Table Creation

**File**: `src/main/resources/db/migration/V{version}__create_vehicle_ownership_claim_table.sql`

```sql
-- Create vehicle_ownership_claim table
CREATE TABLE IF NOT EXISTS vehicle_ownership_claim (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chassis_no VARCHAR(255) NOT NULL,
    vehicle_id INT,
    requester_id INT NOT NULL,
    current_owner_id INT NOT NULL,
    reason VARCHAR(1000),
    status VARCHAR(50) NOT NULL,
    request_date TIMESTAMP NOT NULL,
    response_date TIMESTAMP,
    response_notes VARCHAR(1000),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,

    INDEX idx_chassis_no (chassis_no),
    INDEX idx_requester_id (requester_id),
    INDEX idx_current_owner_id (current_owner_id),
    INDEX idx_status (status),
    INDEX idx_vehicle_id (vehicle_id)
);

-- Add previous_owner_id column to vehicle_details table
ALTER TABLE vehicle_details
ADD COLUMN previous_owner_id INT AFTER customer_id;
```

---

## Testing Guide

### 1. Unit Tests

**File**: `src/test/java/com/autoadda/apis/vehicle/service/VehicleOwnershipClaimServiceTest.java`

```java
@SpringBootTest
class VehicleOwnershipClaimServiceTest {

    @MockBean
    private VehicleOwnershipClaimRepository claimRepository;

    @MockBean
    private VehicleDetailsRepository vehicleRepository;

    @Autowired
    private VehicleOwnershipClaimService claimService;

    @Test
    void testCreateOwnershipClaim_Success() {
        // Test successful claim creation
    }

    @Test
    void testCreateOwnershipClaim_VehicleNotFound() {
        // Test error when chassis doesn't exist
    }

    @Test
    void testCreateOwnershipClaim_AlreadyOwner() {
        // Test error when requester is already owner
    }

    @Test
    void testApproveOwnershipClaim_Success() {
        // Test successful approval and ownership transfer
    }

    @Test
    void testApproveOwnershipClaim_Unauthorized() {
        // Test error when non-owner tries to approve
    }

    @Test
    void testRejectOwnershipClaim_Success() {
        // Test successful rejection
    }
}
```

### 2. Integration Tests (Postman Collection)

**Test Scenario 1: Complete Ownership Transfer**
```
1. POST /vehicles/add (User B creates vehicle with chassis ABC123)
2. POST /ownership-claims/request (User A requests ownership)
   - Body: { "chassisNo": "ABC123", "reason": "Purchased vehicle" }
3. GET /ownership-claims/pending (User B checks pending claims)
4. PUT /ownership-claims/{id}/approve (User B approves)
   - Body: { "notes": "Sale confirmed" }
5. GET /vehicles (User A verifies vehicle now appears in their list)
```

**Test Scenario 2: Claim Rejection**
```
1. POST /ownership-claims/request (User A requests ownership)
2. PUT /ownership-claims/{id}/reject (User B rejects)
   - Body: { "notes": "Vehicle not for sale" }
3. GET /ownership-claims/my-requests (User A sees rejected claim)
```

**Test Scenario 3: Error Handling**
```
1. POST /ownership-claims/request with invalid chassis
   - Expected: 404 error
2. POST /ownership-claims/request for own vehicle
   - Expected: 400 error "Already own this vehicle"
3. POST /ownership-claims/request twice for same vehicle
   - Expected: 400 error "Pending claim exists"
```

---

## Implementation Checklist

### Phase 1: Core Setup
- [ ] Create `ClaimStatusEnum` enum
- [ ] Create `VehicleOwnershipClaim` entity
- [ ] Add `previousOwnerId` field to `VehicleDetails` entity
- [ ] Create database migration script
- [ ] Run migration to create table

### Phase 2: Data Layer
- [ ] Create `VehicleOwnershipClaimRepository` interface
- [ ] Add all required query methods
- [ ] Test repository methods

### Phase 3: Service Layer
- [ ] Create `VehicleOwnershipClaimService` interface
- [ ] Implement `VehicleOwnershipClaimServiceImpl`
- [ ] Create `OwnershipClaimMapper` component
- [ ] Add logging and error handling
- [ ] Write unit tests for service layer

### Phase 4: API Layer
- [ ] Create DTOs: `OwnershipClaimRequest`, `ClaimActionRequest`, `OwnershipClaimResponse`
- [ ] Create `VehicleOwnershipClaimController`
- [ ] Add validation annotations
- [ ] Test all endpoints with Postman

### Phase 5: Integration & Testing
- [ ] Write integration tests
- [ ] Test complete ownership transfer flow
- [ ] Test error scenarios
- [ ] Test authorization and access control
- [ ] Performance testing for claim queries

### Phase 6: Optional Enhancements
- [ ] Add email/push notifications for claim events
- [ ] Add claim expiration (auto-reject after X days)
- [ ] Add claim comments/chat feature
- [ ] Add support for multiple pending claims per vehicle
- [ ] Add admin dashboard for claim management
- [ ] Add claim dispute resolution workflow

---

## Security Considerations

1. **Authorization**: Always verify user identity via `X-Auto-Adda-Id` header
2. **Ownership Verification**: Check current owner before approving/rejecting claims
3. **Status Validation**: Ensure claims can only be acted upon when in PENDING status
4. **Soft Delete**: Use `isDeleted` flag instead of hard deletion for audit trail
5. **Input Validation**: Validate chassis number format and existence
6. **Rate Limiting**: Consider rate limiting claim creation to prevent spam

---

## Future Enhancements

1. **Notification System**
   - Email notifications when claim is created, approved, or rejected
   - Push notifications for mobile apps
   - SMS notifications for critical actions

2. **Claim Expiration**
   - Auto-reject claims after configurable period (e.g., 30 days)
   - Send reminders to owners about pending claims

3. **Document Upload**
   - Allow requester to upload proof of purchase
   - Allow requester to upload sale agreement
   - Store documents with MediaFile entity

4. **Multi-Owner Support**
   - Support for co-ownership scenarios
   - Require approval from all owners

5. **Claim Analytics**
   - Track claim approval rates
   - Monitor average response time
   - Identify fraud patterns

6. **Admin Features**
   - Admin override for claim approval
   - Claim dispute resolution
   - Bulk claim management

7. **Integration with External Services**
   - Verify ownership via government databases
   - Integration with insurance providers
   - Integration with DMV/RTO systems

---

## Troubleshooting Guide

### Common Issues

**Issue 1: "Vehicle not found" error when chassis exists**
- **Cause**: Chassis number case sensitivity or whitespace
- **Solution**: Trim and normalize chassis numbers before comparison

**Issue 2: Claim approval succeeds but ownership not transferred**
- **Cause**: Transaction not committed or vehicle not saved
- **Solution**: Check `@Transactional` annotation and repository save calls

**Issue 3: Multiple pending claims for same vehicle**
- **Cause**: Race condition in hasActiveClaim check
- **Solution**: Add database unique constraint on (chassisNo, requesterId, status)

**Issue 4: Unauthorized errors when valid owner approves**
- **Cause**: Owner ID mismatch (Integer vs String parsing)
- **Solution**: Ensure consistent parsing of X-Auto-Adda-Id header

---

## Conclusion

This implementation guide provides a complete ownership claim mechanism that:
- ✅ Allows users to request ownership of vehicles by chassis number
- ✅ Enables current owners to approve or reject claims
- ✅ Transfers ownership while preserving all vehicle history
- ✅ Maintains a complete audit trail of all ownership changes
- ✅ Provides secure, role-based access control
- ✅ Supports multiple use cases (sales, transfers, disputes)

The system is extensible and can be enhanced with notifications, document uploads, and advanced features as needed.

---

**Document Version**: 1.0
**Last Updated**: 2025-11-27
**Author**: AutoAdda Development Team
