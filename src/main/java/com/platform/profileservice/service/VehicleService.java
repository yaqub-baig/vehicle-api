package com.platform.profileservice.service;

import com.platform.profileservice.request.VehicleRegistrationRequest;
import com.platform.profileservice.response.GenericResponse;

public interface VehicleService {
    GenericResponse addVehicleDetails(VehicleRegistrationRequest request, String userId);

    GenericResponse getVehicleDetails(String userId);
}
