package com.autoadda.apis.vehicle.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarRentalListingRequest {

    @NotNull(message = "Vehicle id is required")
    private Integer vehicleId;

    // Optional - some owners may only rent by the day.
    private BigDecimal pricePerHour;

    @NotNull(message = "Price per day is required")
    private BigDecimal pricePerDay;

    @NotNull(message = "Km limit per day is required")
    private Integer kmLimitPerDay;

    @NotNull(message = "Excess km penalty rate is required")
    private BigDecimal excessKmPenaltyRate;

    @NotNull(message = "Late return penalty rate is required")
    private BigDecimal lateReturnPenaltyRate;

    private String description;
}
