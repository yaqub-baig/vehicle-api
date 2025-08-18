package com.autoadda.apis.vehicle.controller;

import com.autoadda.apis.vehicle.request.VehicleRegistrationRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("images/{vehicleId}")
    public GenericResponse getVehicleImagesById(
                                                @PathVariable Integer vehicleId) {
        return vehicleService.getVehicleImagesByVehicleId ( vehicleId);
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

}
