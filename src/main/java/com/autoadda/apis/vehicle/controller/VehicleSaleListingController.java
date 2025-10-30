package com.autoadda.apis.vehicle.controller;

import com.autoadda.apis.vehicle.request.VehicleListingRequest;
import com.autoadda.apis.vehicle.request.VehicleSearchCriteria;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.impl.VehicleListingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
@CrossOrigin("*")
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
    public GenericResponse getVehicleListings(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Double price,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String search) {

        VehicleSearchCriteria criteria = VehicleSearchCriteria.builder ( )
                .location ( location )
                .price ( price )
                .brand ( brand )
                .model ( model )
                .year ( year )
                .globalSearch ( search )
                .build ( );

        return vehicleListingService.getVehicleListings ( criteria );
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
