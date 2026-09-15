package com.autoadda.apis.vehicle.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogueOfferingRequest {

    @NotBlank(message = "Brand is required")
    private String brand;

    @NotBlank(message = "Model is required")
    private String model;

    private String trimName;

    @NotNull(message = "Model year is required")
    @Min(value = 1900, message = "Model year is not valid")
    private Integer modelYear;

    @NotNull(message = "Indicative price is required")
    @Positive(message = "Indicative price must be greater than zero")
    private BigDecimal indicativePrice;

    private String description;

    // MediaFile ids returned from POST /mediafiles/upload.
    private List<Long> imageIds;

    // Optional, update only: "ACTIVE" | "INACTIVE" | "ARCHIVED".
    private String status;
}
