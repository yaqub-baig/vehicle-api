package com.autoadda.apis.vehicle.service;

import com.autoadda.apis.vehicle.request.CarRentalListingRequest;
import com.autoadda.apis.vehicle.request.RentalSearchCriteria;
import com.autoadda.apis.vehicle.response.GenericResponse;

public interface CarRentalListingService {

    GenericResponse createListing(CarRentalListingRequest request, Integer ownerId);

    GenericResponse updateListing(Long listingId, CarRentalListingRequest request, Integer ownerId);

    GenericResponse pauseListing(Long listingId, Integer ownerId);

    GenericResponse reactivateListing(Long listingId, Integer ownerId);

    GenericResponse removeListing(Long listingId, Integer ownerId);

    GenericResponse getListingById(Long listingId);

    GenericResponse getMyListings(Integer ownerId);

    GenericResponse browseListings(RentalSearchCriteria criteria);
}
