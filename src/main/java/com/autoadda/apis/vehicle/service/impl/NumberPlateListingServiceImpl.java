package com.autoadda.apis.vehicle.service.impl;

import com.autoadda.apis.vehicle.dao.NumberPlateListingRepository;
import com.autoadda.apis.vehicle.entity.NumberPlateListing;
import com.autoadda.apis.vehicle.enums.NumberPlateListingStatus;
import com.autoadda.apis.vehicle.mapper.NumberPlateListingMapper;
import com.autoadda.apis.vehicle.request.NumberPlateListingRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.NumberPlateListingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NumberPlateListingServiceImpl implements NumberPlateListingService {

    private static final String ERROR = "error";
    private static final String SUCCESS = "success";
    private static final String LISTING_NOT_FOUND = "Number plate listing not found";
    private static final String LISTINGS_RETRIEVED_SUCCESSFULLY = "Listings retrieved successfully";

    private final NumberPlateListingRepository listingRepository;
    private final NumberPlateListingMapper listingMapper;

    @Override
    @Transactional
    public GenericResponse createListing(NumberPlateListingRequest request, Integer userId) {
        log.info("Creating number plate listing for user {}", userId);

        NumberPlateListing listing = listingMapper.toEntity(request, userId);
        NumberPlateListing savedListing = listingRepository.save(listing);

        log.info("Number plate listing created with ID: {}", savedListing.getId());

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("201")
                .message("Number plate listing created successfully")
                .body(listingMapper.toResponse(savedListing))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse updateListing(Long listingId, NumberPlateListingRequest request, Integer userId) {
        log.info("Updating number plate listing {} by user {}", listingId, userId);

        Optional<NumberPlateListing> listingOpt = listingRepository.findByIdAndIsDeletedFalse(listingId);
        if (listingOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(LISTING_NOT_FOUND).build();
        }

        NumberPlateListing listing = listingOpt.get();

        if (!listing.getUserId().equals(userId)) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("403")
                    .message("You are not authorized to edit this listing")
                    .build();
        }

        if (listing.getStatus() != NumberPlateListingStatus.ACTIVE) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("400")
                    .message("Only active listings can be edited")
                    .build();
        }

        listingMapper.applyUpdate(listing, request);
        NumberPlateListing savedListing = listingRepository.save(listing);

        log.info("Number plate listing {} updated by user {}", listingId, userId);

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Number plate listing updated successfully")
                .body(listingMapper.toResponse(savedListing))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse markSold(Long listingId, Integer userId) {
        return closeListing(listingId, userId, NumberPlateListingStatus.SOLD, "marked as sold");
    }

    @Override
    @Transactional
    public GenericResponse cancelListing(Long listingId, Integer userId) {
        return closeListing(listingId, userId, NumberPlateListingStatus.CANCELLED, "cancelled");
    }

    // Marking a listing sold is a distinct, manual seller action. Accepting a buyer's interest
    // does NOT trigger this automatically - the seller may still be weighing multiple interests.
    private GenericResponse closeListing(Long listingId, Integer userId, NumberPlateListingStatus newStatus,
                                          String actionDescription) {
        Optional<NumberPlateListing> listingOpt = listingRepository.findByIdAndIsDeletedFalse(listingId);
        if (listingOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(LISTING_NOT_FOUND).build();
        }

        NumberPlateListing listing = listingOpt.get();

        if (!listing.getUserId().equals(userId)) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("403")
                    .message("You are not authorized to update this listing")
                    .build();
        }

        if (listing.getStatus() != NumberPlateListingStatus.ACTIVE) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("400")
                    .message("Only active listings can be " + actionDescription)
                    .build();
        }

        listing.setStatus(newStatus);
        NumberPlateListing savedListing = listingRepository.save(listing);

        log.info("Number plate listing {} {} by user {}", listingId, actionDescription, userId);

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Number plate listing " + actionDescription)
                .body(listingMapper.toResponse(savedListing))
                .build();
    }

    @Override
    public GenericResponse getListingById(Long listingId) {
        Optional<NumberPlateListing> listingOpt = listingRepository.findByIdAndIsDeletedFalse(listingId);
        if (listingOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(LISTING_NOT_FOUND).build();
        }

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Listing retrieved successfully")
                .body(listingMapper.toResponse(listingOpt.get()))
                .build();
    }

    @Override
    public GenericResponse getMyListings(Integer userId) {
        List<NumberPlateListing> listings = listingRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId);

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message(LISTINGS_RETRIEVED_SUCCESSFULLY)
                .body(listingMapper.toResponseList(listings))
                .build();
    }

    @Override
    public GenericResponse browseListings(Long plateSourceId, Long plateTypeId, Long plateCodeId,
                                          BigDecimal minPrice, BigDecimal maxPrice) {
        List<NumberPlateListing> listings = listingRepository.browseListings(plateSourceId, plateTypeId, plateCodeId,
                minPrice, maxPrice);

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message(LISTINGS_RETRIEVED_SUCCESSFULLY)
                .body(listingMapper.toResponseList(listings))
                .build();
    }
}
