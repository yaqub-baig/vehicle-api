package com.autoadda.apis.vehicle.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarpoolConnectionRequest {

    @NotNull(message = "Your listing id is required")
    private Long requesterListingId;

    @NotNull(message = "Target listing id is required")
    private Long targetListingId;

    private String message;
}
