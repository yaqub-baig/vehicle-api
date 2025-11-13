package com.autoadda.apis.vehicle.clients;

import com.autoadda.apis.vehicle.response.BrandOnboardResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "part-api", url = "https://part-api-1.onrender.com/api/v1")
public interface PartClient {
    @GetMapping("/brand/isc/{referenceId}")
    public ResponseEntity<BrandOnboardResponse> getBrandById(@PathVariable String referenceId);
}
