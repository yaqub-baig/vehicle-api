package com.autoadda.apis.vehicle.clients;

import com.autoadda.apis.vehicle.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "identity-api", url = "http://localhost:8010/api/v1")
public interface IdentityClient {
    @GetMapping("/auth/{id}")
    public UserResponse getUser(@PathVariable Integer id);

}
