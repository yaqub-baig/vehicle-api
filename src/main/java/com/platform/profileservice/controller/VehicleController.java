package com.platform.profileservice.controller;

import com.platform.profileservice.request.VehicleRegistrationRequest;
import com.platform.profileservice.response.GenericResponse;
import com.platform.profileservice.service.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vehicle")
@RequiredArgsConstructor
@Slf4j
public class VehicleController {
    private final VehicleService vehicleService;

    @PostMapping("/add")
    public GenericResponse addVehicleDetails(@RequestBody VehicleRegistrationRequest request,
                                             @RequestHeader(value = "X-User-Id") String userId ,
                                             @RequestHeader(value = "X-User-Role") String userRole) {

      log.info( "userRole -> " + userRole + " userId -> " + userId + " request -> "  );
        return vehicleService.addVehicleDetails ( request,userId );
    }

    @GetMapping
    public GenericResponse getVehicleDetails(@RequestHeader(value = "X-User-Id") String userId ,
                                             @RequestHeader(value = "X-User-Role") String userRole) {

        return vehicleService.getVehicleDetails ( userId );
    }


}
