package com.autoadda.apis.vehicle.controller;

import com.autoadda.apis.vehicle.enums.CarpoolListingType;
import com.autoadda.apis.vehicle.request.CarpoolListingRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.CarpoolListingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/carpool-listings")
@RequiredArgsConstructor
public class CarpoolListingController {

    private final CarpoolListingService listingService;

    /**
     * Create a new carpool listing (offer or seeker)
     * POST /carpool-listings
     */
    @PostMapping
    public GenericResponse createListing(
            @Valid @RequestBody CarpoolListingRequest request,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return listingService.createListing(request, Integer.parseInt(userId));
    }

    /**
     * Update an active listing
     * PUT /carpool-listings/{id}
     */
    @PutMapping("/{id}")
    public GenericResponse updateListing(
            @PathVariable Long id,
            @Valid @RequestBody CarpoolListingRequest request,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return listingService.updateListing(id, request, Integer.parseInt(userId));
    }

    /**
     * Cancel a listing
     * PUT /carpool-listings/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public GenericResponse cancelListing(
            @PathVariable Long id,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return listingService.cancelListing(id, Integer.parseInt(userId));
    }

    /**
     * Mark a listing as completed
     * PUT /carpool-listings/{id}/complete
     */
    @PutMapping("/{id}/complete")
    public GenericResponse completeListing(
            @PathVariable Long id,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return listingService.completeListing(id, Integer.parseInt(userId));
    }

    /**
     * Get a listing by ID
     * GET /carpool-listings/{id}
     */
    @GetMapping("/{id}")
    public GenericResponse getListingById(@PathVariable Long id) {
        return listingService.getListingById(id);
    }

    /**
     * Get my listings, optionally filtered by type
     * GET /carpool-listings/my-listings?type=OFFER
     */
    @GetMapping("/my-listings")
    public GenericResponse getMyListings(
            @RequestHeader("X-Auto-Adda-Id") String userId,
            @RequestParam(required = false) CarpoolListingType type) {
        return listingService.getMyListings(Integer.parseInt(userId), type);
    }

    /**
     * Get compatible opposite-type listings for one of my own listings
     * GET /carpool-listings/{id}/matches
     */
    @GetMapping("/{id}/matches")
    public GenericResponse getMatches(
            @PathVariable Long id,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return listingService.getMatchesForListing(id, Integer.parseInt(userId));
    }

    /**
     * Browse active listings by type, optionally filtered by route
     * GET /carpool-listings/browse?type=OFFER&origin=Lahore&destination=Islamabad
     */
    @GetMapping("/browse")
    public GenericResponse browse(
            @RequestParam CarpoolListingType type,
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination) {
        return listingService.browseListings(type, origin, destination);
    }
}
