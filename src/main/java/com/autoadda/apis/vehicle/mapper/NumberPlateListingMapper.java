package com.autoadda.apis.vehicle.mapper;

import com.autoadda.apis.vehicle.clients.AuthClient;
import com.autoadda.apis.vehicle.dao.MediaFileRepository;
import com.autoadda.apis.vehicle.entity.MediaFile;
import com.autoadda.apis.vehicle.entity.NumberPlateListing;
import com.autoadda.apis.vehicle.enums.NumberPlateListingStatus;
import com.autoadda.apis.vehicle.request.NumberPlateListingRequest;
import com.autoadda.apis.vehicle.response.NumberPlateListingResponse;
import com.autoadda.apis.vehicle.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NumberPlateListingMapper {

    private final AuthClient authClient;
    private final MediaFileRepository mediaFileRepository;

    public NumberPlateListing toEntity(NumberPlateListingRequest request, Integer userId) {
        return NumberPlateListing.builder()
                .userId(userId)
                .plateNumber(request.getPlateNumber())
                .emirate(request.getEmirate())
                .category(request.getCategory())
                .expectedPrice(request.getExpectedPrice())
                .description(request.getDescription())
                .images(resolveImages(request.getImageIds()))
                .status(NumberPlateListingStatus.ACTIVE)
                .isDeleted(false)
                .build();
    }

    public void applyUpdate(NumberPlateListing listing, NumberPlateListingRequest request) {
        listing.setPlateNumber(request.getPlateNumber());
        listing.setEmirate(request.getEmirate());
        listing.setCategory(request.getCategory());
        listing.setExpectedPrice(request.getExpectedPrice());
        listing.setDescription(request.getDescription());
        if (request.getImageIds() != null) {
            listing.setImages(resolveImages(request.getImageIds()));
        }
    }

    public NumberPlateListingResponse toResponse(NumberPlateListing entity) {
        if (entity == null) {
            return null;
        }

        NumberPlateListingResponse.NumberPlateListingResponseBuilder builder = NumberPlateListingResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .plateNumber(entity.getPlateNumber())
                .emirate(entity.getEmirate())
                .category(entity.getCategory())
                .expectedPrice(entity.getExpectedPrice())
                .description(entity.getDescription())
                .images(entity.getImages())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        try {
            UserResponse seller = authClient.getUser(entity.getUserId());
            if (seller != null) {
                builder.sellerName(seller.getName());
            }
        } catch (Exception e) {
            log.warn("Failed to fetch seller name for user {}: {}", entity.getUserId(), e.getMessage());
        }

        return builder.build();
    }

    public List<NumberPlateListingResponse> toResponseList(List<NumberPlateListing> entities) {
        return entities.stream().map(this::toResponse).toList();
    }

    private List<MediaFile> resolveImages(List<Long> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) {
            return null;
        }
        return mediaFileRepository.findAllById(imageIds);
    }
}
