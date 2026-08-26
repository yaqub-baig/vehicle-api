package com.autoadda.apis.vehicle.service.impl;

import com.autoadda.apis.vehicle.dao.CarRentalListingRepository;
import com.autoadda.apis.vehicle.dao.RentalCycleRepository;
import com.autoadda.apis.vehicle.dao.RentalRequestRepository;
import com.autoadda.apis.vehicle.entity.CarRentalListing;
import com.autoadda.apis.vehicle.entity.RentalCycle;
import com.autoadda.apis.vehicle.entity.RentalRequest;
import com.autoadda.apis.vehicle.enums.RentalCycleStatus;
import com.autoadda.apis.vehicle.enums.RentalListingStatus;
import com.autoadda.apis.vehicle.enums.RentalRequestStatus;
import com.autoadda.apis.vehicle.enums.RentalType;
import com.autoadda.apis.vehicle.mapper.RentalCycleMapper;
import com.autoadda.apis.vehicle.mapper.RentalRequestMapper;
import com.autoadda.apis.vehicle.request.RentalRequestCreateRequest;
import com.autoadda.apis.vehicle.request.RentalRequestRejectionRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.response.RentalRequestResponse;
import com.autoadda.apis.vehicle.service.RentalRequestService;
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
public class RentalRequestServiceImpl implements RentalRequestService {

    private static final String ERROR = "error";
    private static final String SUCCESS = "success";
    private static final String REQUEST_NOT_FOUND = "Rental request not found";
    private static final String LISTING_NOT_FOUND = "Rental listing not found";
    private static final List<RentalCycleStatus> OPEN_CYCLE_STATUSES =
            List.of(RentalCycleStatus.AWAITING_START, RentalCycleStatus.ACTIVE);

    private final RentalRequestRepository requestRepository;
    private final CarRentalListingRepository listingRepository;
    private final RentalCycleRepository cycleRepository;
    private final RentalRequestMapper requestMapper;
    private final RentalCycleMapper cycleMapper;
    private final RentalNotificationService notificationService;

