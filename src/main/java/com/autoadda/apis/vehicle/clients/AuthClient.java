package com.autoadda.apis.vehicle.clients;

import com.autoadda.apis.vehicle.response.CommunicationPreference;
import com.autoadda.apis.vehicle.request.EmailRequest;
import com.autoadda.apis.vehicle.request.SendNotificationRequest;
import com.autoadda.apis.vehicle.request.SmsRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.response.UserResponse;
import com.autoadda.apis.vehicle.util.ERole;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "identity-api", url = "http://localhost:8010/api/v1")
public interface AuthClient {

    @GetMapping("/auth/{id}")
    UserResponse getUser(@PathVariable Integer id);

    @PutMapping("/auth/update/role/{id}")
    GenericResponse updateRole(@PathVariable Integer id, @RequestParam ERole role);

    @GetMapping("/auth/byNumber/{phoneNumber}")
    ResponseEntity<UserResponse> getUserDetails(@PathVariable String phoneNumber);

    @GetMapping("/preferences/isc/{customerId}")
    ResponseEntity<CommunicationPreference> getPreferencesByCustomerId1(
            @PathVariable Integer customerId);

    @PostMapping("/auth/send-sms")
    GenericResponse sendSms(@RequestBody SmsRequest request);

    @PostMapping("/auth/send-email/{userId}")
    GenericResponse sendEmail(@PathVariable Integer userId,@RequestBody EmailRequest request );

    @PostMapping("/notifications/send")
    ResponseEntity<Map<String, Object>> sendNotification(@RequestBody SendNotificationRequest request);
}
