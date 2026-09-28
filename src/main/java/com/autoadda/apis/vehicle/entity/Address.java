package com.autoadda.apis.vehicle.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * country -> stateTerritory -> territoryArea -> street. Embedded twice (origin/destination) on
 * CarpoolListing via @AssociationOverride/@AttributeOverride so each trip end has its own
 * full address without a separate join table.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @ManyToOne
    private Country country;

    @ManyToOne
    private StateTerritory stateTerritory;

    @ManyToOne
    private TerritoryArea territoryArea;

    // Optional: not every pickup/drop point has a formal street/DM code.
    @ManyToOne
    private Street street;

    @Column(length = 500)
    private String googleMapsUrl;
}
