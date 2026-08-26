package com.autoadda.apis.vehicle.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalCycleReturnRequest {

    @NotNull(message = "End odometer reading is required")
    private Integer endOdometer;
}
