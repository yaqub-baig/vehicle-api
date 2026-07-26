package com.autoadda.apis.vehicle.mapper;

import com.autoadda.apis.vehicle.clients.AuthClient;
import com.autoadda.apis.vehicle.entity.CarpoolListing;
import com.autoadda.apis.vehicle.enums.CarpoolListingStatus;
import com.autoadda.apis.vehicle.request.CarpoolListingRequest;
import com.autoadda.apis.vehicle.response.CarpoolListingResponse;
import com.autoadda.apis.vehicle.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CarpoolListingMapper {

    private final AuthClient authClient;

    public CarpoolListing toEntity(CarpoolListingRequest request, Integer userId) {
        return CarpoolListing.builder()
                .userId(userId)
                .type(request.getType())
                .originCity(request.getOriginCity())
                .destinationCity(request.getDestinationCity())
                .travelDateTime(request.getTravelDateTime())
                .budget(request.getBudget())
                .availableSeats(request.getAvailableSeats())
                .carDescription(request.getCarDescription())
                .status(CarpoolListingStatus.ACTIVE)
                .isDeleted(false)
                .build();
    }

    public void applyUpdate(CarpoolListing entity, CarpoolListingRequest request) {
        entity.setType(request.getType());
        entity.setOriginCity(request.getOriginCity());
        entity.setDestinationCity(request.getDestinationCity());
        entity.setTravelDateTime(request.getTravelDateTime());
        entity.setBudget(request.getBudget());
        entity.setAvailableSeats(request.getAvailableSeats());
        entity.setCarDescription(request.getCarDescription());
    }

    public CarpoolListingResponse toResponse(CarpoolListing entity) {
        if (entity == null) {
            return null;
        }

        CarpoolListingResponse.CarpoolListingResponseBuilder builder = CarpoolListingResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .type(entity.getType())
                .originCity(entity.getOriginCity())
                .destinationCity(entity.getDestinationCity())
                .travelDateTime(entity.getTravelDateTime())
                .budget(entity.getBudget())
                .availableSeats(entity.getAvailableSeats())
                .carDescription(entity.getCarDescription())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        try {
            UserResponse poster = authClient.getUser(entity.getUserId());
            if (poster != null) {
                builder.posterName(poster.getName());
            }
        } catch (Exception e) {
            log.warn("Failed to fetch poster details for ID {}: {}", entity.getUserId(), e.getMessage());
        }

        return builder.build();
    }

    public List<CarpoolListingResponse> toResponseList(List<CarpoolListing> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream().map(this::toResponse).toList();
    }
}
