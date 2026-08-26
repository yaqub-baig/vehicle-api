package com.autoadda.apis.vehicle.entity;

import com.autoadda.apis.vehicle.enums.RentalRateType;
import com.autoadda.apis.vehicle.enums.RentalRequestStatus;
import com.autoadda.apis.vehicle.enums.RentalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rental_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "listing_id", nullable = false)
    private Long listingId;

    // Denormalized from the listing at request time.
    @Column(name = "vehicle_id", nullable = false)
    private Integer vehicleId;

    @Column(name = "renter_id", nullable = false)
    private Integer renterId;

    // Denormalized copy of the listing owner at the time the request was created, so
    // queries/authorization don't need a join back to the listing.
    @Column(name = "owner_id", nullable = false)
    private Integer ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "requested_rate_type", nullable = false)
    private RentalRateType requestedRateType;

    @Enumerated(EnumType.STRING)
    @Column(name = "rental_type")
    private RentalType rentalType;

    @Column(name = "duration_value")
    private Integer durationValue;

    @Column(length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RentalRequestStatus status;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isDeleted == null) {
            isDeleted = false;
        }
        if (status == null) {
            status = RentalRequestStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