    @Override
    @Transactional
    public GenericResponse createRequest(RentalRequestCreateRequest request, Integer renterId) {
        log.info("Creating rental request for listing {} by renter {}", request.getListingId(), renterId);

        Optional<CarRentalListing> listingOpt = listingRepository.findByIdAndIsDeletedFalse(request.getListingId());
        if (listingOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(LISTING_NOT_FOUND).build();
        }

        CarRentalListing listing = listingOpt.get();

        if (listing.getStatus() != RentalListingStatus.ACTIVE) {
            return GenericResponse.builder().status(ERROR).code("400").message("This listing is no longer active").build();
        }

        if (listing.getOwnerId().equals(renterId)) {
            return GenericResponse.builder().status(ERROR).code("400").message("Cannot rent your own vehicle").build();
        }

        if (requestRepository.hasActiveRequest(renterId, listing.getId())) {
            return GenericResponse.builder()
                    .status(ERROR).code("400")
                    .message("You already have a pending request on this listing")
                    .build();
        }

        if (cycleRepository.existsByVehicleIdAndStatusInAndIsDeletedFalse(listing.getVehicleId(), OPEN_CYCLE_STATUSES)) {
            return GenericResponse.builder()
                    .status(ERROR).code("400")
                    .message("This vehicle is currently rented out")
                    .build();
        }

        if (request.getRentalType() == RentalType.LIMITED) {
            if (request.getDurationValue() == null || request.getDurationValue() <= 0) {
                return GenericResponse.builder()
                        .status(ERROR).code("400")
                        .message("Duration is required for a limited rental")
                        .build();
            }
        } else {
            // UNLIMITED requests never carry a duration, even if the client sent one.
            request.setDurationValue(null);
        }

        RentalRequest entity = requestMapper.toEntity(request, listing, renterId);
        RentalRequest saved = requestRepository.save(entity);

        log.info("Rental request created with ID: {}", saved.getId());

        notificationService.sendRequestCreatedNotification(saved, listing);

        return GenericResponse.builder()
                .status(SUCCESS).code("201")
                .message("Rental request sent to the owner")
                .body(requestMapper.toResponse(saved, listing))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse acceptRequest(Long requestId, Integer ownerId) {
        log.info("Accepting rental request {} by owner {}", requestId, ownerId);

        Optional<RentalRequest> requestOpt = requestRepository.findByIdAndIsDeletedFalse(requestId);
        if (requestOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(REQUEST_NOT_FOUND).build();
        }

        RentalRequest rentalRequest = requestOpt.get();

        if (!rentalRequest.getOwnerId().equals(ownerId)) {
            return GenericResponse.builder().status(ERROR).code("403").message("Only the owner can respond to this request").build();
        }

        if (rentalRequest.getStatus() != RentalRequestStatus.PENDING) {
            return GenericResponse.builder().status(ERROR).code("400").message("This request has already been responded to").build();
        }

        CarRentalListing listing = listingRepository.findById(rentalRequest.getListingId()).orElse(null);
        if (listing == null) {
            return GenericResponse.builder().status(ERROR).code("404").message(LISTING_NOT_FOUND).build();
        }

        if (cycleRepository.existsByVehicleIdAndStatusInAndIsDeletedFalse(rentalRequest.getVehicleId(), OPEN_CYCLE_STATUSES)) {
            return GenericResponse.builder()
                    .status(ERROR).code("400")
                    .message("This vehicle is currently rented out")
                    .build();
        }

        rentalRequest.setStatus(RentalRequestStatus.ACCEPTED);
        rentalRequest.setRespondedAt(LocalDateTime.now());
        RentalRequest savedRequest = requestRepository.save(rentalRequest);

        RentalCycle cycle = cycleMapper.toEntity(savedRequest, listing);
        cycleRepository.save(cycle);

        log.info("Rental request {} accepted by owner {}; created rental cycle {}",
                requestId, ownerId, cycle.getId());

        notificationService.sendRequestAcceptedNotification(savedRequest, listing);

        return GenericResponse.builder()
                .status(SUCCESS).code("200")
                .message("Request accepted. Contact details have been shared with both parties.")
                .body(requestMapper.toResponse(savedRequest, listing))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse rejectRequest(Long requestId, Integer ownerId, RentalRequestRejectionRequest request) {
        log.info("Rejecting rental request {} by owner {}", requestId, ownerId);

        Optional<RentalRequest> requestOpt = requestRepository.findByIdAndIsDeletedFalse(requestId);
        if (requestOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(REQUEST_NOT_FOUND).build();
        }

        RentalRequest rentalRequest = requestOpt.get();

        if (!rentalRequest.getOwnerId().equals(ownerId)) {
            return GenericResponse.builder().status(ERROR).code("403").message("Only the owner can respond to this request").build();
        }

        if (rentalRequest.getStatus() != RentalRequestStatus.PENDING) {
            return GenericResponse.builder().status(ERROR).code("400").message("This request has already been responded to").build();
        }

        rentalRequest.setStatus(RentalRequestStatus.REJECTED);
        rentalRequest.setRespondedAt(LocalDateTime.now());
        if (request != null) {
            rentalRequest.setRejectionReason(request.getRejectionReason());
        }
        RentalRequest saved = requestRepository.save(rentalRequest);

        log.info("Rental request {} rejected by owner {}", requestId, ownerId);

        CarRentalListing listing = listingRepository.findById(rentalRequest.getListingId()).orElse(null);
        notificationService.sendRequestRejectedNotification(saved, listing);

        return GenericResponse.builder()
                .status(SUCCESS).code("200")
                .message("Request declined")
                .body(requestMapper.toResponse(saved, listing))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse cancelRequest(Long requestId, Integer renterId) {
        log.info("Cancelling rental request {} by renter {}", requestId, renterId);

        Optional<RentalRequest> requestOpt = requestRepository.findByIdAndIsDeletedFalse(requestId);
        if (requestOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(REQUEST_NOT_FOUND).build();
        }

        RentalRequest rentalRequest = requestOpt.get();

        if (!rentalRequest.getRenterId().equals(renterId)) {
            return GenericResponse.builder().status(ERROR).code("403").message("Only the renter can cancel this request").build();
        }

        if (rentalRequest.getStatus() != RentalRequestStatus.PENDING) {
            return GenericResponse.builder().status(ERROR).code("400").message("Only pending requests can be cancelled").build();
        }

        rentalRequest.setStatus(RentalRequestStatus.CANCELLED);
        rentalRequest.setRespondedAt(LocalDateTime.now());
        RentalRequest saved = requestRepository.save(rentalRequest);

        log.info("Rental request {} cancelled by renter {}", requestId, renterId);

        CarRentalListing listing = listingRepository.findById(rentalRequest.getListingId()).orElse(null);
        notificationService.sendRequestCancelledNotification(saved, listing);

        return GenericResponse.builder()
                .status(SUCCESS).code("200")
                .message("Rental request cancelled")
                .body(requestMapper.toResponse(saved, listing))
                .build();
    }

    @Override
    public GenericResponse getRequestById(Long requestId, Integer userId) {
        Optional<RentalRequest> requestOpt = requestRepository.findByIdAndIsDeletedFalse(requestId);
        if (requestOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(REQUEST_NOT_FOUND).build();
        }

        RentalRequest rentalRequest = requestOpt.get();

        if (!rentalRequest.getRenterId().equals(userId) && !rentalRequest.getOwnerId().equals(userId)) {
            return GenericResponse.builder().status(ERROR).code("403").message("You are not a participant in this request").build();
        }

        return GenericResponse.builder()
                .status(SUCCESS).code("200")
                .message("Request retrieved successfully")
                .body(mapRequest(rentalRequest))
                .build();
    }

    @Override
    public GenericResponse getMyRequests(Integer userId, RentalRequestStatus statusFilter) {
        List<RentalRequest> requests = requestRepository.findAllForUser(userId);
        if (statusFilter != null) {
            requests = requests.stream().filter(r -> r.getStatus() == statusFilter).toList();
        }

        return GenericResponse.builder()
                .status(SUCCESS).code("200")
                .message("Rental requests retrieved successfully")
                .body(requests.stream().map(this::mapRequest).toList())
                .build();
    }

    @Override
    public GenericResponse getPendingRequestsForOwner(Integer ownerId) {
        List<RentalRequest> requests = requestRepository.findPendingForOwner(ownerId);

        return GenericResponse.builder()
                .status(SUCCESS).code("200")
                .message("Pending rental requests retrieved successfully")
                .body(requests.stream().map(this::mapRequest).toList())
                .build();
    }

    @Override
    public GenericResponse getRequestsForListing(Long listingId, Integer ownerId) {
        Optional<CarRentalListing> listingOpt = listingRepository.findByIdAndIsDeletedFalse(listingId);
        if (listingOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(LISTING_NOT_FOUND).build();
        }

        CarRentalListing listing = listingOpt.get();

        if (!listing.getOwnerId().equals(ownerId)) {
            return GenericResponse.builder().status(ERROR).code("403").message("You can only view requests on your own listing").build();
        }

        List<RentalRequest> requests = requestRepository.findByListingIdAndIsDeletedFalseOrderByCreatedAtDesc(listingId);

        return GenericResponse.builder()
                .status(SUCCESS).code("200")
                .message("Rental requests retrieved successfully")
                .body(requests.stream().map(r -> requestMapper.toResponse(r, listing)).toList())
                .build();
    }

    private RentalRequestResponse mapRequest(RentalRequest rentalRequest) {
        CarRentalListing listing = listingRepository.findById(rentalRequest.getListingId()).orElse(null);
        return requestMapper.toResponse(rentalRequest, listing);
    }
}
