package com.autoadda.apis.vehicle.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
}
