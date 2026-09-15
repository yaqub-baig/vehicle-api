package com.autoadda.apis.vehicle.mapper;

import com.autoadda.apis.vehicle.entity.VehicleSale;
import com.autoadda.apis.vehicle.entity.VehicleSaleInvoice;
import com.autoadda.apis.vehicle.response.VehicleSaleInvoiceResponse;
import com.autoadda.apis.vehicle.response.VehicleSaleResponse;
import org.springframework.stereotype.Component;

@Component
public class VehicleSaleMapper {

    public VehicleSaleResponse toResponse(VehicleSale sale) {
        if (sale == null) {
            return null;
        }
        return VehicleSaleResponse.builder()
                .id(sale.getId())
                .vehicleId(sale.getVehicle() != null ? sale.getVehicle().getId() : null)
                .listingId(sale.getVehicleListing() != null ? sale.getVehicleListing().getId() : null)
                .previousOwnerId(sale.getPreviousOwnerId())
                .newOwnerId(sale.getNewOwnerId())
                .buyerName(sale.getBuyerName())
                .salePrice(sale.getSalePrice())
                .saleDate(sale.getSaleDate())
                .paymentMode(sale.getPaymentMode())
                .status(sale.getStatus())
                .invoice(toInvoiceResponse(sale.getInvoice()))
                .build();
    }

    public VehicleSaleInvoiceResponse toInvoiceResponse(VehicleSaleInvoice invoice) {
        if (invoice == null) {
            return null;
        }
        return VehicleSaleInvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceDate(invoice.getInvoiceDate())
                .totalAmount(invoice.getTotalAmount())
                .paymentMethod(invoice.getPaymentMethod())
                .vehicleSaleId(invoice.getVehicleSale() != null ? invoice.getVehicleSale().getId() : null)
                .build();
    }
}
