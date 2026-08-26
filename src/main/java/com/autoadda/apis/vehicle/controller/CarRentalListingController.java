package com.autoadda.apis.vehicle.controller;

import com.autoadda.apis.vehicle.request.CarRentalListingRequest;
import com.autoadda.apis.vehicle.request.RentalSearchCriteria;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.CarRentalListingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/car-rental-listings")
@RequiredArgsConstructor
public class CarRentalListingController {

    private final CarRentalListingService listingService;

    /**
     * Owner creates a rental listing for one of their vehicles
     * POST /car-rental-listings
     */
    @PostMapping
    public GenericResponse createListing(
            @Valid @RequestBody CarRentalListingRequest request,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return listingService.createListing(request, Integer.parseInt(userId));
    }

    /**
     * Owner updates their rental listing
     * PUT /car-rental-listings/{id}
     */
    @PutMapping("/{id}")
    public GenericResponse updateListing(
            @PathVariable Long id,
            @Valid @RequestBody CarRentalListingRequest request,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return listingService.updateListing(id, request, Integer.parseInt(userId));
    }

    /**
     * Owner pauses a listing (hides it from browse without losing history)
     * PUT /car-rental-listings/{id}/pause
     */
    @PutMapping("/{id}/pause")
    public GenericResponse pauseListing(
            @PathVariable Long id,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return listingService.pauseListing(id, Integer.parseInt(userId));
    }

    /**
     * Owner reactivates a paused listing
     * PUT /car-rental-listings/{id}/reactivate
     */
    @PutMapping("/{id}/reactivate")
    public GenericResponse reactivateListing(
            @PathVariable Long id,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return listingService.reactivateListing(id, Integer.parseInt(userId));
    }

    /**
     * Owner removes a listing
     * PUT /car-rental-listings/{id}/remove
     */
    @PutMapping("/{id}/remove")
    public GenericResponse removeListing(
            @PathVariable Long id,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return listingService.removeListing(id, Integer.parseInt(userId));
    }

    /**
     * Get a rental listing by ID
     * GET /car-rental-listings/{id}
     */
    @GetMapping("/{id}")
    public GenericResponse getById(@PathVariable Long id) {
        return listingService.getListingById(id);
    }

    /**
     * Get my rental listings (as owner)
     * GET /car-rental-listings/my-listings
     */
    @GetMapping("/my-listings")
    public GenericResponse myListings(@RequestHeader("X-Auto-Adda-Id") String userId) {
        return listingService.getMyListings(Integer.parseInt(userId));
    }

    /**
     * Browse active rental listings, paginated and sorted (cheapest first by default)
     * GET /car-rental-listings/browse?brand=&model=&fromPrice=&toPrice=&rateType=&minKmLimitPerDay=&page=&size=&sortBy=&sortDirection=
     */
    @GetMapping("/browse")
    public GenericResponse browse(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) Double fromPrice,
            @RequestParam(required = false) Double toPrice,
            @RequestParam(required = false) String rateType,
            @RequestParam(required = false) Integer minKmLimitPerDay,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        RentalSearchCriteria criteria = RentalSearchCriteria.builder()
                .brand(brand)
                .model(model)
                .fromPrice(fromPrice)
                .toPrice(toPrice)
                .rateType(rateType)
                .minKmLimitPerDay(minKmLimitPerDay)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
        return listingService.browseListings(criteria);
    }
}
