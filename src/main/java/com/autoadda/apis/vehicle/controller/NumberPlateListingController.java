package com.autoadda.apis.vehicle.controller;

import com.autoadda.apis.vehicle.request.NumberPlateListingRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.NumberPlateListingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@CrossOrigin("*")
@RestController
@RequestMapping("/number-plate-listings")
@RequiredArgsConstructor
public class NumberPlateListingController {

    private final NumberPlateListingService listingService;

    /**
     * Create a new number plate listing
     * POST /number-plate-listings
     */
    @PostMapping
    public GenericResponse createListing(
            @Valid @RequestBody NumberPlateListingRequest request,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return listingService.createListing(request, Integer.parseInt(userId));
    }

    /**
     * Update an active listing
     * PUT /number-plate-listings/{id}
     */
    @PutMapping("/{id}")
    public GenericResponse updateListing(
            @PathVariable Long id,
            @Valid @RequestBody NumberPlateListingRequest request,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return listingService.updateListing(id, request, Integer.parseInt(userId));
    }

    /**
     * Mark a listing as sold
     * PUT /number-plate-listings/{id}/sold
     */
    @PutMapping("/{id}/sold")
    public GenericResponse markSold(
            @PathVariable Long id,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return listingService.markSold(id, Integer.parseInt(userId));
    }

    /**
     * Cancel a listing
     * PUT /number-plate-listings/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public GenericResponse cancelListing(
            @PathVariable Long id,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return listingService.cancelListing(id, Integer.parseInt(userId));
    }

    /**
     * Get a listing by ID
     * GET /number-plate-listings/{id}
     */
    @GetMapping("/{id}")
    public GenericResponse getListingById(@PathVariable Long id) {
        return listingService.getListingById(id);
    }

    /**
     * Get my listings
     * GET /number-plate-listings/my-listings
     */
    @GetMapping("/my-listings")
    public GenericResponse getMyListings(@RequestHeader("X-Auto-Adda-Id") String userId) {
        return listingService.getMyListings(Integer.parseInt(userId));
    }

    /**
     * Browse active listings, optionally filtered
     * GET /number-plate-listings/browse?emirate=Dubai&category=Private&minPrice=1000&maxPrice=50000
     */
    @GetMapping("/browse")
    public GenericResponse browse(
            @RequestParam(required = false) String emirate,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        return listingService.browseListings(emirate, category, minPrice, maxPrice);
    }
}
