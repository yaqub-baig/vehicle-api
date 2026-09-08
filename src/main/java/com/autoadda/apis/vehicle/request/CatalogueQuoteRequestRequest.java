package com.autoadda.apis.vehicle.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogueQuoteRequestRequest {

    @NotNull(message = "Offering id is required")
    private Long offeringId;

    // Optional note from the buyer to the showroom.
    @Size(max = 1000, message = "Message must be at most 1000 characters")
    private String message;
}
