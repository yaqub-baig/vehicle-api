package com.autoadda.apis.vehicle.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlateCodeResponse {

    private Long id;
    private String code;
    private Integer displayOrder;
    private Boolean isActive;
    private PlateTypeResponse plateType;
}
