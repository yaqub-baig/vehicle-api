package com.autoadda.apis.vehicle.controller;

import com.autoadda.apis.vehicle.request.ClaimActionRequest;
import com.autoadda.apis.vehicle.request.OwnershipClaimRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.VehicleOwnershipClaimService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/ownership-claims")
@RequiredArgsConstructor
public class VehicleOwnershipClaimController {

    private final VehicleOwnershipClaimService claimService;

    /**
     * Request ownership of a vehicle by chassis number
     * POST /ownership-claims/request
     */
    @PostMapping("/request")
    public GenericResponse requestOwnership(
        @RequestBody OwnershipClaimRequest request,
        @RequestHeader(value = "X-Auto-Adda-Id") String userId) {

        Integer requesterId = Integer.parseInt(userId);
        return claimService.createOwnershipClaim(request, requesterId);
    }

    /**
     * Get all ownership claims made by the current user
     * GET /ownership-claims/my-requests
     */
    @GetMapping("/my-requests")
    public GenericResponse getMyRequests(
        @RequestHeader(value = "X-Auto-Adda-Id") String userId) {

        Integer requesterId = Integer.parseInt(userId);
        return claimService.getClaimsByRequester(requesterId);
    }

    /**
     * Get all pending ownership claims for vehicles owned by current user
     * GET /ownership-claims/pending
     */
    @GetMapping("/pending")
    public GenericResponse getPendingClaims(
        @RequestHeader(value = "X-Auto-Adda-Id") String userId) {

        Integer ownerId = Integer.parseInt(userId);
        return claimService.getPendingClaimsForOwner(ownerId);
    }

    /**
     * Get ownership claim details by ID
     * GET /ownership-claims/{claimId}
     */
    @GetMapping("/{claimId}")
    public GenericResponse getClaimById(@PathVariable Long claimId) {
        return claimService.getClaimById(claimId);
    }

    /**
     * Approve an ownership claim (current owner only)
     * PUT /ownership-claims/{claimId}/approve
     */
    @PutMapping("/{claimId}/approve")
    public GenericResponse approveClaim(
        @PathVariable Long claimId,
        @RequestBody(required = false) ClaimActionRequest request,
        @RequestHeader(value = "X-Auto-Adda-Id") String userId) {

        Integer ownerId = Integer.parseInt(userId);
        ClaimActionRequest actionRequest = request != null ? request : new ClaimActionRequest();
        return claimService.approveOwnershipClaim(claimId, actionRequest, ownerId);
    }

    /**
     * Reject an ownership claim (current owner only)
     * PUT /ownership-claims/{claimId}/reject
     */
    @PutMapping("/{claimId}/reject")
    public GenericResponse rejectClaim(
        @PathVariable Long claimId,
        @RequestBody ClaimActionRequest request,
        @RequestHeader(value = "X-Auto-Adda-Id") String userId) {

        Integer ownerId = Integer.parseInt(userId);
        return claimService.rejectOwnershipClaim(claimId, request, ownerId);
    }

    /**
     * Cancel an ownership claim (requester only)
     * PUT /ownership-claims/{claimId}/cancel
     */
    @PutMapping("/{claimId}/cancel")
    public GenericResponse cancelClaim(
        @PathVariable Long claimId,
        @RequestHeader(value = "X-Auto-Adda-Id") String userId) {

        Integer requesterId = Integer.parseInt(userId);
        return claimService.cancelOwnershipClaim(claimId, requesterId);
    }

    /**
     * Get ownership claim history for a vehicle
     * GET /ownership-claims/vehicle/{vehicleId}/history
     */
    @GetMapping("/vehicle/{vehicleId}/history")
    public GenericResponse getVehicleClaimHistory(@PathVariable Integer vehicleId) {
        return claimService.getClaimHistoryByVehicle(vehicleId);
    }
}
