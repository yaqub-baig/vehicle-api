package com.autoadda.apis.vehicle.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalCycleStartRequest {

    @NotNull(message = "Start odometer reading is required")
    private Integer startOdometer;

    // Optional. If provided, a late-return penalty applies if the vehicle isn't
    // returned by this time. If omitted, no late-return penalty is charged.
    private LocalDateTime expectedReturnAt;
}
