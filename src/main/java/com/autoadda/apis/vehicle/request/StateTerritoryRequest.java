package com.autoadda.apis.vehicle.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StateTerritoryRequest {

    @NotBlank(message = "State/territory code is required")
    private String code;

    @NotBlank(message = "State/territory name is required")
    private String name;

    @NotNull(message = "Country is required")
    private Long countryId;
}
