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
