package com.autoadda.apis.vehicle.controller;

import com.autoadda.apis.vehicle.request.AccidentReportRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.AccidentReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/accident-reports")
@RequiredArgsConstructor
public class AccidentReportController {

    private final AccidentReportService accidentReportService;

    @PostMapping
    public GenericResponse createAccidentReport(@RequestBody AccidentReportRequest request) {
        return accidentReportService.createAccidentReport(request);
    }

    @GetMapping("/{id}")
    public GenericResponse getAccidentReportById(@PathVariable Integer id) {
        return accidentReportService.getAccidentReportById(id);
    }

    @GetMapping
    public GenericResponse getAllAccidentReports() {
        return accidentReportService.getAllAccidentReports();
    }

    @GetMapping("/vehicle/{vehicleId}")
    public GenericResponse getAccidentReportsByVehicle(@PathVariable Integer vehicleId) {
        return accidentReportService.getAccidentReportsByVehicle(vehicleId);
    }

    @GetMapping("/reporter/{reportedBy}")
    public GenericResponse getAccidentReportsByReporter(@PathVariable Integer reportedBy) {
        return accidentReportService.getAccidentReportsByReporter(reportedBy);
    }

    @PutMapping("/{id}")
    public GenericResponse updateAccidentReport(@PathVariable Integer id,
                                                @RequestBody AccidentReportRequest request) {
        return accidentReportService.updateAccidentReport(id, request);
    }

    @DeleteMapping("/{id}")
    public GenericResponse deleteAccidentReport(@PathVariable Integer id) {
        return accidentReportService.deleteAccidentReport(id);
    }
}
