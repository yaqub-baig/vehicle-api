package com.autoadda.apis.vehicle.mapper;

import com.autoadda.apis.vehicle.dao.StateTerritoryRepository;
import com.autoadda.apis.vehicle.entity.StateTerritory;
import com.autoadda.apis.vehicle.entity.TerritoryArea;
import com.autoadda.apis.vehicle.exception.ResourceNotFoundException;
import com.autoadda.apis.vehicle.request.TerritoryAreaRequest;
import com.autoadda.apis.vehicle.response.TerritoryAreaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TerritoryAreaMapper {

    private final StateTerritoryRepository stateTerritoryRepository;
    private final StateTerritoryMapper stateTerritoryMapper;

    public TerritoryArea toEntity(TerritoryAreaRequest request) {
        return TerritoryArea.builder()
                .code(request.getCode())
                .name(request.getName())
                .stateTerritory(resolveStateTerritory(request.getStateTerritoryId()))
                .isActive(true)
                .build();
    }

    public void applyUpdate(TerritoryArea entity, TerritoryAreaRequest request) {
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setStateTerritory(resolveStateTerritory(request.getStateTerritoryId()));
    }

    public TerritoryAreaResponse toResponse(TerritoryArea entity) {
        if (entity == null) {
            return null;
        }
        return TerritoryAreaResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .isActive(entity.getIsActive())
                .stateTerritory(stateTerritoryMapper.toResponse(entity.getStateTerritory()))
                .build();
    }

    private StateTerritory resolveStateTerritory(Long stateTerritoryId) {
        return stateTerritoryRepository.findById(stateTerritoryId)
                .orElseThrow(() -> new ResourceNotFoundException("State/territory not found"));
    }
}
