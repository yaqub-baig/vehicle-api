package com.autoadda.apis.vehicle.service;

import com.autoadda.apis.vehicle.request.CompleteSaleRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;

public interface VehicleSaleService {

    GenericResponse completeSale(CompleteSaleRequest request, Integer callerId);

    GenericResponse getSaleById(Integer saleId, Integer callerId);

    GenericResponse getSaleByListingId(Integer listingId, Integer callerId);

    GenericResponse getInvoice(Integer saleId, Integer callerId);

    GenericResponse getMySales(Integer callerId);

    GenericResponse getMyPurchases(Integer callerId);
}
