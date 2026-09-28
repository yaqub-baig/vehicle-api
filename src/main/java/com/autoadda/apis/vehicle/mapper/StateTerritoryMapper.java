package com.autoadda.apis.vehicle.mapper;

import com.autoadda.apis.vehicle.dao.CountryRepository;
import com.autoadda.apis.vehicle.entity.Country;
import com.autoadda.apis.vehicle.entity.StateTerritory;
import com.autoadda.apis.vehicle.exception.ResourceNotFoundException;
import com.autoadda.apis.vehicle.request.StateTerritoryRequest;
import com.autoadda.apis.vehicle.response.StateTerritoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StateTerritoryMapper {

    private final CountryRepository countryRepository;
    private final CountryMapper countryMapper;

    public StateTerritory toEntity(StateTerritoryRequest request) {
        return StateTerritory.builder()
                .code(request.getCode())
                .name(request.getName())
                .country(resolveCountry(request.getCountryId()))
                .isActive(true)
                .build();
    }

    public void applyUpdate(StateTerritory entity, StateTerritoryRequest request) {
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setCountry(resolveCountry(request.getCountryId()));
    }

    public StateTerritoryResponse toResponse(StateTerritory entity) {
        if (entity == null) {
            return null;
        }
        return StateTerritoryResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .isActive(entity.getIsActive())
                .country(countryMapper.toResponse(entity.getCountry()))
                .build();
    }

    private Country resolveCountry(Long countryId) {
        return countryRepository.findById(countryId)
                .orElseThrow(() -> new ResourceNotFoundException("Country not found"));
    }
}
