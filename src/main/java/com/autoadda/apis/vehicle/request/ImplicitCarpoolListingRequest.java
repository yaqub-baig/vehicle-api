package com.autoadda.apis.vehicle.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Inline trip details for a carpool connection request sent without the requester having
 * posted their own CarpoolListing. Type and travelDateTime are derived from the target listing,
 * not supplied here - see CarpoolConnectionServiceImpl.createImplicitRequesterListing.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImplicitCarpoolListingRequest {

    @NotNull(message = "Origin address is required")
    @Valid
    private AddressRequest originAddress;

    @NotNull(message = "Destination address is required")
    @Valid
    private AddressRequest destinationAddress;

    @NotNull(message = "Budget is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Budget must be greater than zero")
    private BigDecimal budget;

    // Required only if the derived type ends up OFFER, validated in the service layer.
    private Integer availableSeats;

    // Optional, OFFER-only freeform description.
    private String carDescription;
}
