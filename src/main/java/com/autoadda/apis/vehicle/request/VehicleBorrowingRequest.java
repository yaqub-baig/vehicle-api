package com.autoadda.apis.vehicle.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleBorrowingRequest {

    @NotNull(message = "Vehicle ID is required")
    private Integer vehicleId;


    private LocalDateTime expectedReturnTime;

    private Long borrowingImageId;

    private String borrowingPurpose;
}
