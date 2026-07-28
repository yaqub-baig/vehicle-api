package com.autoadda.apis.vehicle.controller;

import com.autoadda.apis.vehicle.request.AddCompanyDriverRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.CompanyDriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/companies/{companyId}/drivers")
@RequiredArgsConstructor
public class CompanyDriverController {

    private final CompanyDriverService companyDriverService;

    /**
     * Company owner adds a driver to the company's driver roster,
     * making that driver available for selection as a borrower.
     * POST /companies/{companyId}/drivers
     */
    @PostMapping
    public GenericResponse addDriver(
            @PathVariable Long companyId,
            @RequestBody AddCompanyDriverRequest request,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        Integer callerId = Integer.parseInt(userId);
        return companyDriverService.addDriver(companyId, callerId, request);
    }

    /**
     * Company owner removes a driver from the company's driver roster.
     * DELETE /companies/{companyId}/drivers/{driverUserId}
     */
    @DeleteMapping("/{driverUserId}")
    public GenericResponse removeDriver(
            @PathVariable Long companyId,
            @PathVariable Integer driverUserId,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        Integer callerId = Integer.parseInt(userId);
        return companyDriverService.removeDriver(companyId, callerId, driverUserId);
    }

    /**
     * List a company's drivers.
     * GET /companies/{companyId}/drivers
     */
    @GetMapping
    public GenericResponse getDrivers(
            @PathVariable Long companyId,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        Integer callerId = Integer.parseInt(userId);
        return companyDriverService.getDrivers(companyId, callerId);
    }
}
