package com.autoadda.apis.vehicle.controller;

import com.autoadda.apis.vehicle.request.TerritoryAreaRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.impl.TerritoryAreaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RequiredArgsConstructor
@RestController
@RequestMapping("/territory-areas")
public class TerritoryAreaController {

    private final TerritoryAreaService territoryAreaService;

    @PostMapping
    public GenericResponse createTerritoryArea(@Valid @RequestBody TerritoryAreaRequest request) {
        return territoryAreaService.createTerritoryArea(request);
    }

    /**
     * GET /territory-areas?stateTerritoryId=1 to power a state -> area cascading dropdown.
     */
    @GetMapping
    public GenericResponse getAllTerritoryAreas(@RequestParam(required = false) Long stateTerritoryId) {
        return territoryAreaService.getAllTerritoryAreas(stateTerritoryId);
    }

    @GetMapping("/{id}")
    public GenericResponse getTerritoryAreaById(@PathVariable Long id) {
        return territoryAreaService.getTerritoryAreaById(id);
    }

    @PutMapping("/{id}")
    public GenericResponse updateTerritoryArea(@PathVariable Long id, @Valid @RequestBody TerritoryAreaRequest request) {
        return territoryAreaService.updateTerritoryArea(id, request);
    }

    @DeleteMapping("/{id}")
    public GenericResponse deleteTerritoryArea(@PathVariable Long id) {
        return territoryAreaService.deleteTerritoryArea(id);
    }
}
