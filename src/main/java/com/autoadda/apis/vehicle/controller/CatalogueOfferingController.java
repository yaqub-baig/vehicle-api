package com.autoadda.apis.vehicle.controller;

import com.autoadda.apis.vehicle.request.CatalogueOfferingRequest;
import com.autoadda.apis.vehicle.request.CatalogueSearchCriteria;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.CatalogueOfferingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@CrossOrigin("*")
@RestController
@RequiredArgsConstructor
public class CatalogueOfferingController {

    private final CatalogueOfferingService offeringService;

    /**
     * Showroom owner adds a model/trim offering to their catalogue.
     * POST /companies/{companyId}/catalogue-offerings
     */
    @PostMapping("/companies/{companyId}/catalogue-offerings")
    public GenericResponse createOffering(
            @PathVariable Long companyId,
            @Valid @RequestBody CatalogueOfferingRequest request,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return offeringService.createOffering(companyId, Integer.parseInt(userId), request);
    }

    /**
     * Showroom owner updates one of their offerings.
     * PUT /companies/{companyId}/catalogue-offerings/{offeringId}
     */
    @PutMapping("/companies/{companyId}/catalogue-offerings/{offeringId}")
    public GenericResponse updateOffering(
            @PathVariable Long companyId,
            @PathVariable Long offeringId,
            @Valid @RequestBody CatalogueOfferingRequest request,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return offeringService.updateOffering(offeringId, Integer.parseInt(userId), request);
    }

    /**
     * Showroom owner changes an offering's status (ACTIVE / INACTIVE / ARCHIVED).
     * PUT /companies/{companyId}/catalogue-offerings/{offeringId}/status?value=INACTIVE
     */
    @PutMapping("/companies/{companyId}/catalogue-offerings/{offeringId}/status")
    public GenericResponse setOfferingStatus(
            @PathVariable Long companyId,
            @PathVariable Long offeringId,
            @RequestParam("value") String value,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return offeringService.setStatus(offeringId, Integer.parseInt(userId), value);
    }

    /**
     * List a company's catalogue offerings (public).
     * GET /companies/{companyId}/catalogue-offerings
     */
    @GetMapping("/companies/{companyId}/catalogue-offerings")
    public GenericResponse getOfferingsForCompany(@PathVariable Long companyId) {
        return offeringService.getOfferingsForCompany(companyId);
    }

    /**
     * Get a single offering by id (public).
     * GET /catalogue-offerings/{id}
     */
    @GetMapping("/catalogue-offerings/{id}")
    public GenericResponse getOfferingById(@PathVariable Long id) {
        return offeringService.getOfferingById(id);
    }

    /**
     * Browse active offerings across all showrooms, paginated (public).
     * GET /catalogue-offerings/browse?brand=&model=&minPrice=&maxPrice=&fromYear=&toYear=&companyId=&page=&size=&sortBy=&sortDirection=
     */
    @GetMapping("/catalogue-offerings/browse")
    public GenericResponse browse(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer fromYear,
            @RequestParam(required = false) Integer toYear,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        CatalogueSearchCriteria criteria = CatalogueSearchCriteria.builder()
                .brand(brand)
                .model(model)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .fromYear(fromYear)
                .toYear(toYear)
                .companyId(companyId)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
        return offeringService.browse(criteria);
    }
}
