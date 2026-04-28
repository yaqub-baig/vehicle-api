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
            @RequestParam(required = false) Double fromPrice,
            @RequestParam(required = false) Double toPrice,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) Integer fromYear,
            @RequestParam(required = false) Integer toYear,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String condition) {

        VehicleSearchCriteria criteria = VehicleSearchCriteria.builder ( )
                .location ( location )
                .fromPrice ( fromPrice )
                .toPrice ( toPrice )
                .brand ( brand )
                .model ( model )
                .fromYear ( fromYear )
                .toYear ( toYear )
                .globalSearch ( search )
                .condition ( condition )
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

    @GetMapping("/paginated")
    public GenericResponse getVehicleListingsPaginated(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Double fromPrice,
            @RequestParam(required = false) Double toPrice,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) Integer fromYear,
            @RequestParam(required = false) Integer toYear,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String condition,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {

        VehicleSearchCriteria criteria = VehicleSearchCriteria.builder()
                .location(location)
                .fromPrice(fromPrice)
                .toPrice(toPrice)
                .brand(brand)
                .model(model)
                .fromYear(fromYear)
                .toYear(toYear)
                .globalSearch(search)
                .condition(condition)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        return vehicleListingService.getVehicleListingsWithPagination(criteria);
    }

}
