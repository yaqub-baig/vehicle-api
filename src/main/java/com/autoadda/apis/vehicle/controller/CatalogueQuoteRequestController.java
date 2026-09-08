package com.autoadda.apis.vehicle.controller;

import com.autoadda.apis.vehicle.enums.CatalogueQuoteRequestStatus;
import com.autoadda.apis.vehicle.request.CatalogueQuoteRejectionRequest;
import com.autoadda.apis.vehicle.request.CatalogueQuoteRequestRequest;
import com.autoadda.apis.vehicle.request.CatalogueQuoteResponseRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.CatalogueQuoteRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/catalogue-quote-requests")
@RequiredArgsConstructor
public class CatalogueQuoteRequestController {

    private final CatalogueQuoteRequestService quoteRequestService;

    /**
     * Buyer requests a quote on a catalogue offering.
     * POST /catalogue-quote-requests
     */
    @PostMapping
    public GenericResponse create(
            @Valid @RequestBody CatalogueQuoteRequestRequest request,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return quoteRequestService.createQuoteRequest(request, Integer.parseInt(userId));
    }

    /**
     * Showroom accepts a pending quote request (with an optional quoted price + message);
     * reveals contact info to both parties.
     * PUT /catalogue-quote-requests/{id}/accept
     */
    @PutMapping("/{id}/accept")
    public GenericResponse accept(
            @PathVariable Long id,
            @RequestHeader("X-Auto-Adda-Id") String userId,
            @RequestBody(required = false) CatalogueQuoteResponseRequest response) {
        return quoteRequestService.acceptQuoteRequest(id, Integer.parseInt(userId), response);
    }

    /**
     * Showroom rejects a pending quote request.
     * PUT /catalogue-quote-requests/{id}/reject
     */
    @PutMapping("/{id}/reject")
    public GenericResponse reject(
            @PathVariable Long id,
            @RequestHeader("X-Auto-Adda-Id") String userId,
            @RequestBody(required = false) CatalogueQuoteRejectionRequest request) {
        return quoteRequestService.rejectQuoteRequest(id, Integer.parseInt(userId), request);
    }

    /**
     * Buyer cancels a pending quote request.
     * PUT /catalogue-quote-requests/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public GenericResponse cancel(
            @PathVariable Long id,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return quoteRequestService.cancelQuoteRequest(id, Integer.parseInt(userId));
    }

    /**
     * Get a quote request by id (participants only).
     * GET /catalogue-quote-requests/{id}
     */
    @GetMapping("/{id}")
    public GenericResponse getById(
            @PathVariable Long id,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return quoteRequestService.getQuoteRequestById(id, Integer.parseInt(userId));
    }

    /**
     * Buyer's own quote requests, optionally filtered by status.
     * GET /catalogue-quote-requests/my?status=PENDING
     */
    @GetMapping("/my")
    public GenericResponse my(
            @RequestHeader("X-Auto-Adda-Id") String userId,
            @RequestParam(required = false) CatalogueQuoteRequestStatus status) {
        return quoteRequestService.getMyQuoteRequests(Integer.parseInt(userId), status);
    }

    /**
     * Showroom: all quote requests on one offering.
     * GET /catalogue-quote-requests/offering/{offeringId}
     */
    @GetMapping("/offering/{offeringId}")
    public GenericResponse forOffering(
            @PathVariable Long offeringId,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return quoteRequestService.getQuoteRequestsForOffering(offeringId, Integer.parseInt(userId));
    }

    /**
     * Showroom inbox: all quote requests across the company's catalogue.
     * GET /catalogue-quote-requests/company/{companyId}
     */
    @GetMapping("/company/{companyId}")
    public GenericResponse forCompany(
            @PathVariable Long companyId,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return quoteRequestService.getQuoteRequestsForCompany(companyId, Integer.parseInt(userId));
    }
}
