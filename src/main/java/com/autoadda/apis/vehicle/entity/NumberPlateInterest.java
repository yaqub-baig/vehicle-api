package com.autoadda.apis.vehicle.entity;

import com.autoadda.apis.vehicle.enums.NumberPlateInterestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "number_plate_interest")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NumberPlateInterest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "listing_id", nullable = false)
    private Long listingId;

    @Column(name = "buyer_user_id", nullable = false)
    private Integer buyerUserId;

    // Denormalized copy of the listing owner at the time interest was expressed, so
    // queries/authorization don't need a join back to the listing.
    @Column(name = "seller_user_id", nullable = false)
    private Integer sellerUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NumberPlateInterestStatus status;

    @Column(length = 1000)
    private String message;

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
            status = NumberPlateInterestStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
