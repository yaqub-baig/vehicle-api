package com.autoadda.apis.vehicle.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {

    private CountryResponse country;
    private StateTerritoryResponse stateTerritory;
    private TerritoryAreaResponse territoryArea;
    private StreetResponse street;
    private String googleMapsUrl;
}
