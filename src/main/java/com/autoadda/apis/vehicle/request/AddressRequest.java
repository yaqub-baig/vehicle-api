package com.autoadda.apis.vehicle.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {

    @NotNull(message = "Country is required")
    private Long countryId;

    @NotNull(message = "State/territory is required")
    private Long stateTerritoryId;

    @NotNull(message = "Territory area is required")
    private Long territoryAreaId;

    // Optional: not every pickup/drop point has a formal street/DM code.
    private Long streetId;

    private String googleMapsUrl;
}
