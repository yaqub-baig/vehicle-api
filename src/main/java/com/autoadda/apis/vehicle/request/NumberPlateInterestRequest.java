package com.autoadda.apis.vehicle.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NumberPlateInterestRequest {

    @NotNull(message = "Listing id is required")
    private Long listingId;

    // Optional note from the buyer to the seller.
    private String message;
}
