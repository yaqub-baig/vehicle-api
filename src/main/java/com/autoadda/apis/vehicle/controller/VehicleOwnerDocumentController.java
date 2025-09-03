package com.autoadda.apis.vehicle.controller;

import com.autoadda.apis.vehicle.request.VehicleOwnerDocumentRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.VehicleOwnerDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/vehicle-owner-document")
public class VehicleOwnerDocumentController {
    private final VehicleOwnerDocumentService vehicleDocumentService;

    @PostMapping
    public GenericResponse uploadVehicleOwnerDocument(@RequestBody VehicleOwnerDocumentRequest vehicleOwnerDocumentRequest) {
    return vehicleDocumentService.uploadVehicleOwnerDocument(vehicleOwnerDocumentRequest);
    }

    @GetMapping("/{vehicleOwnerId}")
    public GenericResponse getVehicleOwnerDocument(@PathVariable Integer vehicleOwnerId) {
        return vehicleDocumentService.getVehicleOwnerDocument(vehicleOwnerId);
    }

    @PutMapping("/{vehicleOwnerId}")
    public GenericResponse updateVehicleOwnerDocument(@PathVariable Integer vehicleOwnerId, @RequestBody VehicleOwnerDocumentRequest vehicleOwnerDocumentRequest) {
        return vehicleDocumentService.updateVehicleOwnerDocument(vehicleOwnerId, vehicleOwnerDocumentRequest);
    }
}

