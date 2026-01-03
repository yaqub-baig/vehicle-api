package com.autoadda.apis.vehicle.service;

import com.autoadda.apis.vehicle.request.ClaimActionRequest;
import com.autoadda.apis.vehicle.request.OwnershipClaimRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;

public interface VehicleOwnershipClaimService {

    /**
     * Create a new ownership claim for a vehicle
     */
    GenericResponse createOwnershipClaim(OwnershipClaimRequest request, Integer requesterId);

    /**
     * Approve an ownership claim (transfers ownership)
     */
    GenericResponse approveOwnershipClaim(Long claimId, ClaimActionRequest request, Integer ownerId);

    /**
     * Reject an ownership claim
     */
    GenericResponse rejectOwnershipClaim(Long claimId, ClaimActionRequest request, Integer ownerId);

    /**
     * Cancel a claim by requester
     */
    GenericResponse cancelOwnershipClaim(Long claimId, Integer requesterId);

    /**
     * Get all claims made by a requester
     */
    GenericResponse getClaimsByRequester(Integer requesterId);

    /**
     * Get all pending claims for vehicles owned by user
     */
    GenericResponse getPendingClaimsForOwner(Integer ownerId);

    /**
     * Get claim details by ID
     */
    GenericResponse getClaimById(Long claimId);

    /**
     * Get claim history for a specific vehicle
     */
    GenericResponse getClaimHistoryByVehicle(Integer vehicleId);
}
