package com.autoadda.apis.vehicle.service.impl;

import com.autoadda.apis.vehicle.dao.CarpoolConnectionRepository;
import com.autoadda.apis.vehicle.dao.CarpoolListingRepository;
import com.autoadda.apis.vehicle.entity.CarpoolConnection;
import com.autoadda.apis.vehicle.entity.CarpoolListing;
import com.autoadda.apis.vehicle.enums.CarpoolConnectionStatus;
import com.autoadda.apis.vehicle.enums.CarpoolListingStatus;
import com.autoadda.apis.vehicle.enums.CarpoolListingType;
import com.autoadda.apis.vehicle.mapper.CarpoolConnectionMapper;
import com.autoadda.apis.vehicle.request.CarpoolConnectionRejectionRequest;
import com.autoadda.apis.vehicle.request.CarpoolConnectionRequest;
import com.autoadda.apis.vehicle.response.CarpoolConnectionResponse;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.CarpoolConnectionService;
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
public class CarpoolConnectionServiceImpl implements CarpoolConnectionService {

    private static final String ERROR = "error";
    private static final String SUCCESS = "success";
    private static final String CONNECTION_NOT_FOUND = "Carpool connection request not found";

    private final CarpoolConnectionRepository connectionRepository;
    private final CarpoolListingRepository listingRepository;
    private final CarpoolConnectionMapper connectionMapper;
    private final CarpoolNotificationService notificationService;

    @Override
    @Transactional
    public GenericResponse createConnectionRequest(CarpoolConnectionRequest request, Integer requesterUserId) {
        log.info("Creating carpool connection request from listing {} to listing {} by user {}",
                request.getRequesterListingId(), request.getTargetListingId(), requesterUserId);

        Optional<CarpoolListing> requesterListingOpt =
                listingRepository.findByIdAndIsDeletedFalse(request.getRequesterListingId());
        if (requesterListingOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message("Your listing was not found").build();
        }

        Optional<CarpoolListing> targetListingOpt =
                listingRepository.findByIdAndIsDeletedFalse(request.getTargetListingId());
        if (targetListingOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message("Target listing was not found").build();
        }

        CarpoolListing requesterListing = requesterListingOpt.get();
        CarpoolListing targetListing = targetListingOpt.get();

        if (!requesterListing.getUserId().equals(requesterUserId)) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("403")
                    .message("You can only connect using your own listing")
                    .build();
        }

