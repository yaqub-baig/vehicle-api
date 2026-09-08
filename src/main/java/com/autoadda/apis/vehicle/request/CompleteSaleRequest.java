package com.autoadda.apis.vehicle.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Body for completing a used-car sale: records the {@code VehicleSale}, marks the listing SOLD,
 * generates an invoice and transfers ownership of the vehicle to {@code buyerUserId}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompleteSaleRequest {

    @NotNull(message = "Listing id is required")
    private Integer listingId;

    // Ownership transfers to this user, so the buyer must be a registered platform user.
    @NotNull(message = "Buyer user id is required")
    private Integer buyerUserId;

    // Optional display override; defaults to the buyer's account name.
    private String buyerName;

    @NotNull(message = "Sale price is required")
    @Positive(message = "Sale price must be greater than zero")
    private Double salePrice;

    // Null -> today.
    private LocalDate saleDate;

    // CASH | CREDIT | LOAN | BANK_TRANSFER | CHEQUE
    @NotBlank(message = "Payment mode is required")
    private String paymentMode;

    // Invoice overrides; null -> derived from salePrice / paymentMode.
    private Double invoiceAmount;
    private String invoicePaymentMethod;
}
