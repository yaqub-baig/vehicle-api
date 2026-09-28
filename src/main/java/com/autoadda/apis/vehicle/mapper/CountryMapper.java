package com.autoadda.apis.vehicle.mapper;

import com.autoadda.apis.vehicle.entity.Country;
import com.autoadda.apis.vehicle.request.CountryRequest;
import com.autoadda.apis.vehicle.response.CountryResponse;
import org.springframework.stereotype.Component;

@Component
public class CountryMapper {

    public Country toEntity(CountryRequest request) {
        return Country.builder()
                .code(request.getCode())
                .name(request.getName())
                .isActive(true)
                .build();
    }

    public void applyUpdate(Country entity, CountryRequest request) {
        entity.setCode(request.getCode());
        entity.setName(request.getName());
    }

    public CountryResponse toResponse(Country entity) {
        if (entity == null) {
            return null;
        }
        return CountryResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .isActive(entity.getIsActive())
                .build();
    }
}
