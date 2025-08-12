package com.autoadda.apis.vehicle.controller;

import com.autoadda.apis.vehicle.request.GarageV2Request;
import com.autoadda.apis.vehicle.response.GarageV2Response;
import com.autoadda.apis.vehicle.service.GarageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/garages")
@RequiredArgsConstructor
public class GarageController {

    private final GarageService garageService;

    @PostMapping
    public ResponseEntity<GarageV2Response> createGarage(@RequestHeader(value = "X-User-Id") String userId,
                                                         @RequestHeader(value = "X-User-Role") String userRole,
                                                         @RequestBody GarageV2Request request) {
        return garageService.createGarage ( request, userId, userRole );
    }

    @GetMapping
    public ResponseEntity<List<GarageV2Response>> getGarage(@RequestHeader(value = "X-User-Id") String userId,
                                                            @RequestHeader(value = "X-User-Role") String userRole) {
        return garageService.getGarage ( userId, userRole );
    }

    @GetMapping("/{garageId}")
    public ResponseEntity<GarageV2Response> getGarage(@PathVariable Integer garageId) {
        return garageService.getGarage ( garageId );
    }

    @DeleteMapping("/{garageId}")
    public ResponseEntity<String> deleteGarage(@RequestHeader(value = "X-User-Id") String userId,
                                               @RequestHeader(value = "X-User-Role") String userRole,
                                               @PathVariable Integer garageId) {
        return garageService.deleteGarage ( userId, userRole, garageId );
    }

    @PutMapping("/{garageId}")
    public ResponseEntity<GarageV2Response> updateGarage(@RequestHeader(value = "X-User-Id") String userId,
                                               @RequestHeader(value = "X-User-Role") String userRole,
                                               @PathVariable Integer garageId,
                                               @RequestBody GarageV2Request request) {
        return garageService.updateGarage ( userId, userRole, garageId, request );
    }

    @GetMapping("/all")
    public ResponseEntity<List<GarageV2Response>> getAllGarage(@RequestHeader(value = "X-User-Id") String userId,
                                                            @RequestHeader(value = "X-User-Role") String userRole) {
        return garageService.getAllGarage ( userId, userRole );
    }

}
