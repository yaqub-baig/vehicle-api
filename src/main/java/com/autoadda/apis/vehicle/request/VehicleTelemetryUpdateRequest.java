package com.autoadda.apis.vehicle.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleTelemetryUpdateRequest {

    private String odometer;

}
