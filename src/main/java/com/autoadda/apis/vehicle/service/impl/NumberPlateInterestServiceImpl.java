package com.autoadda.apis.vehicle.service.impl;

import com.autoadda.apis.vehicle.dao.NumberPlateInterestRepository;
import com.autoadda.apis.vehicle.dao.NumberPlateListingRepository;
import com.autoadda.apis.vehicle.entity.NumberPlateInterest;
import com.autoadda.apis.vehicle.entity.NumberPlateListing;
import com.autoadda.apis.vehicle.enums.NumberPlateInterestStatus;
import com.autoadda.apis.vehicle.enums.NumberPlateListingStatus;
import com.autoadda.apis.vehicle.mapper.NumberPlateInterestMapper;
import com.autoadda.apis.vehicle.request.NumberPlateInterestRejectionRequest;
import com.autoadda.apis.vehicle.request.NumberPlateInterestRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.response.NumberPlateInterestResponse;
import com.autoadda.apis.vehicle.service.NumberPlateInterestService;
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
public class NumberPlateInterestServiceImpl implements NumberPlateInterestService {

    private static final String ERROR = "error";
    private static final String SUCCESS = "success";
    private static final String INTEREST_NOT_FOUND = "Number plate interest not found";
    private static final String LISTING_NOT_FOUND = "Number plate listing not found";

    private final NumberPlateInterestRepository interestRepository;
    private final NumberPlateListingRepository listingRepository;
    private final NumberPlateInterestMapper interestMapper;
    private final NumberPlateNotificationService notificationService;

    @Override
    @Transactional
    public GenericResponse createInterest(NumberPlateInterestRequest request, Integer buyerUserId) {
        log.info("Creating number plate interest for listing {} by user {}", request.getListingId(), buyerUserId);

        Optional<NumberPlateListing> listingOpt = listingRepository.findByIdAndIsDeletedFalse(request.getListingId());
        if (listingOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(LISTING_NOT_FOUND).build();
        }

        NumberPlateListing listing = listingOpt.get();

        if (listing.getStatus() != NumberPlateListingStatus.ACTIVE) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("400")
                    .message("This listing is no longer active")
                    .build();
        }

