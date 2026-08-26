package com.autoadda.apis.vehicle.controller;

import com.autoadda.apis.vehicle.request.RentalCycleDiscountRequest;
import com.autoadda.apis.vehicle.request.RentalCycleReturnRequest;
import com.autoadda.apis.vehicle.request.RentalCycleStartRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.RentalCycleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/rental-cycles")
@RequiredArgsConstructor
public class RentalCycleController {

    private final RentalCycleService cycleService;

    /**
     * Owner sets/updates the negotiated flat discount before the rental finishes
     * PUT /rental-cycles/{id}/discount
     */
    @PutMapping("/{id}/discount")
    public GenericResponse setDiscount(
            @PathVariable Long id,
            @Valid @RequestBody RentalCycleDiscountRequest request,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return cycleService.setDiscount(id, request, Integer.parseInt(userId));
    }

    /**
     * Renter starts the rental (picks up the vehicle)
     * PUT /rental-cycles/{id}/start
     */
    @PutMapping("/{id}/start")
    public GenericResponse start(
            @PathVariable Long id,
            @Valid @RequestBody RentalCycleStartRequest request,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return cycleService.startCycle(id, request, Integer.parseInt(userId));
    }

    /**
     * Renter returns the vehicle; computes the final price
     * PUT /rental-cycles/{id}/return
     */
    @PutMapping("/{id}/return")
    public GenericResponse returnVehicle(
            @PathVariable Long id,
            @Valid @RequestBody RentalCycleReturnRequest request,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return cycleService.returnCycle(id, request, Integer.parseInt(userId));
    }

    /**
     * Renter or owner cancels a rental that hasn't started yet
     * PUT /rental-cycles/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public GenericResponse cancel(
            @PathVariable Long id,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return cycleService.cancelCycle(id, Integer.parseInt(userId));
    }

    /**
     * Get a rental cycle by ID (participants only)
     * GET /rental-cycles/{id}
     */
    @GetMapping("/{id}")
    public GenericResponse getById(
            @PathVariable Long id,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return cycleService.getCycleById(id, Integer.parseInt(userId));
    }

    /**
     * Get the rental cycle created for an accepted request
     * GET /rental-cycles/request/{requestId}
     */
    @GetMapping("/request/{requestId}")
    public GenericResponse getByRequestId(
            @PathVariable Long requestId,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return cycleService.getCycleByRequestId(requestId, Integer.parseInt(userId));
    }

    /**
     * Get my rentals (as renter)
     * GET /rental-cycles/my-rentals
     */
    @GetMapping("/my-rentals")
    public GenericResponse myRentals(@RequestHeader("X-Auto-Adda-Id") String userId) {
        return cycleService.getMyRentals(Integer.parseInt(userId));
    }

    /**
     * Get rentals of my vehicles (as owner)
     * GET /rental-cycles/my-vehicles
     */
    @GetMapping("/my-vehicles")
    public GenericResponse myVehicleRentals(@RequestHeader("X-Auto-Adda-Id") String userId) {
        return cycleService.getMyVehicleRentals(Integer.parseInt(userId));
    }
}
