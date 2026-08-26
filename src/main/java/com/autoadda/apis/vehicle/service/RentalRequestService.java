package com.autoadda.apis.vehicle.service;

import com.autoadda.apis.vehicle.enums.RentalRequestStatus;
import com.autoadda.apis.vehicle.request.RentalRequestCreateRequest;
import com.autoadda.apis.vehicle.request.RentalRequestRejectionRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;

public interface RentalRequestService {

    GenericResponse createRequest(RentalRequestCreateRequest request, Integer renterId);

    GenericResponse acceptRequest(Long requestId, Integer ownerId);

    GenericResponse rejectRequest(Long requestId, Integer ownerId, RentalRequestRejectionRequest request);

    GenericResponse cancelRequest(Long requestId, Integer renterId);

    GenericResponse getRequestById(Long requestId, Integer userId);

    GenericResponse getMyRequests(Integer userId, RentalRequestStatus statusFilter);

    GenericResponse getPendingRequestsForOwner(Integer ownerId);

    GenericResponse getRequestsForListing(Long listingId, Integer ownerId);
}
