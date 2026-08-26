package com.autoadda.apis.vehicle.mapper;

import com.autoadda.apis.vehicle.clients.AuthClient;
import com.autoadda.apis.vehicle.entity.CarRentalListing;
import com.autoadda.apis.vehicle.entity.RentalRequest;
import com.autoadda.apis.vehicle.entity.VehicleDetails;
import com.autoadda.apis.vehicle.enums.RentalRequestStatus;
import com.autoadda.apis.vehicle.request.RentalRequestCreateRequest;
import com.autoadda.apis.vehicle.response.RentalRequestResponse;
import com.autoadda.apis.vehicle.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RentalRequestMapper {

    private final AuthClient authClient;
    private final CarRentalListingMapper listingMapper;

    public RentalRequest toEntity(RentalRequestCreateRequest request, CarRentalListing listing, Integer renterId) {
        return RentalRequest.builder()
                .listingId(listing.getId())
                .vehicleId(listing.getVehicleId())
                .renterId(renterId)
                .ownerId(listing.getOwnerId())
                .requestedRateType(request.getRequestedRateType())
                .rentalType(request.getRentalType())
                .durationValue(request.getDurationValue())
                .message(request.getMessage())
                .status(RentalRequestStatus.PENDING)
                .isDeleted(false)
                .build();
    }

    public RentalRequestResponse toResponse(RentalRequest entity, CarRentalListing listing) {
        return toResponse(entity, listing, null);
    }

    public RentalRequestResponse toResponse(RentalRequest entity, CarRentalListing listing, VehicleDetails vehicle) {
        if (entity == null) {
            return null;
        }

        RentalRequestResponse.RentalRequestResponseBuilder builder = RentalRequestResponse.builder()
                .id(entity.getId())
                .listingId(entity.getListingId())
                .vehicleId(entity.getVehicleId())
                .renterId(entity.getRenterId())
                .ownerId(entity.getOwnerId())
                .requestedRateType(entity.getRequestedRateType())
                .rentalType(entity.getRentalType())
                .durationValue(entity.getDurationValue())
                .message(entity.getMessage())
                .status(entity.getStatus())
                .rejectionReason(entity.getRejectionReason())
                .respondedAt(entity.getRespondedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .listing(listingMapper.toResponse(listing, vehicle));

        if (entity.getStatus() == RentalRequestStatus.ACCEPTED) {
            try {
                UserResponse renter = authClient.getUser(entity.getRenterId());
                if (renter != null) {
                    builder.renterContactEmail(renter.getEmail());
                    builder.renterContactPhone(renter.getMobileNumber());
                }
            } catch (Exception e) {
                log.warn("Failed to fetch renter contact for user {}: {}", entity.getRenterId(), e.getMessage());
            }

            try {
                UserResponse owner = authClient.getUser(entity.getOwnerId());
                if (owner != null) {
                    builder.ownerContactEmail(owner.getEmail());
                    builder.ownerContactPhone(owner.getMobileNumber());
                }
            } catch (Exception e) {
                log.warn("Failed to fetch owner contact for user {}: {}", entity.getOwnerId(), e.getMessage());
            }
        }

        return builder.build();
    }
}
