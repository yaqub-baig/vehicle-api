package com.autoadda.apis.vehicle.service;

import com.autoadda.apis.vehicle.request.BorrowingRejectionRequest;
import com.autoadda.apis.vehicle.request.BorrowingReturnRequest;
import com.autoadda.apis.vehicle.request.OwnerInitiatedBorrowingRequest;
import com.autoadda.apis.vehicle.request.VehicleBorrowingRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;

public interface VehicleBorrowingService {

    GenericResponse createBorrowingRequest(VehicleBorrowingRequest request, Integer borrowerId);

    GenericResponse createOwnerInitiatedBorrowing(OwnerInitiatedBorrowingRequest request, Integer ownerId);



    GenericResponse searchUserById(Integer userId);

    GenericResponse approveBorrowingRequest(Long borrowingId, Integer ownerId);

    GenericResponse rejectBorrowingRequest(Long borrowingId, Integer ownerId, BorrowingRejectionRequest request);

    GenericResponse startBorrowing(Long borrowingId, Integer borrowerId);

    GenericResponse initiateReturn(Long borrowingId, Integer borrowerId, BorrowingReturnRequest request);

    GenericResponse confirmReturn(Long borrowingId, Integer userId);

    GenericResponse cancelBorrowingRequest(Long borrowingId, Integer borrowerId);

    GenericResponse getBorrowingById(Long borrowingId);

    GenericResponse getBorrowingsByVehicle(Integer vehicleId);

    GenericResponse getBorrowingsByBorrower(Integer borrowerId);

    GenericResponse getBorrowingsByOwner(Integer ownerId);

    GenericResponse getPendingBorrowingsForOwner(Integer ownerId);

    GenericResponse getActiveBorrowingForVehicle(Integer vehicleId);

    boolean isVehicleAvailableForBorrowing(Integer vehicleId);

    GenericResponse getBorrowingsByCompany(Long companyId);
}
