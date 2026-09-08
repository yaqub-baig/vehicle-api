package com.autoadda.apis.vehicle.response;

import com.autoadda.apis.vehicle.util.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleSaleResponse {
    private Integer id;
    private Integer vehicleId;
    private Integer listingId;
    private Integer previousOwnerId;
    private Integer newOwnerId;
    private String buyerName;
    private Double salePrice;
    private LocalDate saleDate;
    private String paymentMode;
    private Status status;
    private VehicleSaleInvoiceResponse invoice;
}
