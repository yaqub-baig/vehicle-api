package com.autoadda.apis.vehicle.service;

import com.autoadda.apis.vehicle.enums.NumberPlateInterestStatus;
import com.autoadda.apis.vehicle.request.NumberPlateInterestRejectionRequest;
import com.autoadda.apis.vehicle.request.NumberPlateInterestRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;

public interface NumberPlateInterestService {

    GenericResponse createInterest(NumberPlateInterestRequest request, Integer buyerUserId);

    GenericResponse acceptInterest(Long interestId, Integer sellerUserId);

    GenericResponse rejectInterest(Long interestId, Integer sellerUserId, NumberPlateInterestRejectionRequest request);

    GenericResponse cancelInterest(Long interestId, Integer buyerUserId);

    GenericResponse getInterestById(Long interestId, Integer userId);

    GenericResponse getMyInterests(Integer userId, NumberPlateInterestStatus statusFilter);

    GenericResponse getInterestsForListing(Long listingId, Integer sellerUserId);
}
