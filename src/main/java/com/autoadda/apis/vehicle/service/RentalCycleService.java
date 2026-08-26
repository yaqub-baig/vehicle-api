package com.autoadda.apis.vehicle.service;

import com.autoadda.apis.vehicle.request.RentalCycleDiscountRequest;
import com.autoadda.apis.vehicle.request.RentalCycleReturnRequest;
import com.autoadda.apis.vehicle.request.RentalCycleStartRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;

public interface RentalCycleService {

    GenericResponse getCycleById(Long cycleId, Integer callerId);

    GenericResponse getCycleByRequestId(Long requestId, Integer callerId);

    GenericResponse setDiscount(Long cycleId, RentalCycleDiscountRequest request, Integer ownerId);

    GenericResponse startCycle(Long cycleId, RentalCycleStartRequest request, Integer renterId);

    GenericResponse returnCycle(Long cycleId, RentalCycleReturnRequest request, Integer renterId);

    GenericResponse cancelCycle(Long cycleId, Integer callerId);

    GenericResponse getMyRentals(Integer renterId);

    GenericResponse getMyVehicleRentals(Integer ownerId);
}
