package com.autoadda.apis.vehicle.service;

import com.autoadda.apis.vehicle.request.NumberPlateListingRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;

import java.math.BigDecimal;

public interface NumberPlateListingService {

    GenericResponse createListing(NumberPlateListingRequest request, Integer userId);

    GenericResponse updateListing(Long listingId, NumberPlateListingRequest request, Integer userId);

    GenericResponse markSold(Long listingId, Integer userId);

    GenericResponse cancelListing(Long listingId, Integer userId);

    GenericResponse getListingById(Long listingId);

    GenericResponse getMyListings(Integer userId);

    GenericResponse browseListings(String emirate, String category, BigDecimal minPrice, BigDecimal maxPrice);
}
