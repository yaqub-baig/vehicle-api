package com.autoadda.apis.vehicle.service.impl;

import com.autoadda.apis.vehicle.dao.CarRentalListingRepository;
import com.autoadda.apis.vehicle.dao.VehicleDetailsRepository;
import com.autoadda.apis.vehicle.entity.CarRentalListing;
import com.autoadda.apis.vehicle.entity.VehicleDetails;
import com.autoadda.apis.vehicle.enums.RentalListingStatus;
import com.autoadda.apis.vehicle.exception.InvalidInputException;
import com.autoadda.apis.vehicle.mapper.CarRentalListingMapper;
import com.autoadda.apis.vehicle.request.CarRentalListingRequest;
import com.autoadda.apis.vehicle.request.RentalSearchCriteria;
import com.autoadda.apis.vehicle.response.CarRentalListingResponse;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.response.PagedResponse;
import com.autoadda.apis.vehicle.service.CarRentalListingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarRentalListingServiceImpl implements CarRentalListingService {

    private static final String ERROR = "error";
    private static final String SUCCESS = "success";
    private static final String LISTING_NOT_FOUND = "Rental listing not found";

    private final CarRentalListingRepository listingRepository;
    private final VehicleDetailsRepository vehicleDetailsRepository;
    private final CarRentalListingMapper mapper;

    @Override
    @Transactional
    public GenericResponse createListing(CarRentalListingRequest request, Integer ownerId) {
        log.info("Creating rental listing for vehicle {} by owner {}", request.getVehicleId(), ownerId);

        Optional<VehicleDetails> vehicleOpt = vehicleDetailsRepository.findById(request.getVehicleId());
        if (vehicleOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message("Vehicle not found").build();
        }

        VehicleDetails vehicle = vehicleOpt.get();

        if (!vehicle.getCustomerId().equals(ownerId)) {
            return GenericResponse.builder()
                    .status(ERROR).code("403")
                    .message("You can only create a rental listing for your own vehicle")
                    .build();
        }

        if (listingRepository.existsByVehicleIdAndStatusAndIsDeletedFalse(request.getVehicleId(), RentalListingStatus.ACTIVE)) {
            return GenericResponse.builder()
                    .status(ERROR).code("400")
                    .message("This vehicle already has an active rental listing")
                    .build();
        }

        if (request.getPricePerDay() == null || request.getPricePerDay().compareTo(BigDecimal.ZERO) <= 0) {
            return GenericResponse.builder().status(ERROR).code("400").message("Price per day must be positive").build();
        }

        if (request.getKmLimitPerDay() == null || request.getKmLimitPerDay() <= 0) {
            return GenericResponse.builder().status(ERROR).code("400").message("Km limit per day must be positive").build();
        }

        if (isNegative(request.getExcessKmPenaltyRate()) || isNegative(request.getLateReturnPenaltyRate())) {
            return GenericResponse.builder().status(ERROR).code("400").message("Penalty rates cannot be negative").build();
        }

        CarRentalListing listing = mapper.toEntity(request, ownerId);
        CarRentalListing saved = listingRepository.save(listing);

        log.info("Rental listing created with ID: {}", saved.getId());

        return GenericResponse.builder()
                .status(SUCCESS).code("201")
                .message("Rental listing created successfully")
                .body(mapper.toResponse(saved, vehicle))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse updateListing(Long listingId, CarRentalListingRequest request, Integer ownerId) {
        log.info("Updating rental listing {} by owner {}", listingId, ownerId);

        Optional<CarRentalListing> listingOpt = listingRepository.findByIdAndIsDeletedFalse(listingId);
        if (listingOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(LISTING_NOT_FOUND).build();
        }

        CarRentalListing listing = listingOpt.get();

        if (!listing.getOwnerId().equals(ownerId)) {
            return GenericResponse.builder().status(ERROR).code("403").message("You can only update your own listing").build();
        }

        if (request.getPricePerDay() == null || request.getPricePerDay().compareTo(BigDecimal.ZERO) <= 0) {
            return GenericResponse.builder().status(ERROR).code("400").message("Price per day must be positive").build();
        }

        mapper.applyUpdate(listing, request);
        CarRentalListing saved = listingRepository.save(listing);

        VehicleDetails vehicle = vehicleDetailsRepository.findById(saved.getVehicleId()).orElse(null);

        return GenericResponse.builder()
                .status(SUCCESS).code("200")
                .message("Rental listing updated successfully")
                .body(mapper.toResponse(saved, vehicle))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse pauseListing(Long listingId, Integer ownerId) {
        return changeStatus(listingId, ownerId, RentalListingStatus.PAUSED, "paused");
    }

    @Override
    @Transactional
    public GenericResponse reactivateListing(Long listingId, Integer ownerId) {
        Optional<CarRentalListing> listingOpt = listingRepository.findByIdAndIsDeletedFalse(listingId);
        if (listingOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(LISTING_NOT_FOUND).build();
        }
        CarRentalListing listing = listingOpt.get();

        if (!listing.getOwnerId().equals(ownerId)) {
            return GenericResponse.builder().status(ERROR).code("403").message("You can only manage your own listing").build();
        }

        if (listingRepository.existsByVehicleIdAndStatusAndIsDeletedFalse(listing.getVehicleId(), RentalListingStatus.ACTIVE)) {
            return GenericResponse.builder()
                    .status(ERROR).code("400")
                    .message("This vehicle already has another active rental listing")
                    .build();
        }

        listing.setStatus(RentalListingStatus.ACTIVE);
        CarRentalListing saved = listingRepository.save(listing);
        VehicleDetails vehicle = vehicleDetailsRepository.findById(saved.getVehicleId()).orElse(null);

        return GenericResponse.builder()
                .status(SUCCESS).code("200")
                .message("Rental listing reactivated successfully")
                .body(mapper.toResponse(saved, vehicle))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse removeListing(Long listingId, Integer ownerId) {
        return changeStatus(listingId, ownerId, RentalListingStatus.REMOVED, "removed");
    }

    private GenericResponse changeStatus(Long listingId, Integer ownerId, RentalListingStatus newStatus, String verb) {
        Optional<CarRentalListing> listingOpt = listingRepository.findByIdAndIsDeletedFalse(listingId);
        if (listingOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(LISTING_NOT_FOUND).build();
        }
        CarRentalListing listing = listingOpt.get();

        if (!listing.getOwnerId().equals(ownerId)) {
            return GenericResponse.builder().status(ERROR).code("403").message("You can only manage your own listing").build();
        }

        listing.setStatus(newStatus);
        CarRentalListing saved = listingRepository.save(listing);
        VehicleDetails vehicle = vehicleDetailsRepository.findById(saved.getVehicleId()).orElse(null);

        return GenericResponse.builder()
                .status(SUCCESS).code("200")
                .message("Rental listing " + verb + " successfully")
                .body(mapper.toResponse(saved, vehicle))
                .build();
    }

    @Override
    public GenericResponse getListingById(Long listingId) {
        Optional<CarRentalListing> listingOpt = listingRepository.findByIdAndIsDeletedFalse(listingId);
        if (listingOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(LISTING_NOT_FOUND).build();
        }
        CarRentalListing listing = listingOpt.get();
        VehicleDetails vehicle = vehicleDetailsRepository.findById(listing.getVehicleId()).orElse(null);

        return GenericResponse.builder()
                .status(SUCCESS).code("200")
                .message("Rental listing retrieved successfully")
                .body(mapper.toResponse(listing, vehicle))
                .build();
    }

    @Override
    public GenericResponse getMyListings(Integer ownerId) {
        List<CarRentalListing> listings = listingRepository.findByOwnerIdAndIsDeletedFalseOrderByCreatedAtDesc(ownerId);

        List<CarRentalListingResponse> responses = listings.stream()
                .map(l -> mapper.toResponse(l, vehicleDetailsRepository.findById(l.getVehicleId()).orElse(null)))
                .toList();

        return GenericResponse.builder()
                .status(SUCCESS).code("200")
                .message("Rental listings retrieved successfully")
                .body(responses)
                .build();
    }

    @Override
    public GenericResponse browseListings(RentalSearchCriteria criteria) {
        try {
            criteria.validateRanges();
            criteria.getSortByOrDefault();
            criteria.getSortDirectionOrDefault();
        } catch (InvalidInputException | IllegalArgumentException e) {
            return GenericResponse.builder().status(ERROR).code("400").message(e.getMessage()).build();
        }

        Page<CarRentalListing> page = listingRepository.findByCriteriaWithPagination(criteria);

        List<CarRentalListingResponse> content = page.getContent().stream()
                .map(l -> mapper.toResponse(l, vehicleDetailsRepository.findById(l.getVehicleId()).orElse(null)))
                .toList();

        PagedResponse<CarRentalListingResponse> pagedResponse = PagedResponse.<CarRentalListingResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .numberOfElements(page.getNumberOfElements())
                .build();

        return GenericResponse.builder()
                .status(SUCCESS).code("200")
                .message("Rental listings retrieved successfully")
                .body(pagedResponse)
                .build();
    }

    private boolean isNegative(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) < 0;
    }
}
