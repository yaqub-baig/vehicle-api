package com.autoadda.apis.vehicle.service.impl;

import com.autoadda.apis.vehicle.dao.CarpoolListingRepository;
import com.autoadda.apis.vehicle.entity.CarpoolListing;
import com.autoadda.apis.vehicle.enums.CarpoolListingStatus;
import com.autoadda.apis.vehicle.enums.CarpoolListingType;
import com.autoadda.apis.vehicle.mapper.CarpoolListingMapper;
import com.autoadda.apis.vehicle.request.CarpoolListingRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.CarpoolListingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarpoolListingServiceImpl implements CarpoolListingService {

    private static final String ERROR = "error";
    private static final String SUCCESS = "success";
    private static final String LISTING_NOT_FOUND = "Carpool listing not found";
    private static final String LISTINGS_RETRIEVED_SUCCESSFULLY = "Listings retrieved successfully";

    private final CarpoolListingRepository listingRepository;
    private final CarpoolListingMapper listingMapper;

    @Override
    @Transactional
    public GenericResponse createListing(CarpoolListingRequest request, Integer userId) {
        log.info("Creating carpool listing type {} for user {}", request.getType(), userId);

        GenericResponse validationError = validateListingRequest(request);
        if (validationError != null) {
            return validationError;
        }

        CarpoolListing listing = listingMapper.toEntity(request, userId);
        CarpoolListing savedListing = listingRepository.save(listing);

        log.info("Carpool listing created with ID: {}", savedListing.getId());

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("201")
                .message("Carpool listing created successfully")
                .body(listingMapper.toResponse(savedListing))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse updateListing(Long listingId, CarpoolListingRequest request, Integer userId) {
        log.info("Updating carpool listing {} by user {}", listingId, userId);

        Optional<CarpoolListing> listingOpt = listingRepository.findByIdAndIsDeletedFalse(listingId);
        if (listingOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(LISTING_NOT_FOUND).build();
        }

        CarpoolListing listing = listingOpt.get();

        if (!listing.getUserId().equals(userId)) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("403")
                    .message("You are not authorized to edit this listing")
                    .build();
        }

        if (listing.getStatus() != CarpoolListingStatus.ACTIVE) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("400")
                    .message("Only active listings can be edited")
                    .build();
        }

        GenericResponse validationError = validateListingRequest(request);
        if (validationError != null) {
            return validationError;
        }

        listingMapper.applyUpdate(listing, request);
        CarpoolListing savedListing = listingRepository.save(listing);

        log.info("Carpool listing {} updated by user {}", listingId, userId);

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Carpool listing updated successfully")
                .body(listingMapper.toResponse(savedListing))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse cancelListing(Long listingId, Integer userId) {
        return closeListing(listingId, userId, CarpoolListingStatus.CANCELLED, "cancelled");
    }

    @Override
    @Transactional
    public GenericResponse completeListing(Long listingId, Integer userId) {
        return closeListing(listingId, userId, CarpoolListingStatus.COMPLETED, "marked as completed");
    }

    private GenericResponse closeListing(Long listingId, Integer userId, CarpoolListingStatus newStatus,
                                          String actionDescription) {
        Optional<CarpoolListing> listingOpt = listingRepository.findByIdAndIsDeletedFalse(listingId);
        if (listingOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(LISTING_NOT_FOUND).build();
        }

        CarpoolListing listing = listingOpt.get();

        if (!listing.getUserId().equals(userId)) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("403")
                    .message("You are not authorized to update this listing")
                    .build();
        }

        if (listing.getStatus() != CarpoolListingStatus.ACTIVE) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("400")
                    .message("Only active listings can be " + actionDescription)
                    .build();
        }

        listing.setStatus(newStatus);
        CarpoolListing savedListing = listingRepository.save(listing);

        log.info("Carpool listing {} {} by user {}", listingId, actionDescription, userId);

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Carpool listing " + actionDescription)
                .body(listingMapper.toResponse(savedListing))
                .build();
    }

    @Override
    public GenericResponse getListingById(Long listingId) {
        Optional<CarpoolListing> listingOpt = listingRepository.findByIdAndIsDeletedFalse(listingId);
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
    public GenericResponse getMyListings(Integer userId, CarpoolListingType typeFilter) {
        List<CarpoolListing> listings = typeFilter != null
                ? listingRepository.findByUserIdAndTypeAndIsDeletedFalseOrderByCreatedAtDesc(userId, typeFilter)
                : listingRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId);

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message(LISTINGS_RETRIEVED_SUCCESSFULLY)
                .body(listingMapper.toResponseList(listings))
                .build();
    }

    @Override
    public GenericResponse getMatchesForListing(Long listingId, Integer userId) {
        Optional<CarpoolListing> listingOpt = listingRepository.findByIdAndIsDeletedFalse(listingId);
        if (listingOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(LISTING_NOT_FOUND).build();
        }

        CarpoolListing listing = listingOpt.get();

        if (!listing.getUserId().equals(userId)) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("403")
                    .message("You can only view matches for your own listing")
                    .build();
        }

        if (listing.getStatus() != CarpoolListingStatus.ACTIVE) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("400")
                    .message("This listing is not active")
                    .build();
        }

        List<CarpoolListing> matches = listing.getType() == CarpoolListingType.SEEKER
                ? listingRepository.findMatchingOffersForSeeker(listing.getOriginCity(), listing.getDestinationCity(),
                        userId, listing.getBudget(), LocalDateTime.now())
                : listingRepository.findMatchingSeekersForOffer(listing.getOriginCity(), listing.getDestinationCity(),
                        userId, listing.getBudget(), LocalDateTime.now());

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Matches retrieved successfully")
                .body(listingMapper.toResponseList(matches))
                .build();
    }

    @Override
    public GenericResponse browseListings(CarpoolListingType type, String originCity, String destinationCity) {
        List<CarpoolListing> listings = listingRepository
                .browseListings(type, originCity, destinationCity, CarpoolListingStatus.ACTIVE);

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message(LISTINGS_RETRIEVED_SUCCESSFULLY)
                .body(listingMapper.toResponseList(listings))
                .build();
    }

    private GenericResponse validateListingRequest(CarpoolListingRequest request) {
        if (request.getTravelDateTime().isBefore(LocalDateTime.now())) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("400")
                    .message("Travel date/time must be in the future")
                    .build();
        }

        if (request.getType() == CarpoolListingType.OFFER) {
            if (request.getAvailableSeats() == null || request.getAvailableSeats() <= 0) {
                return GenericResponse.builder()
                        .status(ERROR)
                        .code("400")
                        .message("Available seats are required for an offer listing")
                        .build();
            }
        } else {
            // SEEKER listings never carry offer-only fields, even if the client sent them.
            request.setAvailableSeats(null);
            request.setCarDescription(null);
        }

        return null;
    }
}
