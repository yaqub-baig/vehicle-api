package com.autoadda.apis.vehicle.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleOwnerDocumentRequest {
    private Integer vehicleOwnerId;
    private Long tradeLicenceId;
    private Long emiratesIdId;
    private Long drivingLicenceId;
}
