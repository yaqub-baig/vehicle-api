package com.autoadda.apis.vehicle.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NumberPlateListingRequest {

    @NotBlank(message = "Plate number is required")
    private String plateNumber;

    @NotBlank(message = "Emirate is required")
    private String emirate;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Expected price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Expected price must be greater than zero")
    private BigDecimal expectedPrice;

    // Optional, freeform. Not linked to an existing VehicleDetails record.
    private String description;

    // Optional. MediaFile ids returned from POST /mediafiles/upload.
    private List<Long> imageIds;
}
