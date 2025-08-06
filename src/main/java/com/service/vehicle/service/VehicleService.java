package com.service.vehicle.service;

import com.service.vehicle.request.VehicleRegistrationRequest;
import com.service.vehicle.response.GenericResponse;

public interface VehicleService {
    GenericResponse addVehicleDetails(VehicleRegistrationRequest request, String userId);

    GenericResponse getVehicleDetails(String userId);

    GenericResponse getVehicleDetailsByVehicleId(Integer vehicleId, String userId, String userRole);

    GenericResponse getVehicleImagesByVehicleId(Integer vehicleId, String userId, String userRole);

    GenericResponse updateVehicleDetails(Integer vehicleId, VehicleRegistrationRequest request, String userId);

    GenericResponse deleteVehicleDetails(Integer vehicleId, String userId);
}
