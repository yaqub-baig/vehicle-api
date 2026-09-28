package com.autoadda.apis.vehicle.controller;

import com.autoadda.apis.vehicle.request.StateTerritoryRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.impl.StateTerritoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RequiredArgsConstructor
@RestController
@RequestMapping("/state-territories")
public class StateTerritoryController {

    private final StateTerritoryService stateTerritoryService;

    @PostMapping
    public GenericResponse createStateTerritory(@Valid @RequestBody StateTerritoryRequest request) {
        return stateTerritoryService.createStateTerritory(request);
    }

    /**
     * GET /state-territories?countryId=1 to power a country -> state cascading dropdown.
     */
    @GetMapping
    public GenericResponse getAllStateTerritories(@RequestParam(required = false) Long countryId) {
        return stateTerritoryService.getAllStateTerritories(countryId);
    }

    @GetMapping("/{id}")
    public GenericResponse getStateTerritoryById(@PathVariable Long id) {
        return stateTerritoryService.getStateTerritoryById(id);
    }

    @PutMapping("/{id}")
    public GenericResponse updateStateTerritory(@PathVariable Long id, @Valid @RequestBody StateTerritoryRequest request) {
        return stateTerritoryService.updateStateTerritory(id, request);
    }

    @DeleteMapping("/{id}")
    public GenericResponse deleteStateTerritory(@PathVariable Long id) {
        return stateTerritoryService.deleteStateTerritory(id);
    }
}
