package com.autoadda.apis.vehicle.mapper;

import com.autoadda.apis.vehicle.dao.PlateTypeRepository;
import com.autoadda.apis.vehicle.entity.PlateCode;
import com.autoadda.apis.vehicle.entity.PlateType;
import com.autoadda.apis.vehicle.exception.ResourceNotFoundException;
import com.autoadda.apis.vehicle.request.PlateCodeRequest;
import com.autoadda.apis.vehicle.response.PlateCodeResponse;
import com.autoadda.apis.vehicle.response.PlateRefResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlateCodeMapper {

    private final PlateTypeRepository plateTypeRepository;
    private final PlateTypeMapper plateTypeMapper;

    public PlateCode toEntity(PlateCodeRequest request) {
        return PlateCode.builder()
                .code(request.getCode())
                .plateType(resolvePlateType(request.getPlateTypeId()))
                .colorHex(request.getColorHex())
                .displayOrder(request.getDisplayOrder())
                .isActive(true)
                .build();
    }

    public void applyUpdate(PlateCode entity, PlateCodeRequest request) {
        entity.setCode(request.getCode());
        entity.setPlateType(resolvePlateType(request.getPlateTypeId()));
        entity.setColorHex(request.getColorHex());
        entity.setDisplayOrder(request.getDisplayOrder());
    }

    public PlateCodeResponse toResponse(PlateCode entity) {
        if (entity == null) {
            return null;
        }
        return PlateCodeResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .colorHex(entity.getColorHex())
                .displayOrder(entity.getDisplayOrder())
                .isActive(entity.getIsActive())
                .plateType(plateTypeMapper.toResponse(entity.getPlateType()))
                .build();
    }

    // Plate codes have no separate display name - the code itself is shown.
    public PlateRefResponse toRef(PlateCode entity) {
        if (entity == null) {
            return null;
        }
        return PlateRefResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getCode())
                .colorHex(entity.getColorHex())
                .build();
    }

    private PlateType resolvePlateType(Long plateTypeId) {
        return plateTypeRepository.findById(plateTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Plate type not found"));
    }
}
