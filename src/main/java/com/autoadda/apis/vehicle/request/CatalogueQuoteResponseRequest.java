package com.autoadda.apis.vehicle.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * The showroom's response body when accepting a quote request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogueQuoteResponseRequest {

    @Size(max = 1000, message = "Response message must be at most 1000 characters")
    private String responseMessage;

    private BigDecimal quotedPrice;
}
