package com.autoadda.apis.vehicle.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TerritoryAreaRequest {

    @NotBlank(message = "Territory area code is required")
    private String code;

    @NotBlank(message = "Territory area name is required")
    private String name;

    @NotNull(message = "State/territory is required")
    private Long stateTerritoryId;
}
