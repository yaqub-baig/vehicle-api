package com.autoadda.apis.vehicle.request;

import com.autoadda.apis.vehicle.enums.CarpoolListingType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarpoolListingRequest {

    @NotNull(message = "Listing type is required")
    private CarpoolListingType type;

    @NotNull(message = "Origin address is required")
    @Valid
    private AddressRequest originAddress;

    @NotNull(message = "Destination address is required")
    @Valid
    private AddressRequest destinationAddress;

    @NotNull(message = "Travel date/time is required")
    private LocalDateTime travelDateTime;

    @NotNull(message = "Budget is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Budget must be greater than zero")
    private BigDecimal budget;

    // Required only when type == OFFER, validated in the service layer.
    private Integer availableSeats;

    // Optional, OFFER-only freeform description. Not linked to an existing VehicleDetails record.
    private String carDescription;
}
