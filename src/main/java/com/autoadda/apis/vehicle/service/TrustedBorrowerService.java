package com.autoadda.apis.vehicle.service;

import com.autoadda.apis.vehicle.request.AddTrustedBorrowerRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;

public interface TrustedBorrowerService {

    GenericResponse addTrustedBorrower(Integer ownerId, AddTrustedBorrowerRequest request);

    GenericResponse removeTrustedBorrower(Integer ownerId, Integer borrowerId);

    GenericResponse getTrustedBorrowers(Integer ownerId);
}
