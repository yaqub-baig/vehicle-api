package com.autoadda.apis.vehicle.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleSaleInvoiceResponse {
    private Integer id;
    private LocalDate invoiceDate;
    private double totalAmount;
    private String paymentMethod;
    private Integer vehicleSaleId;
}
