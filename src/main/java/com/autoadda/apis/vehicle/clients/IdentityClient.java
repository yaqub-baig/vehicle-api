package com.autoadda.apis.vehicle.clients;

import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.response.UserResponse;
import com.autoadda.apis.vehicle.util.ERole;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name = "identity-api", url = "https://identity-api-wbad.onrender.com/api/v1")
public interface IdentityClient {
    @GetMapping("/auth/{id}")
    public UserResponse getUser(@PathVariable Integer id);
    @PutMapping("/auth/update/role/{id}")
    GenericResponse updateRole(@PathVariable Integer id, @RequestParam ERole role);

}
