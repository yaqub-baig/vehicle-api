package com.autoadda.apis.vehicle.service;

import com.autoadda.apis.vehicle.request.AccidentReportRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;

public interface AccidentReportService {
    GenericResponse createAccidentReport(AccidentReportRequest request);
    GenericResponse getAccidentReportById(Integer id);
    GenericResponse getAllAccidentReports();
    GenericResponse getAccidentReportsByVehicle(Integer vehicleId);
    GenericResponse getAccidentReportsByReporter(Integer reportedBy);
    GenericResponse updateAccidentReport(Integer id, AccidentReportRequest request);
    GenericResponse deleteAccidentReport(Integer id);
}
