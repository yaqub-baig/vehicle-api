package com.autoadda.apis.vehicle.service.impl;

import com.autoadda.apis.vehicle.dao.StateTerritoryRepository;
import com.autoadda.apis.vehicle.entity.StateTerritory;
import com.autoadda.apis.vehicle.exception.ConflictException;
import com.autoadda.apis.vehicle.exception.ResourceNotFoundException;
import com.autoadda.apis.vehicle.mapper.StateTerritoryMapper;
import com.autoadda.apis.vehicle.request.StateTerritoryRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StateTerritoryService {

    private static final String SUCCESS = "success";
    private static final String STATE_TERRITORY_NOT_FOUND = "State/territory not found";

    private final StateTerritoryRepository stateTerritoryRepository;
    private final StateTerritoryMapper stateTerritoryMapper;

    @Transactional
    public GenericResponse createStateTerritory(StateTerritoryRequest request) {
        stateTerritoryRepository.findByCountryIdAndCodeIgnoreCase(request.getCountryId(), request.getCode())
                .ifPresent(existing -> {
                    throw new ConflictException("A state/territory with this code already exists for this country");
                });
        StateTerritory saved = stateTerritoryRepository.save(stateTerritoryMapper.toEntity(request));
        return GenericResponse.builder()
                .status(SUCCESS)
                .code("201")
                .message("State/territory created successfully")
                .body(stateTerritoryMapper.toResponse(saved))
                .build();
    }

    public GenericResponse getAllStateTerritories(Long countryId) {
        List<StateTerritory> stateTerritories = countryId != null
                ? stateTerritoryRepository.findByCountryIdAndIsActiveTrue(countryId)
                : stateTerritoryRepository.findByIsActiveTrue();
        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("States/territories retrieved successfully")
                .body(stateTerritories.stream().map(stateTerritoryMapper::toResponse).toList())
                .build();
    }

    public GenericResponse getStateTerritoryById(Long id) {
        StateTerritory stateTerritory = findActiveOrThrow(id);
        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("State/territory retrieved successfully")
                .body(stateTerritoryMapper.toResponse(stateTerritory))
                .build();
    }

    @Transactional
    public GenericResponse updateStateTerritory(Long id, StateTerritoryRequest request) {
        StateTerritory stateTerritory = findActiveOrThrow(id);
        stateTerritoryRepository.findByCountryIdAndCodeIgnoreCase(request.getCountryId(), request.getCode())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new ConflictException("A state/territory with this code already exists for this country");
                    }
                });
        stateTerritoryMapper.applyUpdate(stateTerritory, request);
        StateTerritory saved = stateTerritoryRepository.save(stateTerritory);
        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("State/territory updated successfully")
                .body(stateTerritoryMapper.toResponse(saved))
                .build();
    }

    @Transactional
    public GenericResponse deleteStateTerritory(Long id) {
        StateTerritory stateTerritory = findActiveOrThrow(id);
        stateTerritory.setIsActive(false);
        stateTerritoryRepository.save(stateTerritory);
        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("State/territory deleted successfully")
                .build();
    }

    private StateTerritory findActiveOrThrow(Long id) {
        return stateTerritoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(STATE_TERRITORY_NOT_FOUND));
    }
}
