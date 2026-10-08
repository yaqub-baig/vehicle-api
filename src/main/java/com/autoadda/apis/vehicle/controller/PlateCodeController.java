package com.autoadda.apis.vehicle.controller;

import com.autoadda.apis.vehicle.request.PlateCodeRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.impl.PlateCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RequiredArgsConstructor
@RestController
@RequestMapping("/plate-codes")
public class PlateCodeController {

    private final PlateCodeService plateCodeService;

    @PostMapping
    public GenericResponse createPlateCode(@Valid @RequestBody PlateCodeRequest request) {
        return plateCodeService.createPlateCode(request);
    }

    @GetMapping
    public GenericResponse getAllPlateCodes(@RequestParam(required = false) Long plateTypeId) {
        return plateCodeService.getAllPlateCodes(plateTypeId);
    }

    @GetMapping("/{id}")
    public GenericResponse getPlateCodeById(@PathVariable Long id) {
        return plateCodeService.getPlateCodeById(id);
    }

    @PutMapping("/{id}")
    public GenericResponse updatePlateCode(@PathVariable Long id, @Valid @RequestBody PlateCodeRequest request) {
        return plateCodeService.updatePlateCode(id, request);
    }

    @DeleteMapping("/{id}")
    public GenericResponse deletePlateCode(@PathVariable Long id) {
        return plateCodeService.deletePlateCode(id);
    }
}
