package com.autoadda.apis.vehicle.mapper;

import com.autoadda.apis.vehicle.dao.TerritoryAreaRepository;
import com.autoadda.apis.vehicle.entity.Street;
import com.autoadda.apis.vehicle.entity.TerritoryArea;
import com.autoadda.apis.vehicle.exception.ResourceNotFoundException;
import com.autoadda.apis.vehicle.request.StreetRequest;
import com.autoadda.apis.vehicle.response.StreetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StreetMapper {

    private final TerritoryAreaRepository territoryAreaRepository;
    private final TerritoryAreaMapper territoryAreaMapper;

    public Street toEntity(StreetRequest request) {
        return Street.builder()
                .code(request.getCode())
                .name(request.getName())
                .territoryArea(resolveTerritoryArea(request.getTerritoryAreaId()))
                .isActive(true)
                .build();
    }

    public void applyUpdate(Street entity, StreetRequest request) {
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setTerritoryArea(resolveTerritoryArea(request.getTerritoryAreaId()));
    }

    public StreetResponse toResponse(Street entity) {
        if (entity == null) {
            return null;
        }
        return StreetResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .isActive(entity.getIsActive())
                .territoryArea(territoryAreaMapper.toResponse(entity.getTerritoryArea()))
                .build();
    }

    private TerritoryArea resolveTerritoryArea(Long territoryAreaId) {
        return territoryAreaRepository.findById(territoryAreaId)
                .orElseThrow(() -> new ResourceNotFoundException("Territory area not found"));
    }
}
