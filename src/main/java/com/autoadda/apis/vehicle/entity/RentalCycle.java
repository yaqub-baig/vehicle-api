package com.autoadda.apis.vehicle.entity;

import com.autoadda.apis.vehicle.enums.RentalCycleStatus;
import com.autoadda.apis.vehicle.enums.RentalRateType;
import com.autoadda.apis.vehicle.enums.RentalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rental_cycle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rental_request_id", nullable = false)
    private Long rentalRequestId;

    // Denormalized from the accepted request, so queries/authorization don't need a join.
    @Column(name = "listing_id", nullable = false)
    private Long listingId;

    @Column(name = "vehicle_id", nullable = false)
    private Integer vehicleId;

    @Column(name = "renter_id", nullable = false)
    private Integer renterId;

    @Column(name = "owner_id", nullable = false)
    private Integer ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "rate_type", nullable = false)
    private RentalRateType rateType;

    // Snapshotted from the accepted RentalRequest at cycle-creation time, same rationale as the
    // rate-card snapshots below. UNLIMITED cycles have rentalType = UNLIMITED, durationValue = null.
    @Enumerated(EnumType.STRING)
    @Column(name = "rental_type")
    private RentalType rentalType;

    @Column(name = "duration_value")
    private Integer durationValue;

    // Snapshots of the listing's rate-card values taken when the cycle is created, so an
    // owner editing the listing later can't retroactively change an in-progress/completed rental.
    @Column(name = "rate_at_start", nullable = false, precision = 12, scale = 2)
    private BigDecimal rateAtStart;

    @Column(name = "km_limit_per_day_at_start", nullable = false)
    private Integer kmLimitPerDayAtStart;

    @Column(name = "excess_km_penalty_rate_at_start", nullable = false, precision = 12, scale = 2)
    private BigDecimal excessKmPenaltyRateAtStart;

    @Column(name = "late_return_penalty_rate_at_start", nullable = false, precision = 12, scale = 2)
    private BigDecimal lateReturnPenaltyRateAtStart;

    // Flat currency amount negotiated off-platform; owner-settable any time before return.
    @Column(name = "discount_amount", precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "start_odometer")
    private Integer startOdometer;

    @Column(name = "end_odometer")
    private Integer endOdometer;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    // Renter-declared at start time; optional. If set, a late-return penalty applies when
    // actualReturnAt exceeds it. If null, no late-return penalty is ever charged for this cycle.
    @Column(name = "expected_return_at")
    private LocalDateTime expectedReturnAt;

    @Column(name = "actual_return_at")
    private LocalDateTime actualReturnAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RentalCycleStatus status;

    @Column(name = "base_price", precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "excess_km_penalty", precision = 12, scale = 2)
    private BigDecimal excessKmPenalty;

    @Column(name = "late_return_penalty", precision = 12, scale = 2)
    private BigDecimal lateReturnPenalty;

    @Column(name = "final_price", precision = 12, scale = 2)
    private BigDecimal finalPrice;

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
            status = RentalCycleStatus.AWAITING_START;
        }
        if (discountAmount == null) {
            discountAmount = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
