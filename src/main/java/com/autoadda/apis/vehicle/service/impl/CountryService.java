package com.autoadda.apis.vehicle.service.impl;

import com.autoadda.apis.vehicle.dao.CountryRepository;
import com.autoadda.apis.vehicle.entity.Country;
import com.autoadda.apis.vehicle.exception.ConflictException;
import com.autoadda.apis.vehicle.exception.ResourceNotFoundException;
import com.autoadda.apis.vehicle.mapper.CountryMapper;
import com.autoadda.apis.vehicle.request.CountryRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CountryService {

    private static final String SUCCESS = "success";
    private static final String COUNTRY_NOT_FOUND = "Country not found";

    private final CountryRepository countryRepository;
    private final CountryMapper countryMapper;

    @Transactional
    public GenericResponse createCountry(CountryRequest request) {
        countryRepository.findByCodeIgnoreCase(request.getCode()).ifPresent(existing -> {
            throw new ConflictException("A country with this code already exists");
        });
        Country saved = countryRepository.save(countryMapper.toEntity(request));
        return GenericResponse.builder()
                .status(SUCCESS)
                .code("201")
                .message("Country created successfully")
                .body(countryMapper.toResponse(saved))
                .build();
    }

    public GenericResponse getAllCountries() {
        List<Country> countries = countryRepository.findByIsActiveTrue();
        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Countries retrieved successfully")
                .body(countries.stream().map(countryMapper::toResponse).toList())
                .build();
    }

    public GenericResponse getCountryById(Long id) {
        Country country = findActiveOrThrow(id);
        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Country retrieved successfully")
                .body(countryMapper.toResponse(country))
                .build();
    }

    @Transactional
    public GenericResponse updateCountry(Long id, CountryRequest request) {
        Country country = findActiveOrThrow(id);
        countryRepository.findByCodeIgnoreCase(request.getCode()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new ConflictException("A country with this code already exists");
            }
        });
        countryMapper.applyUpdate(country, request);
        Country saved = countryRepository.save(country);
        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Country updated successfully")
                .body(countryMapper.toResponse(saved))
                .build();
    }

    @Transactional
    public GenericResponse deleteCountry(Long id) {
        Country country = findActiveOrThrow(id);
        country.setIsActive(false);
        countryRepository.save(country);
        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Country deleted successfully")
                .build();
    }

    private Country findActiveOrThrow(Long id) {
        return countryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(COUNTRY_NOT_FOUND));
    }
}
