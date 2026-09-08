package com.autoadda.apis.vehicle.controller;

import com.autoadda.apis.vehicle.request.CompleteSaleRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.VehicleSaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/vehicle-sales")
@RequiredArgsConstructor
public class VehicleSaleController {

    private final VehicleSaleService vehicleSaleService;

    /**
     * Complete a used-car sale: records the sale + invoice, marks the listing SOLD,
     * clears isForSale and transfers vehicle ownership to the buyer.
     * POST /vehicle-sales
     */
    @PostMapping
    public GenericResponse completeSale(
            @Valid @RequestBody CompleteSaleRequest request,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return vehicleSaleService.completeSale(request, Integer.parseInt(userId));
    }

    /**
     * GET /vehicle-sales/{id}
     */
    @GetMapping("/{id}")
    public GenericResponse getById(
            @PathVariable Integer id,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return vehicleSaleService.getSaleById(id, Integer.parseInt(userId));
    }

    /**
     * GET /vehicle-sales/listing/{listingId}
     */
    @GetMapping("/listing/{listingId}")
    public GenericResponse getByListing(
            @PathVariable Integer listingId,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return vehicleSaleService.getSaleByListingId(listingId, Integer.parseInt(userId));
    }

    /**
     * GET /vehicle-sales/{id}/invoice
     */
    @GetMapping("/{id}/invoice")
    public GenericResponse getInvoice(
            @PathVariable Integer id,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return vehicleSaleService.getInvoice(id, Integer.parseInt(userId));
    }

    /**
     * Sales where the caller was the seller (previous owner).
     * GET /vehicle-sales/my-sales
     */
    @GetMapping("/my-sales")
    public GenericResponse mySales(@RequestHeader("X-Auto-Adda-Id") String userId) {
        return vehicleSaleService.getMySales(Integer.parseInt(userId));
    }

    /**
     * Sales where the caller was the buyer (new owner).
     * GET /vehicle-sales/my-purchases
     */
    @GetMapping("/my-purchases")
    public GenericResponse myPurchases(@RequestHeader("X-Auto-Adda-Id") String userId) {
        return vehicleSaleService.getMyPurchases(Integer.parseInt(userId));
    }
}
