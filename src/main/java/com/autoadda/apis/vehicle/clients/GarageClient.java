package com.autoadda.apis.vehicle.clients;

import com.autoadda.apis.vehicle.response.VehicleServiceHistoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "garage-api", url = "http://localhost:8020/api/v1")
public interface GarageClient {

    @GetMapping("/work-orders/vehicle/{vehicleId}")
    ResponseEntity<List<VehicleServiceHistoryResponse>> getWorkOrdersByVehicleId(
            @PathVariable Integer vehicleId);
}
