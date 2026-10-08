package com.autoadda.apis.vehicle.controller;

import com.autoadda.apis.vehicle.request.PlateTypeRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.impl.PlateTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RequiredArgsConstructor
@RestController
@RequestMapping("/plate-types")
public class PlateTypeController {

    private final PlateTypeService plateTypeService;

    @PostMapping
    public GenericResponse createPlateType(@Valid @RequestBody PlateTypeRequest request) {
        return plateTypeService.createPlateType(request);
    }

    @GetMapping
    public GenericResponse getAllPlateTypes(@RequestParam(required = false) Long plateSourceId) {
        return plateTypeService.getAllPlateTypes(plateSourceId);
    }

    @GetMapping("/{id}")
    public GenericResponse getPlateTypeById(@PathVariable Long id) {
        return plateTypeService.getPlateTypeById(id);
    }

    @PutMapping("/{id}")
    public GenericResponse updatePlateType(@PathVariable Long id, @Valid @RequestBody PlateTypeRequest request) {
        return plateTypeService.updatePlateType(id, request);
    }

    @DeleteMapping("/{id}")
    public GenericResponse deletePlateType(@PathVariable Long id) {
        return plateTypeService.deletePlateType(id);
    }
}
