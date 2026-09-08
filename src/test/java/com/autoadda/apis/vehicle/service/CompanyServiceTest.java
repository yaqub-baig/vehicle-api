package com.autoadda.apis.vehicle.service;

import com.autoadda.apis.vehicle.clients.AuthClient;
import com.autoadda.apis.vehicle.dao.CompanyRepository;
import com.autoadda.apis.vehicle.entity.Company;
import com.autoadda.apis.vehicle.enums.CompanyType;
import com.autoadda.apis.vehicle.exception.ConflictException;
import com.autoadda.apis.vehicle.exception.InvalidInputException;
import com.autoadda.apis.vehicle.exception.ResourceNotFoundException;
import com.autoadda.apis.vehicle.mapper.CompanyMapper;
import com.autoadda.apis.vehicle.request.CompanyRequest;
import com.autoadda.apis.vehicle.response.CompanyResponse;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.impl.CompanyService;
import com.autoadda.apis.vehicle.util.ERole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    private static final Integer OWNER_ID = 42;
    private static final Long COMPANY_ID = 7L;

    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private CompanyMapper companyMapper;
    @Mock
    private AuthClient identityClient;
    @InjectMocks
    private CompanyService companyService;

    private CompanyRequest request;

    @BeforeEach
    void setUp() {
        request = new CompanyRequest();
        request.setCompanyOwnerId(OWNER_ID);
        request.setName("Al Test Motors");
        request.setTradeLicenseNumber("TL-123");
    }

    private Company companyWithTypes(Set<CompanyType> types) {
        Company company = new Company();
        company.setId(COMPANY_ID);
        company.setCompanyOwnerId(OWNER_ID);
        company.setIsActive(true);
        company.setTypes(types);
        company.setAuthorizedBrands(new HashSet<>());
        return company;
    }

    @Test
    void createCompany_fleetOwner_savesAndUpgradesRole() {
        Company mapped = companyWithTypes(new HashSet<>(Set.of(CompanyType.FLEET_OWNER)));
        when(companyRepository.findByTradeLicenseNumber("TL-123")).thenReturn(Optional.empty());
        when(companyRepository.findByCompanyOwnerId(OWNER_ID)).thenReturn(List.of());
        when(companyMapper.toEntity(request)).thenReturn(mapped);
        when(companyRepository.save(mapped)).thenReturn(mapped);
        when(companyMapper.toResponse(mapped)).thenReturn(new CompanyResponse());

        GenericResponse response = companyService.createCompany(request);

        assertEquals("success", response.getMessage());
        verify(companyRepository).save(mapped);
        verify(identityClient, times(1)).updateRole(OWNER_ID, ERole.COMPANY_OWNER);
    }

    @Test
    void createCompany_newShowroomMissingShowroomAddress_rejectedBeforeSave() {
        Company mapped = companyWithTypes(new HashSet<>(Set.of(CompanyType.NEW_CAR_SHOWROOM)));
        mapped.setRtaApprovalNumber("RTA-1");
        mapped.setAuthorizedBrands(new HashSet<>(Set.of("BMW")));
        // showroomAddress deliberately null
        when(companyRepository.findByTradeLicenseNumber("TL-123")).thenReturn(Optional.empty());
        when(companyRepository.findByCompanyOwnerId(OWNER_ID)).thenReturn(List.of());
        when(companyMapper.toEntity(request)).thenReturn(mapped);

        assertThrows(InvalidInputException.class, () -> companyService.createCompany(request));

        verify(companyRepository, never()).save(any());
        verify(identityClient, never()).updateRole(any(), any());
    }

    @Test
    void createCompany_newShowroomWithAllFields_saved() {
        Company mapped = companyWithTypes(new HashSet<>(Set.of(CompanyType.NEW_CAR_SHOWROOM)));
        mapped.setShowroomAddress("Sheikh Zayed Road");
        mapped.setRtaApprovalNumber("RTA-1");
        mapped.setAuthorizedBrands(new HashSet<>(Set.of("BMW")));
        when(companyRepository.findByTradeLicenseNumber("TL-123")).thenReturn(Optional.empty());
        when(companyRepository.findByCompanyOwnerId(OWNER_ID)).thenReturn(List.of());
        when(companyMapper.toEntity(request)).thenReturn(mapped);
        when(companyRepository.save(mapped)).thenReturn(mapped);
        when(companyMapper.toResponse(mapped)).thenReturn(new CompanyResponse());

        GenericResponse response = companyService.createCompany(request);

        assertEquals("success", response.getMessage());
        verify(companyRepository).save(mapped);
    }

    @Test
    void createCompany_duplicateTradeLicense_rejected() {
        when(companyRepository.findByTradeLicenseNumber("TL-123"))
                .thenReturn(Optional.of(companyWithTypes(new HashSet<>(Set.of(CompanyType.FLEET_OWNER)))));

        assertThrows(ResourceNotFoundException.class, () -> companyService.createCompany(request));
        verify(companyRepository, never()).save(any());
    }

    @Test
    void createCompany_ownerAlreadyHasActiveCompany_conflict() {
        when(companyRepository.findByTradeLicenseNumber("TL-123")).thenReturn(Optional.empty());
        when(companyRepository.findByCompanyOwnerId(OWNER_ID))
                .thenReturn(List.of(companyWithTypes(new HashSet<>(Set.of(CompanyType.FLEET_OWNER)))));

        assertThrows(ConflictException.class, () -> companyService.createCompany(request));
        verify(companyRepository, never()).save(any());
    }

    @Test
    void updateCompany_typeSpecificValidationRunsAgainstResultingEntity() {
        Company existing = companyWithTypes(new HashSet<>(Set.of(CompanyType.NEW_CAR_SHOWROOM)));
        // ends up a showroom with no address -> must be rejected
        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.of(existing));
        when(companyMapper.updateEntity(existing, request)).thenReturn(existing);

        assertThrows(InvalidInputException.class, () -> companyService.updateCompany(COMPANY_ID, request));
        verify(companyRepository, never()).save(any());
    }

    @Test
    void updateCompany_happyPath_saves() {
        Company existing = companyWithTypes(new HashSet<>(Set.of(CompanyType.FLEET_OWNER)));
        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.of(existing));
        when(companyMapper.updateEntity(existing, request)).thenReturn(existing);
        when(companyRepository.save(existing)).thenReturn(existing);
        when(companyMapper.toResponse(existing)).thenReturn(new CompanyResponse());

        GenericResponse response = companyService.updateCompany(COMPANY_ID, request);

        assertEquals("success", response.getMessage());
        verify(companyRepository).save(existing);
        verify(identityClient, never()).updateRole(eq(OWNER_ID), any());
    }
}
