package com.autoadda.apis.vehicle.service.impl;

import com.autoadda.apis.vehicle.dao.TerritoryAreaRepository;
import com.autoadda.apis.vehicle.entity.TerritoryArea;
import com.autoadda.apis.vehicle.exception.ConflictException;
import com.autoadda.apis.vehicle.exception.ResourceNotFoundException;
import com.autoadda.apis.vehicle.mapper.TerritoryAreaMapper;
import com.autoadda.apis.vehicle.request.TerritoryAreaRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TerritoryAreaService {

    private static final String SUCCESS = "success";
    private static final String TERRITORY_AREA_NOT_FOUND = "Territory area not found";

    private final TerritoryAreaRepository territoryAreaRepository;
    private final TerritoryAreaMapper territoryAreaMapper;

    @Transactional
    public GenericResponse createTerritoryArea(TerritoryAreaRequest request) {
        territoryAreaRepository.findByStateTerritoryIdAndCodeIgnoreCase(request.getStateTerritoryId(), request.getCode())
                .ifPresent(existing -> {
                    throw new ConflictException("A territory area with this code already exists for this state/territory");
                });
        TerritoryArea saved = territoryAreaRepository.save(territoryAreaMapper.toEntity(request));
        return GenericResponse.builder()
                .status(SUCCESS)
                .code("201")
                .message("Territory area created successfully")
                .body(territoryAreaMapper.toResponse(saved))
                .build();
    }

    public GenericResponse getAllTerritoryAreas(Long stateTerritoryId) {
        List<TerritoryArea> territoryAreas = stateTerritoryId != null
                ? territoryAreaRepository.findByStateTerritoryIdAndIsActiveTrue(stateTerritoryId)
                : territoryAreaRepository.findByIsActiveTrue();
        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Territory areas retrieved successfully")
                .body(territoryAreas.stream().map(territoryAreaMapper::toResponse).toList())
                .build();
    }

    public GenericResponse getTerritoryAreaById(Long id) {
        TerritoryArea territoryArea = findActiveOrThrow(id);
        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Territory area retrieved successfully")
                .body(territoryAreaMapper.toResponse(territoryArea))
                .build();
    }

    @Transactional
    public GenericResponse updateTerritoryArea(Long id, TerritoryAreaRequest request) {
        TerritoryArea territoryArea = findActiveOrThrow(id);
        territoryAreaRepository.findByStateTerritoryIdAndCodeIgnoreCase(request.getStateTerritoryId(), request.getCode())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new ConflictException("A territory area with this code already exists for this state/territory");
                    }
                });
        territoryAreaMapper.applyUpdate(territoryArea, request);
        TerritoryArea saved = territoryAreaRepository.save(territoryArea);
        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Territory area updated successfully")
                .body(territoryAreaMapper.toResponse(saved))
                .build();
    }

    @Transactional
    public GenericResponse deleteTerritoryArea(Long id) {
        TerritoryArea territoryArea = findActiveOrThrow(id);
        territoryArea.setIsActive(false);
        territoryAreaRepository.save(territoryArea);
        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Territory area deleted successfully")
                .build();
    }

    private TerritoryArea findActiveOrThrow(Long id) {
        return territoryAreaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TERRITORY_AREA_NOT_FOUND));
    }
}
