package com.autoadda.apis.vehicle.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlateCodeRequest {

    @NotBlank(message = "Plate code is required")
    private String code;

    @NotNull(message = "Plate type is required")
    private Long plateTypeId;

    private Integer displayOrder;

    // Optional. Set only for colour codes, e.g. "#1B7F3B"; leave null for text codes.
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Colour must be a hex value like #1B7F3B")
    private String colorHex;
}
