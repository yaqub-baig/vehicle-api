package com.autoadda.apis.vehicle.controller;

import com.autoadda.apis.vehicle.request.CompanyDocumentRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.impl.CompanyDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
@CrossOrigin("*")
@RestController
@RequiredArgsConstructor
@RequestMapping("/company-document")
public class CompanyDocumentController {
    private final CompanyDocumentService companyDocumentService;

    @PostMapping
    public GenericResponse uploadCompanyDocument(@RequestBody CompanyDocumentRequest companyDocumentRequest) {
        return companyDocumentService.uploadCompanyDocument ( companyDocumentRequest );
    }

    @GetMapping("/{companyId}")
    public GenericResponse getCompanyDocument(@PathVariable Long companyId) {
        return companyDocumentService.getCompanyDocument ( companyId );
    }

    @PutMapping("/{companyId}")
    public GenericResponse updateCompanyDocument(@PathVariable Long companyId, @RequestBody CompanyDocumentRequest companyDocumentRequest) {
        return companyDocumentService.updateCompanyDocument ( companyId, companyDocumentRequest );
    }
}

