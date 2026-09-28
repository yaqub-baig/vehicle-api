package com.autoadda.apis.vehicle.controller;

import com.autoadda.apis.vehicle.request.StreetRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.impl.StreetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RequiredArgsConstructor
@RestController
@RequestMapping("/streets")
public class StreetController {

    private final StreetService streetService;

    @PostMapping
    public GenericResponse createStreet(@Valid @RequestBody StreetRequest request) {
        return streetService.createStreet(request);
    }

    /**
     * GET /streets?territoryAreaId=1 to power an area -> street cascading dropdown.
     */
    @GetMapping
    public GenericResponse getAllStreets(@RequestParam(required = false) Long territoryAreaId) {
        return streetService.getAllStreets(territoryAreaId);
    }

    @GetMapping("/{id}")
    public GenericResponse getStreetById(@PathVariable Long id) {
        return streetService.getStreetById(id);
    }

    @PutMapping("/{id}")
    public GenericResponse updateStreet(@PathVariable Long id, @Valid @RequestBody StreetRequest request) {
        return streetService.updateStreet(id, request);
    }

    @DeleteMapping("/{id}")
    public GenericResponse deleteStreet(@PathVariable Long id) {
        return streetService.deleteStreet(id);
    }
}
