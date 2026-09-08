package com.autoadda.apis.vehicle.entity;

import com.autoadda.apis.vehicle.enums.CatalogueOfferingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * A brand/model/trim a NEW_CAR_SHOWROOM offers. This is a catalogue entry, not a specific
 * chassis - buyers browse it and request a quote rather than buying a unit directly.
 */
@Entity
@Table(name = "catalogue_offering")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogueOffering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    // "trim" is a reserved word in some SQL dialects - use trim_name.
    @Column(name = "trim_name")
    private String trimName;

    // "year" is a MySQL keyword - use model_year.
    @Column(name = "model_year")
    private Integer modelYear;

    @Column(name = "indicative_price", precision = 12, scale = 2)
    private BigDecimal indicativePrice;

    @Column(length = 2000)
    private String description;

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "catalogue_offering_images_id")
    private List<MediaFile> images;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CatalogueOfferingStatus status;

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
            status = CatalogueOfferingStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
