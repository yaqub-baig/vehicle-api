package com.autoadda.apis.vehicle.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BrandOnboardResponse {
    private Integer id;
    private String brandName;
    private String modelName;
    private String engineType;
    private String vehicleType;
    private String requestReferenceId;
    private Boolean isChecked;

}
