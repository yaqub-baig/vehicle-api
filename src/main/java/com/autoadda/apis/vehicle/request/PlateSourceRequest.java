package com.autoadda.apis.vehicle.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlateSourceRequest {

    @NotBlank(message = "Plate source code is required")
    private String code;

    @NotBlank(message = "Plate source name is required")
    private String name;

    private Integer displayOrder;

    // Optional. MediaFile id of the emirate's plate template PNG, from POST /mediafiles/upload.
    private Long imageId;
}
