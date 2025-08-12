package com.autoadda.apis.vehicle.service;

import com.autoadda.apis.vehicle.request.VehicleRegistrationRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;

public interface VehicleService {
    GenericResponse addVehicleDetails(VehicleRegistrationRequest request, String userId, String autoAddaId);

    GenericResponse getVehicleDetails(String userId, String autoAddaId);

    GenericResponse getVehicleDetailsByVehicleId(Integer vehicleId, String userId, String userRole);

    GenericResponse getVehicleImagesByVehicleId(Integer vehicleId, String userId, String userRole);

    GenericResponse updateVehicleDetails(Integer vehicleId, VehicleRegistrationRequest request, String userId);

    GenericResponse deleteVehicleDetails(Integer vehicleId, String userId);
}
