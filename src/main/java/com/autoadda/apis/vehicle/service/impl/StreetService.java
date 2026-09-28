package com.autoadda.apis.vehicle.service.impl;

import com.autoadda.apis.vehicle.dao.StreetRepository;
import com.autoadda.apis.vehicle.entity.Street;
import com.autoadda.apis.vehicle.exception.ConflictException;
import com.autoadda.apis.vehicle.exception.ResourceNotFoundException;
import com.autoadda.apis.vehicle.mapper.StreetMapper;
import com.autoadda.apis.vehicle.request.StreetRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StreetService {

    private static final String SUCCESS = "success";
    private static final String STREET_NOT_FOUND = "Street not found";

    private final StreetRepository streetRepository;
    private final StreetMapper streetMapper;

    @Transactional
    public GenericResponse createStreet(StreetRequest request) {
        streetRepository.findByTerritoryAreaIdAndCodeIgnoreCase(request.getTerritoryAreaId(), request.getCode())
                .ifPresent(existing -> {
                    throw new ConflictException("A street with this code already exists for this territory area");
                });
        Street saved = streetRepository.save(streetMapper.toEntity(request));
        return GenericResponse.builder()
                .status(SUCCESS)
                .code("201")
                .message("Street created successfully")
                .body(streetMapper.toResponse(saved))
                .build();
    }

    public GenericResponse getAllStreets(Long territoryAreaId) {
        List<Street> streets = territoryAreaId != null
                ? streetRepository.findByTerritoryAreaIdAndIsActiveTrue(territoryAreaId)
                : streetRepository.findByIsActiveTrue();
        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Streets retrieved successfully")
                .body(streets.stream().map(streetMapper::toResponse).toList())
                .build();
    }

    public GenericResponse getStreetById(Long id) {
        Street street = findActiveOrThrow(id);
        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Street retrieved successfully")
                .body(streetMapper.toResponse(street))
                .build();
    }

    @Transactional
    public GenericResponse updateStreet(Long id, StreetRequest request) {
        Street street = findActiveOrThrow(id);
        streetRepository.findByTerritoryAreaIdAndCodeIgnoreCase(request.getTerritoryAreaId(), request.getCode())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new ConflictException("A street with this code already exists for this territory area");
                    }
                });
        streetMapper.applyUpdate(street, request);
        Street saved = streetRepository.save(street);
        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Street updated successfully")
                .body(streetMapper.toResponse(saved))
                .build();
    }

    @Transactional
    public GenericResponse deleteStreet(Long id) {
        Street street = findActiveOrThrow(id);
        street.setIsActive(false);
        streetRepository.save(street);
        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Street deleted successfully")
                .build();
    }

    private Street findActiveOrThrow(Long id) {
        return streetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(STREET_NOT_FOUND));
    }
}
