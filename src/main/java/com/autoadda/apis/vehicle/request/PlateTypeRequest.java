package com.autoadda.apis.vehicle.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlateTypeRequest {

    @NotBlank(message = "Plate type code is required")
    private String code;

    @NotBlank(message = "Plate type name is required")
    private String name;

    @NotNull(message = "Plate source is required")
    private Long plateSourceId;

    private Integer displayOrder;
}
