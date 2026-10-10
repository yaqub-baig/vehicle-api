package com.autoadda.apis.vehicle.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// Code/letter/colour printed on a plate of a given type (e.g. "A", "1", "WHITE").
// Some plate types have no codes at all.
@Entity
@Table(name = "plate_code", uniqueConstraints = @UniqueConstraint(columnNames = {"plate_type_id", "code"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlateCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String code;

    @ManyToOne
    @JoinColumn(name = "plate_type_id", nullable = false)
    private PlateType plateType;

    // Set for colour codes (GREEN, RED, ...): the plate body colour. Null for text codes (A, 1, ...),
    // which are printed on the plate instead.
    @Column(name = "color_hex", length = 7)
    private String colorHex;

    // Optional override of the source's plate image for this code, e.g. a higher-contrast template
    // for a dark plate colour. Null means use the plate source's image.
    @ManyToOne
    @JoinColumn(name = "image_id")
    private MediaFile image;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
