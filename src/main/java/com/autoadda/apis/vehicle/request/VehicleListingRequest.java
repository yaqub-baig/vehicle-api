package com.autoadda.apis.vehicle.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Request DTO for creating or updating a vehicle listing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleListingRequest {
    private Integer vehicleId;
    private Double price;
    private String description;
    private String city;
    private String status;
    private String condition;
}