package com.autoadda.apis.vehicle.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalCycleDiscountRequest {

    @NotNull(message = "Discount amount is required")
    private BigDecimal discountAmount;
}
