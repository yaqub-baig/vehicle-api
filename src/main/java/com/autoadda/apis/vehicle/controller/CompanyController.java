package com.autoadda.apis.vehicle.controller;

import com.autoadda.apis.vehicle.request.CompanyRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.impl.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/company")
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    public GenericResponse createCompany(@RequestBody CompanyRequest companyRequest) {
        return companyService.createCompany ( companyRequest );
    }

    @GetMapping("/all")
    public GenericResponse getAllCompanies() {
        return companyService.getAllCompanies ( );
    }

    @GetMapping("/{id}")
    public GenericResponse getCompanyById(@PathVariable Long id) {
        return companyService.getCompanyById ( id );
    }

    @GetMapping("/{id}/owner")
    public GenericResponse getCompanyByOwnerId(@PathVariable Integer id) {
        return companyService.getCompanyByOwnerId ( id );
    }


    @PutMapping("/{id}")
    public GenericResponse updateCompany(@PathVariable Long id, @RequestBody CompanyRequest companyRequest) {
        return companyService.updateCompany ( id, companyRequest );
    }

    @DeleteMapping("/{id}")
    public GenericResponse deleteCompany(@PathVariable Long id) {
        return companyService.deleteCompany ( id );
    }

}
