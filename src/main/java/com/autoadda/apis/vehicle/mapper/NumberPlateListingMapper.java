package com.autoadda.apis.vehicle.mapper;

import com.autoadda.apis.vehicle.clients.AuthClient;
import com.autoadda.apis.vehicle.dao.MediaFileRepository;
import com.autoadda.apis.vehicle.dao.PlateCodeRepository;
import com.autoadda.apis.vehicle.dao.PlateSourceRepository;
import com.autoadda.apis.vehicle.dao.PlateTypeRepository;
import com.autoadda.apis.vehicle.entity.MediaFile;
import com.autoadda.apis.vehicle.entity.NumberPlateListing;
import com.autoadda.apis.vehicle.entity.PlateCode;
import com.autoadda.apis.vehicle.entity.PlateSource;
import com.autoadda.apis.vehicle.entity.PlateType;
import com.autoadda.apis.vehicle.enums.NumberPlateListingStatus;
import com.autoadda.apis.vehicle.exception.InvalidInputException;
import com.autoadda.apis.vehicle.exception.ResourceNotFoundException;
import com.autoadda.apis.vehicle.request.NumberPlateListingRequest;
import com.autoadda.apis.vehicle.response.NumberPlateListingResponse;
import com.autoadda.apis.vehicle.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NumberPlateListingMapper {

    private final AuthClient authClient;
    private final MediaFileRepository mediaFileRepository;
    private final PlateSourceRepository plateSourceRepository;
    private final PlateTypeRepository plateTypeRepository;
    private final PlateCodeRepository plateCodeRepository;
    private final PlateSourceMapper plateSourceMapper;
    private final PlateTypeMapper plateTypeMapper;
    private final PlateCodeMapper plateCodeMapper;

    public NumberPlateListing toEntity(NumberPlateListingRequest request, Integer userId) {
        PlateSelection plate = resolvePlate(request);
        return NumberPlateListing.builder()
                .userId(userId)
                .plateNumber(request.getPlateNumber())
                .plateSource(plate.source())
                .plateType(plate.type())
                .plateCode(plate.code())
                .expectedPrice(request.getExpectedPrice())
                .description(request.getDescription())
                .images(resolveImages(request.getImageIds()))
                .status(NumberPlateListingStatus.ACTIVE)
                .isDeleted(false)
                .build();
    }

    public void applyUpdate(NumberPlateListing listing, NumberPlateListingRequest request) {
        PlateSelection plate = resolvePlate(request);
        listing.setPlateNumber(request.getPlateNumber());
        listing.setPlateSource(plate.source());
        listing.setPlateType(plate.type());
        listing.setPlateCode(plate.code());
        listing.setExpectedPrice(request.getExpectedPrice());
        listing.setDescription(request.getDescription());
        if (request.getImageIds() != null) {
            // images has orphanRemoval: replace the contents of the managed collection, never the collection itself
            List<MediaFile> images = resolveImages(request.getImageIds());
            if (listing.getImages() == null) {
                listing.setImages(new ArrayList<>());
            }
            listing.getImages().clear();
            if (images != null) {
                listing.getImages().addAll(images);
            }
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
                .plateSource(plateSourceMapper.toRef(entity.getPlateSource()))
                .plateType(plateTypeMapper.toRef(entity.getPlateType()))
                .plateCode(plateCodeMapper.toRef(entity.getPlateCode()))
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

    private record PlateSelection(PlateSource source, PlateType type, PlateCode code) {
    }

    // Resolves the source -> type -> code ids and checks they form a valid chain: the type must
    // belong to the source, and a code is required exactly when the type has active codes.
    private PlateSelection resolvePlate(NumberPlateListingRequest request) {
        PlateSource source = plateSourceRepository.findById(request.getPlateSourceId())
                .filter(p -> Boolean.TRUE.equals(p.getIsActive()))
                .orElseThrow(() -> new ResourceNotFoundException("Plate source not found"));
        PlateType type = plateTypeRepository.findById(request.getPlateTypeId())
                .filter(p -> Boolean.TRUE.equals(p.getIsActive()))
                .orElseThrow(() -> new ResourceNotFoundException("Plate type not found"));
        if (!type.getPlateSource().getId().equals(source.getId())) {
            throw new InvalidInputException("Plate type does not belong to the selected plate source");
        }

        boolean typeHasCodes = plateCodeRepository.existsByPlateTypeIdAndIsActiveTrue(type.getId());
        if (request.getPlateCodeId() == null) {
            if (typeHasCodes) {
                throw new InvalidInputException("Plate code is required for this plate type");
            }
            return new PlateSelection(source, type, null);
        }
        if (!typeHasCodes) {
            throw new InvalidInputException("This plate type does not use plate codes");
        }

        PlateCode code = plateCodeRepository.findById(request.getPlateCodeId())
                .filter(p -> Boolean.TRUE.equals(p.getIsActive()))
                .orElseThrow(() -> new ResourceNotFoundException("Plate code not found"));
        if (!code.getPlateType().getId().equals(type.getId())) {
            throw new InvalidInputException("Plate code does not belong to the selected plate type");
        }
        return new PlateSelection(source, type, code);
    }

    private List<MediaFile> resolveImages(List<Long> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) {
            return null;
        }
        return mediaFileRepository.findAllById(imageIds);
    }
}
