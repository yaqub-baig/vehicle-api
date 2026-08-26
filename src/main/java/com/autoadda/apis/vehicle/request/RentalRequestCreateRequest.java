package com.autoadda.apis.vehicle.request;

import com.autoadda.apis.vehicle.enums.RentalRateType;
import com.autoadda.apis.vehicle.enums.RentalType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalRequestCreateRequest {

    @NotNull(message = "Listing id is required")
    private Long listingId;

    @NotNull(message = "Requested rate type is required")
    private RentalRateType requestedRateType;

    @NotNull(message = "Rental type is required")
    private RentalType rentalType;

    // Required only when rentalType == LIMITED (positive integer), validated in the service layer.
    // Ignored/nulled by the service when rentalType == UNLIMITED.
    private Integer durationValue;

    // Optional note from the renter to the owner.
    private String message;
}
