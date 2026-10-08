package com.autoadda.apis.vehicle.controller;

import com.autoadda.apis.vehicle.request.PlateSourceRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.impl.PlateSourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RequiredArgsConstructor
@RestController
@RequestMapping("/plate-sources")
public class PlateSourceController {

    private final PlateSourceService plateSourceService;

    @PostMapping
    public GenericResponse createPlateSource(@Valid @RequestBody PlateSourceRequest request) {
        return plateSourceService.createPlateSource(request);
    }

    @GetMapping
    public GenericResponse getAllPlateSources() {
        return plateSourceService.getAllPlateSources();
    }

    /**
     * GET /plate-sources/tree - every active source with its types and codes nested,
     * for building the source -> type -> code dropdowns in one call.
     */
    @GetMapping("/tree")
    public GenericResponse getPlateMasterTree() {
        return plateSourceService.getPlateMasterTree();
    }

    @GetMapping("/{id}")
    public GenericResponse getPlateSourceById(@PathVariable Long id) {
        return plateSourceService.getPlateSourceById(id);
    }

    @PutMapping("/{id}")
    public GenericResponse updatePlateSource(@PathVariable Long id, @Valid @RequestBody PlateSourceRequest request) {
        return plateSourceService.updatePlateSource(id, request);
    }

    @DeleteMapping("/{id}")
    public GenericResponse deletePlateSource(@PathVariable Long id) {
        return plateSourceService.deletePlateSource(id);
    }
}
