package com.autoadda.apis.vehicle.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CountryRequest {

    @NotBlank(message = "Country code is required")
    private String code;

    @NotBlank(message = "Country name is required")
    private String name;
}