        if (listing.getUserId().equals(buyerUserId)) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("400")
                    .message("You cannot express interest in your own listing")
                    .build();
        }

        boolean alreadyInterested = interestRepository.existsByBuyerUserIdAndListingIdAndStatusInAndIsDeletedFalse(
                buyerUserId, listing.getId(),
                List.of(NumberPlateInterestStatus.PENDING, NumberPlateInterestStatus.ACCEPTED));
        if (alreadyInterested) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("400")
                    .message("You already have an active interest in this listing")
                    .build();
        }

        NumberPlateInterest interest = interestMapper.toEntity(request, buyerUserId, listing.getUserId());
        NumberPlateInterest savedInterest = interestRepository.save(interest);

        log.info("Number plate interest created with ID: {}", savedInterest.getId());

        notificationService.sendInterestCreatedNotification(savedInterest, listing);

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("201")
                .message("Interest sent to the seller")
                .body(interestMapper.toResponse(savedInterest, listing))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse acceptInterest(Long interestId, Integer sellerUserId) {
        log.info("Accepting number plate interest {} by user {}", interestId, sellerUserId);

        Optional<NumberPlateInterest> interestOpt = interestRepository.findByIdAndIsDeletedFalse(interestId);
        if (interestOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(INTEREST_NOT_FOUND).build();
        }

        NumberPlateInterest interest = interestOpt.get();

        if (!interest.getSellerUserId().equals(sellerUserId)) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("403")
                    .message("Only the seller can respond to this interest")
                    .build();
        }

        if (interest.getStatus() != NumberPlateInterestStatus.PENDING) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("400")
                    .message("This interest has already been responded to")
                    .build();
        }

        // Accepting an interest deliberately does NOT close the listing or touch any other
        // pending interests on it - the seller decides separately when to mark it sold, and may
        // still be weighing multiple buyers.
        interest.setStatus(NumberPlateInterestStatus.ACCEPTED);
        interest.setRespondedAt(LocalDateTime.now());
        NumberPlateInterest savedInterest = interestRepository.save(interest);

        log.info("Number plate interest {} accepted by user {}", interestId, sellerUserId);

        NumberPlateListing listing = listingRepository.findById(interest.getListingId()).orElse(null);

        notificationService.sendInterestAcceptedNotification(savedInterest, listing);

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Interest accepted. Contact details have been shared with both parties.")
                .body(interestMapper.toResponse(savedInterest, listing))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse rejectInterest(Long interestId, Integer sellerUserId, NumberPlateInterestRejectionRequest request) {
        log.info("Rejecting number plate interest {} by user {}", interestId, sellerUserId);

        Optional<NumberPlateInterest> interestOpt = interestRepository.findByIdAndIsDeletedFalse(interestId);
        if (interestOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(INTEREST_NOT_FOUND).build();
        }

        NumberPlateInterest interest = interestOpt.get();

        if (!interest.getSellerUserId().equals(sellerUserId)) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("403")
                    .message("Only the seller can respond to this interest")
                    .build();
        }

        if (interest.getStatus() != NumberPlateInterestStatus.PENDING) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("400")
                    .message("This interest has already been responded to")
                    .build();
        }

        interest.setStatus(NumberPlateInterestStatus.REJECTED);
        interest.setRespondedAt(LocalDateTime.now());
        if (request != null) {
            interest.setRejectionReason(request.getRejectionReason());
        }
        NumberPlateInterest savedInterest = interestRepository.save(interest);

        log.info("Number plate interest {} rejected by user {}", interestId, sellerUserId);

        NumberPlateListing listing = listingRepository.findById(interest.getListingId()).orElse(null);

        notificationService.sendInterestRejectedNotification(savedInterest, listing);

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Interest declined")
                .body(interestMapper.toResponse(savedInterest, listing))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse cancelInterest(Long interestId, Integer buyerUserId) {
        log.info("Cancelling number plate interest {} by user {}", interestId, buyerUserId);

        Optional<NumberPlateInterest> interestOpt = interestRepository.findByIdAndIsDeletedFalse(interestId);
        if (interestOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(INTEREST_NOT_FOUND).build();
        }

        NumberPlateInterest interest = interestOpt.get();

        if (!interest.getBuyerUserId().equals(buyerUserId)) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("403")
                    .message("Only the buyer can cancel this interest")
                    .build();
        }

        if (interest.getStatus() != NumberPlateInterestStatus.PENDING) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("400")
                    .message("Only pending interests can be cancelled")
                    .build();
        }

        interest.setStatus(NumberPlateInterestStatus.CANCELLED);
        interest.setRespondedAt(LocalDateTime.now());
        NumberPlateInterest savedInterest = interestRepository.save(interest);

        log.info("Number plate interest {} cancelled by user {}", interestId, buyerUserId);

        NumberPlateListing listing = listingRepository.findById(interest.getListingId()).orElse(null);

        notificationService.sendInterestCancelledNotification(savedInterest, listing);

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Interest cancelled")
                .body(interestMapper.toResponse(savedInterest, listing))
                .build();
    }

    @Override
    public GenericResponse getInterestById(Long interestId, Integer userId) {
        Optional<NumberPlateInterest> interestOpt = interestRepository.findByIdAndIsDeletedFalse(interestId);
        if (interestOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(INTEREST_NOT_FOUND).build();
        }

        NumberPlateInterest interest = interestOpt.get();

        if (!interest.getBuyerUserId().equals(userId) && !interest.getSellerUserId().equals(userId)) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("403")
                    .message("You are not a participant in this interest")
                    .build();
        }

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Interest retrieved successfully")
                .body(mapInterest(interest))
                .build();
    }

    @Override
    public GenericResponse getMyInterests(Integer userId, NumberPlateInterestStatus statusFilter) {
        List<NumberPlateInterest> interests = statusFilter != null
                ? interestRepository.findAllForUserAndStatus(userId, statusFilter)
                : interestRepository.findAllForUser(userId);

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Interests retrieved successfully")
                .body(interests.stream().map(this::mapInterest).toList())
                .build();
    }

    @Override
    public GenericResponse getInterestsForListing(Long listingId, Integer sellerUserId) {
        Optional<NumberPlateListing> listingOpt = listingRepository.findByIdAndIsDeletedFalse(listingId);
        if (listingOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(LISTING_NOT_FOUND).build();
        }

        NumberPlateListing listing = listingOpt.get();

        if (!listing.getUserId().equals(sellerUserId)) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("403")
                    .message("You can only view interest on your own listing")
                    .build();
        }

        List<NumberPlateInterest> interests = interestRepository.findByListingIdAndIsDeletedFalseOrderByCreatedAtDesc(listingId);

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Interests retrieved successfully")
                .body(interests.stream().map(i -> interestMapper.toResponse(i, listing)).toList())
                .build();
    }

    private NumberPlateInterestResponse mapInterest(NumberPlateInterest interest) {
        NumberPlateListing listing = listingRepository.findById(interest.getListingId()).orElse(null);
        return interestMapper.toResponse(interest, listing);
    }
}
