package com.autoadda.apis.vehicle.request;

import com.autoadda.apis.vehicle.util.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

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
}