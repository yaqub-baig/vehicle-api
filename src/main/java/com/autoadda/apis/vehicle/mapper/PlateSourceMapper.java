package com.autoadda.apis.vehicle.mapper;

import com.autoadda.apis.vehicle.entity.PlateSource;
import com.autoadda.apis.vehicle.request.PlateSourceRequest;
import com.autoadda.apis.vehicle.response.PlateRefResponse;
import com.autoadda.apis.vehicle.response.PlateSourceResponse;
import org.springframework.stereotype.Component;

@Component
public class PlateSourceMapper {

    public PlateSource toEntity(PlateSourceRequest request) {
        return PlateSource.builder()
                .code(request.getCode())
                .name(request.getName())
                .displayOrder(request.getDisplayOrder())
                .isActive(true)
                .build();
    }

    public void applyUpdate(PlateSource entity, PlateSourceRequest request) {
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setDisplayOrder(request.getDisplayOrder());
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
                .build();
    }
}
