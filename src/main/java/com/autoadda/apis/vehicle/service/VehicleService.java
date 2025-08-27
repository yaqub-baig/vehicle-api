package com.autoadda.apis.vehicle.service;

import com.autoadda.apis.vehicle.request.VehicleRegistrationRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;

public interface VehicleService {
    GenericResponse addVehicleDetails(VehicleRegistrationRequest request,  String autoAddaId);

    GenericResponse getVehicleDetails( String autoAddaId);

    GenericResponse getVehicleDetailsByVehicleId(Integer vehicleId);

    GenericResponse getVehicleImagesByVehicleId(Integer vehicleId);

    GenericResponse updateVehicleDetails(Integer vehicleId, VehicleRegistrationRequest request, String userId);

    GenericResponse deleteVehicleDetails(Integer vehicleId, String userId);
}
