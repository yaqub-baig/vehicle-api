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
                                             @RequestHeader(value = "X-User-Id") String userId ,
                                             @RequestHeader(value = "X-User-Role") String userRole,
                                             @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId) {

      log.info( "userRole -> " + userRole + " userId -> " + userId + " request -> "  );
        return vehicleService.addVehicleDetails ( request,userId ,autoAddaId );
    }

    @GetMapping
    public GenericResponse getVehicleDetails(@RequestHeader(value = "X-User-Id") String userId ,
                                             @RequestHeader(value = "X-User-Role") String userRole,
                                             @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId) {
        return vehicleService.getVehicleDetails ( userId,autoAddaId );
    }

    @GetMapping("/{vehicleId}")
    public GenericResponse getVehicleDetailsById(
                                              @RequestHeader(value = "X-User-Id") String userId ,
                                             @RequestHeader(value = "X-User-Role") String userRole,
                                             @PathVariable Integer vehicleId) {
        return vehicleService.getVehicleDetailsByVehicleId ( vehicleId , userId , userRole );
    }

    @GetMapping("images/{vehicleId}")
    public GenericResponse getVehicleImagesById(@RequestHeader(value = "X-User-Id") String userId ,
                                                @RequestHeader(value = "X-User-Role") String userRole,
                                                @PathVariable Integer vehicleId) {
        return vehicleService.getVehicleImagesByVehicleId ( vehicleId , userId , userRole );
    }

    @PutMapping
    public GenericResponse updateVehicleDetails(@RequestBody VehicleRegistrationRequest request,
                                               @RequestParam("vehicleId") Integer vehicleId,
                                               @RequestHeader(value = "X-User-Id") String userId ,
                                               @RequestHeader(value = "X-User-Role") String userRole) {
        return vehicleService.updateVehicleDetails (vehicleId, request, userId );
    }


    @DeleteMapping
    public GenericResponse deleteVehicleDetails(@RequestParam("vehicleId") Integer vehicleId,
                                               @RequestHeader(value = "X-User-Id") String userId ,
                                               @RequestHeader(value = "X-User-Role") String userRole) {
        return vehicleService.deleteVehicleDetails (vehicleId, userId );
    }

}
