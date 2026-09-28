package com.autoadda.apis.vehicle.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreetResponse {

    private Long id;
    private String code;
    private String name;
    private Boolean isActive;
    private TerritoryAreaResponse territoryArea;
}
