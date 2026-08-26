package com.autoadda.apis.vehicle.controller;

import com.autoadda.apis.vehicle.enums.RentalRequestStatus;
import com.autoadda.apis.vehicle.request.RentalRequestCreateRequest;
import com.autoadda.apis.vehicle.request.RentalRequestRejectionRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.RentalRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/rental-requests")
@RequiredArgsConstructor
public class RentalRequestController {

    private final RentalRequestService requestService;

    /**
     * Renter requests to rent a specific car rental listing
     * POST /rental-requests
     */
    @PostMapping
    public GenericResponse createRequest(
            @Valid @RequestBody RentalRequestCreateRequest request,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return requestService.createRequest(request, Integer.parseInt(userId));
    }

    /**
     * Owner accepts a pending request; reveals contact info to both parties and creates the rental cycle
     * PUT /rental-requests/{id}/accept
     */
    @PutMapping("/{id}/accept")
    public GenericResponse accept(
            @PathVariable Long id,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return requestService.acceptRequest(id, Integer.parseInt(userId));
    }

    /**
     * Owner rejects a pending request
     * PUT /rental-requests/{id}/reject
     */
    @PutMapping("/{id}/reject")
    public GenericResponse reject(
            @PathVariable Long id,
            @RequestHeader("X-Auto-Adda-Id") String userId,
            @RequestBody(required = false) RentalRequestRejectionRequest request) {
        return requestService.rejectRequest(id, Integer.parseInt(userId), request);
    }

    /**
     * Renter cancels a pending request
     * PUT /rental-requests/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public GenericResponse cancel(
            @PathVariable Long id,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return requestService.cancelRequest(id, Integer.parseInt(userId));
    }

    /**
     * Get a rental request by ID (participants only)
     * GET /rental-requests/{id}
     */
    @GetMapping("/{id}")
    public GenericResponse getById(
            @PathVariable Long id,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return requestService.getRequestById(id, Integer.parseInt(userId));
    }

    /**
     * Get my rental requests (as renter or owner), optionally filtered by status
     * GET /rental-requests/my-requests?status=PENDING
     */
    @GetMapping("/my-requests")
    public GenericResponse myRequests(
            @RequestHeader("X-Auto-Adda-Id") String userId,
            @RequestParam(required = false) RentalRequestStatus status) {
        return requestService.getMyRequests(Integer.parseInt(userId), status);
    }

    /**
     * Owner: get pending rental requests across all their listings
     * GET /rental-requests/pending
     */
    @GetMapping("/pending")
    public GenericResponse pending(@RequestHeader("X-Auto-Adda-Id") String userId) {
        return requestService.getPendingRequestsForOwner(Integer.parseInt(userId));
    }

    /**
     * Owner: get all requests on one of their listings
     * GET /rental-requests/listing/{listingId}
     */
    @GetMapping("/listing/{listingId}")
    public GenericResponse forListing(
            @PathVariable Long listingId,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return requestService.getRequestsForListing(listingId, Integer.parseInt(userId));
    }
}
