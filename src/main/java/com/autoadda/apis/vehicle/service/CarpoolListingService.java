package com.autoadda.apis.vehicle.service;

import com.autoadda.apis.vehicle.enums.CarpoolListingType;
import com.autoadda.apis.vehicle.request.CarpoolListingRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;

public interface CarpoolListingService {

    GenericResponse createListing(CarpoolListingRequest request, Integer userId);

    GenericResponse updateListing(Long listingId, CarpoolListingRequest request, Integer userId);

    GenericResponse cancelListing(Long listingId, Integer userId);

    GenericResponse completeListing(Long listingId, Integer userId);

    GenericResponse getListingById(Long listingId);

    GenericResponse getMyListings(Integer userId, CarpoolListingType typeFilter);

    GenericResponse getMatchesForListing(Long listingId, Integer userId);

    GenericResponse browseListings(CarpoolListingType type, Long originTerritoryAreaId, Long destinationTerritoryAreaId);
}
