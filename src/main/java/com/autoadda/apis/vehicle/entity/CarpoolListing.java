package com.autoadda.apis.vehicle.entity;

import com.autoadda.apis.vehicle.enums.CarpoolListingStatus;
import com.autoadda.apis.vehicle.enums.CarpoolListingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "carpool_listing")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarpoolListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CarpoolListingType type;

    @Embedded
    @AssociationOverrides({
        @AssociationOverride(name = "country", joinColumns = @JoinColumn(name = "origin_country_id")),
        @AssociationOverride(name = "stateTerritory", joinColumns = @JoinColumn(name = "origin_state_territory_id")),
        @AssociationOverride(name = "territoryArea", joinColumns = @JoinColumn(name = "origin_territory_area_id")),
        @AssociationOverride(name = "street", joinColumns = @JoinColumn(name = "origin_street_id"))
    })
    @AttributeOverride(name = "googleMapsUrl", column = @Column(name = "origin_google_maps_url", length = 500))
    private Address originAddress;

    @Embedded
    @AssociationOverrides({
        @AssociationOverride(name = "country", joinColumns = @JoinColumn(name = "destination_country_id")),
        @AssociationOverride(name = "stateTerritory", joinColumns = @JoinColumn(name = "destination_state_territory_id")),
        @AssociationOverride(name = "territoryArea", joinColumns = @JoinColumn(name = "destination_territory_area_id")),
        @AssociationOverride(name = "street", joinColumns = @JoinColumn(name = "destination_street_id"))
    })
    @AttributeOverride(name = "googleMapsUrl", column = @Column(name = "destination_google_maps_url", length = 500))
    private Address destinationAddress;

    @Column(name = "travel_date_time", nullable = false)
    private LocalDateTime travelDateTime;

    // OFFER -> asking price per seat. SEEKER -> max amount willing to pay per seat.
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal budget;

    @Column(name = "available_seats")
    private Integer availableSeats;

    @Column(name = "car_description", length = 500)
    private String carDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CarpoolListingStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    // false for a listing auto-created to back a direct connection request (no listing of the
    // user's own) - kept out of browse/matches/my-listings since the owner never posted it.
    @Column(name = "discoverable")
    private Boolean discoverable;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isDeleted == null) {
            isDeleted = false;
        }
        if (status == null) {
            status = CarpoolListingStatus.ACTIVE;
        }
        if (discoverable == null) {
            discoverable = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
