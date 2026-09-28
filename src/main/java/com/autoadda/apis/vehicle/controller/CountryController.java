package com.autoadda.apis.vehicle.controller;

import com.autoadda.apis.vehicle.request.CountryRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.impl.CountryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RequiredArgsConstructor
@RestController
@RequestMapping("/countries")
public class CountryController {

    private final CountryService countryService;

    @PostMapping
    public GenericResponse createCountry(@Valid @RequestBody CountryRequest request) {
        return countryService.createCountry(request);
    }

    @GetMapping
    public GenericResponse getAllCountries() {
        return countryService.getAllCountries();
    }

    @GetMapping("/{id}")
    public GenericResponse getCountryById(@PathVariable Long id) {
        return countryService.getCountryById(id);
    }

    @PutMapping("/{id}")
    public GenericResponse updateCountry(@PathVariable Long id, @Valid @RequestBody CountryRequest request) {
        return countryService.updateCountry(id, request);
    }

    @DeleteMapping("/{id}")
    public GenericResponse deleteCountry(@PathVariable Long id) {
        return countryService.deleteCountry(id);
    }
}
