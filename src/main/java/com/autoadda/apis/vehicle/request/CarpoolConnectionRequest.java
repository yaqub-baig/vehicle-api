package com.autoadda.apis.vehicle.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarpoolConnectionRequest {

    // Exactly one of requesterListingId / requesterListingDetails is required - enforced in
    // CarpoolConnectionServiceImpl.createConnectionRequest, not via bean validation.
    private Long requesterListingId;

    @Valid
    private ImplicitCarpoolListingRequest requesterListingDetails;

    @NotNull(message = "Target listing id is required")
    private Long targetListingId;

    private String message;
}