        if (requesterListing.getUserId().equals(targetListing.getUserId())) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("400")
                    .message("Cannot connect to your own listing")
                    .build();
        }

        if (requesterListing.getType() == targetListing.getType()) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("400")
                    .message("Can only connect to the opposite listing type")
                    .build();
        }

        if (requesterListing.getStatus() != CarpoolListingStatus.ACTIVE
                || targetListing.getStatus() != CarpoolListingStatus.ACTIVE) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("400")
                    .message("One of the listings is no longer active")
                    .build();
        }

        if (!listingsAreCompatible(requesterListing, targetListing)) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("400")
                    .message("These listings are not compatible for connecting")
                    .build();
        }

        boolean alreadyRequested = connectionRepository.existsByRequesterListingIdAndTargetListingIdAndStatusInAndIsDeletedFalse(
                requesterListing.getId(), targetListing.getId(),
                List.of(CarpoolConnectionStatus.PENDING, CarpoolConnectionStatus.ACCEPTED));
        if (alreadyRequested) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("400")
                    .message("A connection request already exists between these listings")
                    .build();
        }

        CarpoolConnection connection = connectionMapper.toEntity(request, requesterListing, targetListing);
        CarpoolConnection savedConnection = connectionRepository.save(connection);

        log.info("Carpool connection request created with ID: {}", savedConnection.getId());

        notificationService.sendConnectionRequestNotification(savedConnection, requesterListing, targetListing);

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("201")
                .message("Connection request sent")
                .body(connectionMapper.toResponse(savedConnection, requesterListing, targetListing))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse acceptConnection(Long connectionId, Integer userId) {
        log.info("Accepting carpool connection {} by user {}", connectionId, userId);

        Optional<CarpoolConnection> connectionOpt = connectionRepository.findByIdAndIsDeletedFalse(connectionId);
        if (connectionOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(CONNECTION_NOT_FOUND).build();
        }

        CarpoolConnection connection = connectionOpt.get();

        if (!connection.getTargetUserId().equals(userId)) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("403")
                    .message("Only the recipient can respond to this connection request")
                    .build();
        }

        if (connection.getStatus() != CarpoolConnectionStatus.PENDING) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("400")
                    .message("This connection request has already been responded to")
                    .build();
        }

        connection.setStatus(CarpoolConnectionStatus.ACCEPTED);
        connection.setRespondedAt(LocalDateTime.now());
        CarpoolConnection savedConnection = connectionRepository.save(connection);

        log.info("Carpool connection {} accepted by user {}", connectionId, userId);

        CarpoolListing requesterListing = listingRepository.findById(connection.getRequesterListingId()).orElse(null);
        CarpoolListing targetListing = listingRepository.findById(connection.getTargetListingId()).orElse(null);

        notificationService.sendConnectionAcceptedNotification(savedConnection, requesterListing, targetListing);

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Connection accepted. Contact details have been shared with both parties.")
                .body(connectionMapper.toResponse(savedConnection, requesterListing, targetListing))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse rejectConnection(Long connectionId, Integer userId, CarpoolConnectionRejectionRequest request) {
        log.info("Rejecting carpool connection {} by user {}", connectionId, userId);

        Optional<CarpoolConnection> connectionOpt = connectionRepository.findByIdAndIsDeletedFalse(connectionId);
        if (connectionOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(CONNECTION_NOT_FOUND).build();
        }

        CarpoolConnection connection = connectionOpt.get();

        if (!connection.getTargetUserId().equals(userId)) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("403")
                    .message("Only the recipient can respond to this connection request")
                    .build();
        }

        if (connection.getStatus() != CarpoolConnectionStatus.PENDING) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("400")
                    .message("This connection request has already been responded to")
                    .build();
        }

        connection.setStatus(CarpoolConnectionStatus.REJECTED);
        connection.setRespondedAt(LocalDateTime.now());
        if (request != null) {
            connection.setRejectionReason(request.getRejectionReason());
        }
        CarpoolConnection savedConnection = connectionRepository.save(connection);

        log.info("Carpool connection {} rejected by user {}", connectionId, userId);

        CarpoolListing requesterListing = listingRepository.findById(connection.getRequesterListingId()).orElse(null);
        CarpoolListing targetListing = listingRepository.findById(connection.getTargetListingId()).orElse(null);

        notificationService.sendConnectionRejectedNotification(savedConnection, requesterListing, targetListing);

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Connection request declined")
                .body(connectionMapper.toResponse(savedConnection, requesterListing, targetListing))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse cancelConnection(Long connectionId, Integer userId) {
        log.info("Cancelling carpool connection {} by user {}", connectionId, userId);

        Optional<CarpoolConnection> connectionOpt = connectionRepository.findByIdAndIsDeletedFalse(connectionId);
        if (connectionOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(CONNECTION_NOT_FOUND).build();
        }

        CarpoolConnection connection = connectionOpt.get();

        if (!connection.getRequesterUserId().equals(userId)) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("403")
                    .message("Only the requester can cancel this connection request")
                    .build();
        }

        if (connection.getStatus() != CarpoolConnectionStatus.PENDING) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("400")
                    .message("Only pending connection requests can be cancelled")
                    .build();
        }

        connection.setStatus(CarpoolConnectionStatus.CANCELLED);
        connection.setRespondedAt(LocalDateTime.now());
        CarpoolConnection savedConnection = connectionRepository.save(connection);

        log.info("Carpool connection {} cancelled by user {}", connectionId, userId);

        CarpoolListing requesterListing = listingRepository.findById(connection.getRequesterListingId()).orElse(null);
        CarpoolListing targetListing = listingRepository.findById(connection.getTargetListingId()).orElse(null);

        notificationService.sendConnectionCancelledNotification(savedConnection, requesterListing, targetListing);

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Connection request cancelled")
                .body(connectionMapper.toResponse(savedConnection, requesterListing, targetListing))
                .build();
    }

    @Override
    public GenericResponse getConnectionById(Long connectionId, Integer userId) {
        Optional<CarpoolConnection> connectionOpt = connectionRepository.findByIdAndIsDeletedFalse(connectionId);
        if (connectionOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(CONNECTION_NOT_FOUND).build();
        }

        CarpoolConnection connection = connectionOpt.get();

        if (!connection.getRequesterUserId().equals(userId) && !connection.getTargetUserId().equals(userId)) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("403")
                    .message("You are not a participant in this connection request")
                    .build();
        }

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Connection retrieved successfully")
                .body(mapConnection(connection))
                .build();
    }

    @Override
    public GenericResponse getMyConnections(Integer userId, CarpoolConnectionStatus statusFilter) {
        List<CarpoolConnection> connections = statusFilter != null
                ? connectionRepository.findAllForUserAndStatus(userId, statusFilter)
                : connectionRepository.findAllForUser(userId);

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Connections retrieved successfully")
                .body(connections.stream().map(this::mapConnection).toList())
                .build();
    }

    private boolean listingsAreCompatible(CarpoolListing requesterListing, CarpoolListing targetListing) {
        CarpoolListing offerListing = requesterListing.getType() == CarpoolListingType.OFFER
                ? requesterListing : targetListing;
        CarpoolListing seekerListing = requesterListing.getType() == CarpoolListingType.SEEKER
                ? requesterListing : targetListing;

        boolean citiesMatch = offerListing.getOriginCity().equalsIgnoreCase(seekerListing.getOriginCity())
                && offerListing.getDestinationCity().equalsIgnoreCase(seekerListing.getDestinationCity());
        boolean budgetCompatible = seekerListing.getBudget().compareTo(offerListing.getBudget()) >= 0;

        return citiesMatch && budgetCompatible;
    }

    private CarpoolConnectionResponse mapConnection(CarpoolConnection connection) {
        CarpoolListing requesterListing = listingRepository.findById(connection.getRequesterListingId()).orElse(null);
        CarpoolListing targetListing = listingRepository.findById(connection.getTargetListingId()).orElse(null);
        return connectionMapper.toResponse(connection, requesterListing, targetListing);
    }
}
