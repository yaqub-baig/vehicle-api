package com.autoadda.apis.vehicle.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlateSourceResponse {

    private Long id;
    private String code;
    private String name;
    private Integer displayOrder;
    private Boolean isActive;
}
