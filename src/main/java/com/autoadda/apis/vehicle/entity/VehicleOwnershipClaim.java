package com.autoadda.apis.vehicle.entity;

import com.autoadda.apis.vehicle.enums.ClaimStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
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
