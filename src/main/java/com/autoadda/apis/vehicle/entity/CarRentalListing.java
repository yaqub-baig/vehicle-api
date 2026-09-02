package com.autoadda.apis.vehicle.entity;

import com.autoadda.apis.vehicle.enums.RentalListingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "car_rental_listing")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarRentalListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private Integer vehicleId;

    // Denormalized copy of VehicleDetails.customerId at the time the listing was created, so
    // queries/authorization don't need a join back to the vehicle.
    @Column(name = "owner_id", nullable = false)
    private Integer ownerId;

    @Column(name = "price_per_hour", precision = 12, scale = 2)
    private BigDecimal pricePerHour;

    @Column(name = "price_per_day", nullable = false, precision = 12, scale = 2)
    private BigDecimal pricePerDay;

    @Column(name = "km_limit_per_day", nullable = false)
    private Integer kmLimitPerDay;

    @Column(name = "excess_km_penalty_rate", nullable = false, precision = 12, scale = 2)
    private BigDecimal excessKmPenaltyRate;

    @Column(name = "late_return_penalty_rate", nullable = false, precision = 12, scale = 2)
    private BigDecimal lateReturnPenaltyRate;

    @Column(length = 2000)
    private String description;

    @Column(name = "location", nullable = false)
    private String location;

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "car_rental_listing_images_id")
    private List<MediaFile> images;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RentalListingStatus status;

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
            status = RentalListingStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
