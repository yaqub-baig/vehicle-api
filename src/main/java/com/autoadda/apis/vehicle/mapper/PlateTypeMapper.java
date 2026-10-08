package com.autoadda.apis.vehicle.mapper;

import com.autoadda.apis.vehicle.dao.PlateSourceRepository;
import com.autoadda.apis.vehicle.entity.PlateSource;
import com.autoadda.apis.vehicle.entity.PlateType;
import com.autoadda.apis.vehicle.exception.ResourceNotFoundException;
import com.autoadda.apis.vehicle.request.PlateTypeRequest;
import com.autoadda.apis.vehicle.response.PlateRefResponse;
import com.autoadda.apis.vehicle.response.PlateTypeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlateTypeMapper {

    private final PlateSourceRepository plateSourceRepository;
    private final PlateSourceMapper plateSourceMapper;

    public PlateType toEntity(PlateTypeRequest request) {
        return PlateType.builder()
                .code(request.getCode())
                .name(request.getName())
                .plateSource(resolvePlateSource(request.getPlateSourceId()))
                .displayOrder(request.getDisplayOrder())
                .isActive(true)
                .build();
    }

    public void applyUpdate(PlateType entity, PlateTypeRequest request) {
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setPlateSource(resolvePlateSource(request.getPlateSourceId()));
        entity.setDisplayOrder(request.getDisplayOrder());
    }

    public PlateTypeResponse toResponse(PlateType entity) {
        if (entity == null) {
            return null;
        }
        return PlateTypeResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .displayOrder(entity.getDisplayOrder())
                .isActive(entity.getIsActive())
                .plateSource(plateSourceMapper.toResponse(entity.getPlateSource()))
                .build();
    }

    public PlateRefResponse toRef(PlateType entity) {
        if (entity == null) {
            return null;
        }
        return PlateRefResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .build();
    }

    private PlateSource resolvePlateSource(Long plateSourceId) {
        return plateSourceRepository.findById(plateSourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Plate source not found"));
    }
}
