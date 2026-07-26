package com.autoadda.apis.vehicle.mapper;

import com.autoadda.apis.vehicle.clients.AuthClient;
import com.autoadda.apis.vehicle.entity.CarpoolConnection;
import com.autoadda.apis.vehicle.entity.CarpoolListing;
import com.autoadda.apis.vehicle.enums.CarpoolConnectionStatus;
import com.autoadda.apis.vehicle.request.CarpoolConnectionRequest;
import com.autoadda.apis.vehicle.response.CarpoolConnectionResponse;
import com.autoadda.apis.vehicle.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CarpoolConnectionMapper {

    private final AuthClient authClient;
    private final CarpoolListingMapper listingMapper;

    public CarpoolConnection toEntity(CarpoolConnectionRequest request, CarpoolListing requesterListing,
                                       CarpoolListing targetListing) {
        return CarpoolConnection.builder()
                .requesterListingId(requesterListing.getId())
                .requesterUserId(requesterListing.getUserId())
                .targetListingId(targetListing.getId())
                .targetUserId(targetListing.getUserId())
                .message(request.getMessage())
                .status(CarpoolConnectionStatus.PENDING)
                .isDeleted(false)
                .build();
    }

    public CarpoolConnectionResponse toResponse(CarpoolConnection entity, CarpoolListing requesterListing,
                                                 CarpoolListing targetListing) {
        if (entity == null) {
            return null;
        }

        CarpoolConnectionResponse.CarpoolConnectionResponseBuilder builder = CarpoolConnectionResponse.builder()
                .id(entity.getId())
                .status(entity.getStatus())
                .message(entity.getMessage())
                .rejectionReason(entity.getRejectionReason())
                .respondedAt(entity.getRespondedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .requesterListing(listingMapper.toResponse(requesterListing))
                .targetListing(listingMapper.toResponse(targetListing));

        if (entity.getStatus() == CarpoolConnectionStatus.ACCEPTED) {
            try {
                UserResponse requesterUser = authClient.getUser(entity.getRequesterUserId());
                if (requesterUser != null) {
                    builder.requesterContactEmail(requesterUser.getEmail());
                    builder.requesterContactPhone(requesterUser.getMobileNumber());
                }
            } catch (Exception e) {
                log.warn("Failed to fetch requester contact for user {}: {}",
                        entity.getRequesterUserId(), e.getMessage());
            }

            try {
                UserResponse targetUser = authClient.getUser(entity.getTargetUserId());
                if (targetUser != null) {
                    builder.targetContactEmail(targetUser.getEmail());
                    builder.targetContactPhone(targetUser.getMobileNumber());
                }
            } catch (Exception e) {
                log.warn("Failed to fetch target contact for user {}: {}",
                        entity.getTargetUserId(), e.getMessage());
            }
        }

        return builder.build();
    }
}
