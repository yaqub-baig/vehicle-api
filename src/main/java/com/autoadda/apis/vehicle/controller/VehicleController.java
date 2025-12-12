package com.autoadda.apis.vehicle.controller;

import com.autoadda.apis.vehicle.request.VehicleRegistrationRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.response.VehicleDetailResponse;
import com.autoadda.apis.vehicle.service.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/vehicles")
@RequiredArgsConstructor
@Slf4j
public class VehicleController {
    private final VehicleService vehicleService;

    @PostMapping("/add")
    public GenericResponse addVehicleDetails(@RequestBody VehicleRegistrationRequest request,
                                             @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId) {
        return vehicleService.addVehicleDetails ( request, autoAddaId );
    }

    @GetMapping
    public GenericResponse getVehicleDetails(
            @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId) {
        return vehicleService.getVehicleDetails ( autoAddaId );
    }

    @GetMapping("/{vehicleId}")
    public GenericResponse getVehicleDetailsById(
            @PathVariable Integer vehicleId) {
        return vehicleService.getVehicleDetailsByVehicleId ( vehicleId );
    }

    //isc  --> inter service communication
    @GetMapping("/isc/{vehicleId}")
    public VehicleDetailResponse getVehicleDetailsByIdResponse(
            @PathVariable Integer vehicleId) {
        return vehicleService.getVehicleDetailsByVehicleIdForIsc ( vehicleId );
    }

    @GetMapping("images/{vehicleId}")
    public GenericResponse getVehicleImagesById(
            @PathVariable Integer vehicleId) {
        return vehicleService.getVehicleImagesByVehicleId ( vehicleId );
    }

    @PutMapping
    public GenericResponse updateVehicleDetails(@RequestBody VehicleRegistrationRequest request,
                                                @RequestParam("vehicleId") Integer vehicleId,
                                                @RequestHeader(value = "X-Auto-Adda-Id") String userId) {
        return vehicleService.updateVehicleDetails ( vehicleId, request, userId );
    }


    @DeleteMapping
    public GenericResponse deleteVehicleDetails(@RequestParam("vehicleId") Integer vehicleId,
                                                @RequestHeader(value = "X-Auto-Adda-Id") String userId) {
        return vehicleService.deleteVehicleDetails ( vehicleId, userId );
    }

    @GetMapping("/company/{companyId}")
    public GenericResponse getVehicleDetailsByCompanyId(@PathVariable Long companyId) {
        return vehicleService.getVehiclesByCompanyId ( companyId );
    }

    @GetMapping("/owner/{ownerId}")
    public GenericResponse getVehicleDetailsByOwnerId(@PathVariable Long ownerId) {
        return vehicleService.getVehiclesByOwnerId ( ownerId );
    }

    @GetMapping("/company-owner/{ownerId}")
    public GenericResponse getCompanyVehiclesByOwnerId(@PathVariable Integer ownerId) {
        return vehicleService.getCompanyVehiclesByOwnerId ( ownerId );
    }

    @GetMapping("/validate-chassis")
    public GenericResponse validateChassisNo(@RequestParam("chassisNo") String chassisNo) {
        boolean exists = vehicleService.validateChassisNo(chassisNo);
        return GenericResponse.builder()
                .status("success")
                .code("200")
                .message(exists ? "Chassis number already exists" : "Chassis number is available")
                .body(exists)
                .build();
    }
    @GetMapping("/all")
    public GenericResponse getAllVehicle(){
        return vehicleService.getAllVehicle();
    }
}
