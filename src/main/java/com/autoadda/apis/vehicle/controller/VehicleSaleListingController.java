package com.autoadda.apis.vehicle.controller;

import com.autoadda.apis.vehicle.request.VehicleListingRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.VehicleListingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/vehicle-listing")
public class VehicleSaleListingController {

    private final VehicleListingService vehicleListingService;

    @PostMapping
    public GenericResponse createVehicleListing(@RequestBody VehicleListingRequest vehicleListingRequest) {
        return vehicleListingService.createVehicleListing ( vehicleListingRequest );
    }

    @GetMapping
    public GenericResponse getVehicleListings(@RequestParam(required = false) String location) {
        location = location != null ? location : "";
        return vehicleListingService.getVehicleListings ( location);
    }

    @GetMapping("/{id}")
    public GenericResponse getVehicleListingById(@PathVariable Integer id) {
        return vehicleListingService.getVehicleListingById ( id );
    }

    @GetMapping("/user/{userId}")
    public GenericResponse getVehicleListingsByUserId(@PathVariable Integer userId) {

        return vehicleListingService.getVehicleListingsByUserId ( userId );
    }
    @PutMapping("/{id}")
    public GenericResponse updateVehicleListing(@PathVariable Integer id, @RequestBody VehicleListingRequest vehicleListingRequest) {
        return vehicleListingService.updateVehicleListing ( id, vehicleListingRequest );
    }


}
