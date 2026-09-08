package com.autoadda.apis.vehicle.service;

import com.autoadda.apis.vehicle.dao.CatalogueOfferingRepository;
import com.autoadda.apis.vehicle.entity.CatalogueOffering;
import com.autoadda.apis.vehicle.enums.CatalogueOfferingStatus;
import com.autoadda.apis.vehicle.enums.CompanyType;
import com.autoadda.apis.vehicle.exception.UnauthorizedAccessException;
import com.autoadda.apis.vehicle.mapper.CatalogueOfferingMapper;
import com.autoadda.apis.vehicle.request.CatalogueOfferingRequest;
import com.autoadda.apis.vehicle.request.CatalogueSearchCriteria;
import com.autoadda.apis.vehicle.response.CatalogueOfferingResponse;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.impl.CatalogueOfferingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogueOfferingServiceTest {

    private static final Long COMPANY_ID = 3L;
    private static final Long OFFERING_ID = 55L;
    private static final Integer OWNER_ID = 9;

    @Mock
    private CatalogueOfferingRepository offeringRepository;
    @Mock
    private CatalogueOfferingMapper offeringMapper;
    @Mock
    private CompanyAuthorizationService companyAuth;
    @InjectMocks
    private CatalogueOfferingServiceImpl service;

    private CatalogueOfferingRequest request;

    @BeforeEach
    void setUp() {
        request = new CatalogueOfferingRequest();
        request.setBrand("BMW");
        request.setModel("X5");
        request.setModelYear(2026);
        request.setIndicativePrice(new BigDecimal("450000"));
    }

    private CatalogueOffering offering() {
        CatalogueOffering offering = new CatalogueOffering();
        offering.setId(OFFERING_ID);
        offering.setCompanyId(COMPANY_ID);
        offering.setStatus(CatalogueOfferingStatus.ACTIVE);
        return offering;
    }

    @Test
    void createOffering_happyPath_saves() {
        CatalogueOffering mapped = offering();
        when(offeringMapper.toEntity(request, COMPANY_ID)).thenReturn(mapped);
        when(offeringRepository.save(mapped)).thenReturn(mapped);
        when(offeringMapper.toResponse(mapped)).thenReturn(CatalogueOfferingResponse.builder().id(OFFERING_ID).build());

        GenericResponse response = service.createOffering(COMPANY_ID, OWNER_ID, request);

        assertEquals("201", response.getCode());
        verify(companyAuth).requireOwnerWithType(COMPANY_ID, OWNER_ID, CompanyType.NEW_CAR_SHOWROOM);
        verify(offeringRepository).save(mapped);
    }

    @Test
    void createOffering_callerNotShowroomOwner_rejectedBeforeSave() {
        doThrow(new UnauthorizedAccessException("This company is not a NEW_CAR_SHOWROOM"))
                .when(companyAuth).requireOwnerWithType(COMPANY_ID, OWNER_ID, CompanyType.NEW_CAR_SHOWROOM);

        assertThrows(UnauthorizedAccessException.class,
                () -> service.createOffering(COMPANY_ID, OWNER_ID, request));

        verify(offeringRepository, never()).save(any());
    }

    @Test
    void updateOffering_notFound_returns404() {
        when(offeringRepository.findByIdAndIsDeletedFalse(OFFERING_ID)).thenReturn(Optional.empty());

        GenericResponse response = service.updateOffering(OFFERING_ID, OWNER_ID, request);

        assertEquals("404", response.getCode());
        verify(companyAuth, never()).requireOwnerWithType(any(), any(), any());
    }

    @Test
    void updateOffering_happyPath_checksOwnershipOfTheOfferingsCompany() {
        CatalogueOffering existing = offering();
        when(offeringRepository.findByIdAndIsDeletedFalse(OFFERING_ID)).thenReturn(Optional.of(existing));
        when(offeringRepository.save(existing)).thenReturn(existing);
        when(offeringMapper.toResponse(existing)).thenReturn(CatalogueOfferingResponse.builder().build());

        GenericResponse response = service.updateOffering(OFFERING_ID, OWNER_ID, request);

        assertEquals("200", response.getCode());
        verify(companyAuth).requireOwnerWithType(COMPANY_ID, OWNER_ID, CompanyType.NEW_CAR_SHOWROOM);
        verify(offeringMapper).applyUpdate(existing, request);
    }

    @Test
    void setStatus_invalidStatusString_rejectedBeforeLookup() {
        when(offeringMapper.parseStatus("BOGUS"))
                .thenThrow(new com.autoadda.apis.vehicle.exception.InvalidInputException("Invalid catalogue offering status: BOGUS"));

        assertThrows(com.autoadda.apis.vehicle.exception.InvalidInputException.class,
                () -> service.setStatus(OFFERING_ID, OWNER_ID, "BOGUS"));

        verify(offeringRepository, never()).findByIdAndIsDeletedFalse(any());
    }

    @Test
    void browse_invalidPriceRange_returns400() {
        CatalogueSearchCriteria criteria = CatalogueSearchCriteria.builder()
                .minPrice(new BigDecimal("100"))
                // maxPrice missing -> validateRanges throws
                .build();

        GenericResponse response = service.browse(criteria);

        assertEquals("400", response.getCode());
        verify(offeringRepository, never())
                .browseOfferings(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void getOfferingById_notFound_returns404() {
        when(offeringRepository.findByIdAndIsDeletedFalse(OFFERING_ID)).thenReturn(Optional.empty());

        GenericResponse response = service.getOfferingById(OFFERING_ID);

        assertEquals("404", response.getCode());
    }

    @Test
    void getOfferingsForCompany_delegatesToRepository() {
        when(offeringRepository.findByCompanyIdAndIsDeletedFalseOrderByCreatedAtDesc(COMPANY_ID))
                .thenReturn(java.util.List.of());
        when(offeringMapper.toResponseList(any())).thenReturn(java.util.List.of());

        GenericResponse response = service.getOfferingsForCompany(COMPANY_ID);

        assertEquals("200", response.getCode());
        verify(offeringRepository).findByCompanyIdAndIsDeletedFalseOrderByCreatedAtDesc(eq(COMPANY_ID));
    }
}
