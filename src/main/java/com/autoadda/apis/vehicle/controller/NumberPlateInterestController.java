package com.autoadda.apis.vehicle.controller;

import com.autoadda.apis.vehicle.enums.NumberPlateInterestStatus;
import com.autoadda.apis.vehicle.request.NumberPlateInterestRejectionRequest;
import com.autoadda.apis.vehicle.request.NumberPlateInterestRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.NumberPlateInterestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/number-plate-interests")
@RequiredArgsConstructor
public class NumberPlateInterestController {

    private final NumberPlateInterestService interestService;

    /**
     * Express interest in a plate listing
     * POST /number-plate-interests
     */
    @PostMapping
    public GenericResponse createInterest(
            @Valid @RequestBody NumberPlateInterestRequest request,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return interestService.createInterest(request, Integer.parseInt(userId));
    }

    /**
     * Seller accepts a pending interest; reveals contact info to both parties
     * PUT /number-plate-interests/{id}/accept
     */
    @PutMapping("/{id}/accept")
    public GenericResponse accept(
            @PathVariable Long id,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return interestService.acceptInterest(id, Integer.parseInt(userId));
    }

    /**
     * Seller rejects a pending interest
     * PUT /number-plate-interests/{id}/reject
     */
    @PutMapping("/{id}/reject")
    public GenericResponse reject(
            @PathVariable Long id,
            @RequestHeader("X-Auto-Adda-Id") String userId,
            @RequestBody(required = false) NumberPlateInterestRejectionRequest request) {
        return interestService.rejectInterest(id, Integer.parseInt(userId), request);
    }

    /**
     * Buyer cancels a pending interest
     * PUT /number-plate-interests/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public GenericResponse cancel(
            @PathVariable Long id,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return interestService.cancelInterest(id, Integer.parseInt(userId));
    }

    /**
     * Get an interest by ID (participants only)
     * GET /number-plate-interests/{id}
     */
    @GetMapping("/{id}")
    public GenericResponse getById(
            @PathVariable Long id,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return interestService.getInterestById(id, Integer.parseInt(userId));
    }

    /**
     * Get my interests (as buyer or seller), optionally filtered by status
     * GET /number-plate-interests/my-interests?status=PENDING
     */
    @GetMapping("/my-interests")
    public GenericResponse myInterests(
            @RequestHeader("X-Auto-Adda-Id") String userId,
            @RequestParam(required = false) NumberPlateInterestStatus status) {
        return interestService.getMyInterests(Integer.parseInt(userId), status);
    }

    /**
     * Seller: get all interest expressed on one of their listings
     * GET /number-plate-interests/listing/{listingId}
     */
    @GetMapping("/listing/{listingId}")
    public GenericResponse forListing(
            @PathVariable Long listingId,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return interestService.getInterestsForListing(listingId, Integer.parseInt(userId));
    }
}
