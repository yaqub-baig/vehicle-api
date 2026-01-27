package com.autoadda.apis.vehicle.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleServiceHistoryResponse {
    private Integer id;
    private String status;
}
