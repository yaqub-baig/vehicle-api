package com.autoadda.apis.vehicle.mapper;

import com.autoadda.apis.vehicle.dao.MediaFileRepository;
import com.autoadda.apis.vehicle.entity.MediaFile;
import com.autoadda.apis.vehicle.entity.PlateSource;
import com.autoadda.apis.vehicle.exception.ResourceNotFoundException;
import com.autoadda.apis.vehicle.request.PlateSourceRequest;
import com.autoadda.apis.vehicle.response.PlateRefResponse;
import com.autoadda.apis.vehicle.response.PlateSourceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlateSourceMapper {

    private final MediaFileRepository mediaFileRepository;

    public PlateSource toEntity(PlateSourceRequest request) {
        return PlateSource.builder()
                .code(request.getCode())
                .name(request.getName())
                .displayOrder(request.getDisplayOrder())
                .image(resolveImage(request.getImageId()))
                .isActive(true)
                .build();
    }

    public void applyUpdate(PlateSource entity, PlateSourceRequest request) {
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setDisplayOrder(request.getDisplayOrder());
        entity.setImage(resolveImage(request.getImageId()));
    }

    public PlateSourceResponse toResponse(PlateSource entity) {
        if (entity == null) {
            return null;
        }
        return PlateSourceResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .displayOrder(entity.getDisplayOrder())
                .image(entity.getImage())
                .isActive(entity.getIsActive())
                .build();
    }

    public PlateRefResponse toRef(PlateSource entity) {
        if (entity == null) {
            return null;
        }
        return PlateRefResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .image(entity.getImage())
                .build();
    }

    private MediaFile resolveImage(Long imageId) {
        if (imageId == null) {
            return null;
        }
        return mediaFileRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Plate image not found"));
    }
}
