package com.autoadda.apis.vehicle.entity;

import com.autoadda.apis.vehicle.enums.NumberPlateListingStatus;
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
@Table(name = "number_plate_listing")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NumberPlateListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "plate_number", nullable = false)
    private String plateNumber;

    @Column(name = "emirate", nullable = false)
    private String emirate;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "expected_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal expectedPrice;

    // Freeform listing field, not linked to an existing VehicleDetails record.
    @Column(name = "description", length = 2000)
    private String description;

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "number_plate_listing_images_id")
    private List<MediaFile> images;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NumberPlateListingStatus status;

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
            status = NumberPlateListingStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
