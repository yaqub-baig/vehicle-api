package com.autoadda.apis.vehicle.service;

import com.autoadda.apis.vehicle.request.AddCompanyDriverRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;

public interface CompanyDriverService {

    GenericResponse addDriver(Long companyId, Integer callerId, AddCompanyDriverRequest request);

    GenericResponse removeDriver(Long companyId, Integer callerId, Integer driverUserId);

    GenericResponse getDrivers(Long companyId, Integer callerId);
}
