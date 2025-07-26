package com.platform.profileservice.service;

import com.platform.profileservice.response.GarageV2Response;
import com.platform.profileservice.request.GarageV2Request;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface GarageService {
    ResponseEntity<GarageV2Response> createGarage(GarageV2Request request, String userId, String userRole);

    ResponseEntity<List<GarageV2Response>> getGarage(String userId, String userRole);

    ResponseEntity<String> deleteGarage(String userId, String userRole, Integer garageId);

    ResponseEntity<GarageV2Response> updateGarage(String userId, String userRole, Integer garageId, GarageV2Request request);

    ResponseEntity<GarageV2Response>     getGarage(Integer garageId);

    ResponseEntity<List<GarageV2Response>> getAllGarage(String userId, String userRole);

}
