package com.autoadda.apis.vehicle.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StreetRequest {

    @NotBlank(message = "Street code is required")
    private String code;

    private String name;

    @NotNull(message = "Territory area is required")
    private Long territoryAreaId;
}
