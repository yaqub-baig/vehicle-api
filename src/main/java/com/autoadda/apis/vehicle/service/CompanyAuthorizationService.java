package com.autoadda.apis.vehicle.service;

import com.autoadda.apis.vehicle.dao.CompanyRepository;
import com.autoadda.apis.vehicle.entity.Company;
import com.autoadda.apis.vehicle.enums.CompanyType;
import com.autoadda.apis.vehicle.exception.ResourceNotFoundException;
import com.autoadda.apis.vehicle.exception.UnauthorizedAccessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Shared "is this caller the owner of that company?" checks, reused by the catalogue,
 * quote-request and vehicle-sale flows. Mirrors CompanyDriverServiceImpl.requireCompanyOwner.
 */
@Component
@RequiredArgsConstructor
public class CompanyAuthorizationService {

    private static final String COMPANY_NOT_FOUND = "Company not found";

    private final CompanyRepository companyRepository;

    public Company requireOwner(Long companyId, Integer callerId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(COMPANY_NOT_FOUND));
        if (!callerId.equals(company.getCompanyOwnerId())) {
            throw new UnauthorizedAccessException("Only the company owner can perform this action");
        }
        return company;
    }

    public Company requireOwnerWithType(Long companyId, Integer callerId, CompanyType type) {
        Company company = requireOwner(companyId, callerId);
        if (company.getTypes() == null || !company.getTypes().contains(type)) {
            throw new UnauthorizedAccessException("This company is not a " + type.name());
        }
        return company;
    }

    public boolean isOwner(Long companyId, Integer callerId) {
        if (companyId == null || callerId == null) {
            return false;
        }
        return companyRepository.findById(companyId)
                .map(c -> callerId.equals(c.getCompanyOwnerId()))
                .orElse(false);
    }

    public boolean isOwnerWithType(Long companyId, Integer callerId, CompanyType type) {
        if (companyId == null || callerId == null) {
            return false;
        }
        return companyRepository.findById(companyId)
                .map(c -> callerId.equals(c.getCompanyOwnerId())
                        && c.getTypes() != null && c.getTypes().contains(type))
                .orElse(false);
    }
}
