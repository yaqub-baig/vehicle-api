package com.autoadda.apis.vehicle.service;

import com.autoadda.apis.vehicle.dao.CatalogueOfferingRepository;
import com.autoadda.apis.vehicle.dao.CatalogueQuoteRequestRepository;
import com.autoadda.apis.vehicle.entity.CatalogueOffering;
import com.autoadda.apis.vehicle.entity.CatalogueQuoteRequest;
import com.autoadda.apis.vehicle.enums.CatalogueOfferingStatus;
import com.autoadda.apis.vehicle.enums.CatalogueQuoteRequestStatus;
import com.autoadda.apis.vehicle.mapper.CatalogueQuoteRequestMapper;
import com.autoadda.apis.vehicle.request.CatalogueQuoteRejectionRequest;
import com.autoadda.apis.vehicle.request.CatalogueQuoteRequestRequest;
import com.autoadda.apis.vehicle.request.CatalogueQuoteResponseRequest;
import com.autoadda.apis.vehicle.response.CatalogueQuoteRequestResponse;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.impl.CatalogueQuoteNotificationService;
import com.autoadda.apis.vehicle.service.impl.CatalogueQuoteRequestServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogueQuoteRequestServiceTest {

    private static final Long OFFERING_ID = 20L;
    private static final Long COMPANY_ID = 4L;
    private static final Long QUOTE_ID = 100L;
    private static final Integer BUYER_ID = 77;
    private static final Integer SHOWROOM_OWNER_ID = 5;

    @Mock
    private CatalogueQuoteRequestRepository quoteRepository;
    @Mock
    private CatalogueOfferingRepository offeringRepository;
    @Mock
    private CatalogueQuoteRequestMapper quoteMapper;
    @Mock
    private CompanyAuthorizationService companyAuth;
    @Mock
    private CatalogueQuoteNotificationService notificationService;
    @InjectMocks
    private CatalogueQuoteRequestServiceImpl service;

    private CatalogueQuoteRequestRequest request;

    @BeforeEach
    void setUp() {
        request = new CatalogueQuoteRequestRequest();
        request.setOfferingId(OFFERING_ID);
        request.setMessage("Best price on this trim?");
        lenient().when(quoteMapper.toResponse(any(), any())).thenReturn(CatalogueQuoteRequestResponse.builder().build());
    }

    private CatalogueOffering activeOffering() {
        CatalogueOffering offering = new CatalogueOffering();
        offering.setId(OFFERING_ID);
        offering.setCompanyId(COMPANY_ID);
        offering.setStatus(CatalogueOfferingStatus.ACTIVE);
        return offering;
    }

    private CatalogueQuoteRequest pendingQuote() {
        return CatalogueQuoteRequest.builder()
                .id(QUOTE_ID)
                .offeringId(OFFERING_ID)
                .companyId(COMPANY_ID)
                .buyerUserId(BUYER_ID)
                .status(CatalogueQuoteRequestStatus.PENDING)
                .build();
    }

    @Test
    void create_offeringNotFound_returns404() {
        when(offeringRepository.findByIdAndIsDeletedFalse(OFFERING_ID)).thenReturn(Optional.empty());

        GenericResponse response = service.createQuoteRequest(request, BUYER_ID);

        assertEquals("404", response.getCode());
        verify(quoteRepository, never()).save(any());
    }

    @Test
    void create_offeringInactive_returns400() {
        CatalogueOffering offering = activeOffering();
        offering.setStatus(CatalogueOfferingStatus.INACTIVE);
        when(offeringRepository.findByIdAndIsDeletedFalse(OFFERING_ID)).thenReturn(Optional.of(offering));

        GenericResponse response = service.createQuoteRequest(request, BUYER_ID);

        assertEquals("400", response.getCode());
    }

    @Test
    void create_onOwnShowroom_returns400() {
        when(offeringRepository.findByIdAndIsDeletedFalse(OFFERING_ID)).thenReturn(Optional.of(activeOffering()));
        when(companyAuth.isOwner(COMPANY_ID, BUYER_ID)).thenReturn(true);

        GenericResponse response = service.createQuoteRequest(request, BUYER_ID);

        assertEquals("400", response.getCode());
        verify(quoteRepository, never()).save(any());
    }

    @Test
    void create_duplicateActiveRequest_returns400() {
        when(offeringRepository.findByIdAndIsDeletedFalse(OFFERING_ID)).thenReturn(Optional.of(activeOffering()));
        when(companyAuth.isOwner(COMPANY_ID, BUYER_ID)).thenReturn(false);
        when(quoteRepository.existsByBuyerUserIdAndOfferingIdAndStatusInAndIsDeletedFalse(
                eq(BUYER_ID), eq(OFFERING_ID), anyList())).thenReturn(true);

        GenericResponse response = service.createQuoteRequest(request, BUYER_ID);

        assertEquals("400", response.getCode());
    }

    @Test
    void create_happyPath_savesWithDenormalizedCompanyId() {
        when(offeringRepository.findByIdAndIsDeletedFalse(OFFERING_ID)).thenReturn(Optional.of(activeOffering()));
        when(companyAuth.isOwner(COMPANY_ID, BUYER_ID)).thenReturn(false);
        when(quoteRepository.existsByBuyerUserIdAndOfferingIdAndStatusInAndIsDeletedFalse(
                eq(BUYER_ID), eq(OFFERING_ID), anyList())).thenReturn(false);
        CatalogueQuoteRequest mapped = pendingQuote();
        when(quoteMapper.toEntity(request, BUYER_ID, COMPANY_ID)).thenReturn(mapped);
        when(quoteRepository.save(mapped)).thenReturn(mapped);

        GenericResponse response = service.createQuoteRequest(request, BUYER_ID);

        assertEquals("201", response.getCode());
        verify(quoteMapper).toEntity(request, BUYER_ID, COMPANY_ID);
        verify(notificationService).sendQuoteRequestCreated(mapped);
    }

    @Test
    void accept_notShowroomOwner_returns403() {
        when(quoteRepository.findByIdAndIsDeletedFalse(QUOTE_ID)).thenReturn(Optional.of(pendingQuote()));
        when(companyAuth.isOwner(COMPANY_ID, SHOWROOM_OWNER_ID)).thenReturn(false);

        GenericResponse response = service.acceptQuoteRequest(QUOTE_ID, SHOWROOM_OWNER_ID, new CatalogueQuoteResponseRequest());

        assertEquals("403", response.getCode());
        verify(quoteRepository, never()).save(any());
    }

    @Test
    void accept_alreadyResponded_returns400() {
        CatalogueQuoteRequest quote = pendingQuote();
        quote.setStatus(CatalogueQuoteRequestStatus.REJECTED);
        when(quoteRepository.findByIdAndIsDeletedFalse(QUOTE_ID)).thenReturn(Optional.of(quote));
        when(companyAuth.isOwner(COMPANY_ID, SHOWROOM_OWNER_ID)).thenReturn(true);

        GenericResponse response = service.acceptQuoteRequest(QUOTE_ID, SHOWROOM_OWNER_ID, new CatalogueQuoteResponseRequest());

        assertEquals("400", response.getCode());
    }

    @Test
    void accept_happyPath_setsQuoteFields() {
        CatalogueQuoteRequest quote = pendingQuote();
        when(quoteRepository.findByIdAndIsDeletedFalse(QUOTE_ID)).thenReturn(Optional.of(quote));
        when(companyAuth.isOwner(COMPANY_ID, SHOWROOM_OWNER_ID)).thenReturn(true);
        when(quoteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.of(activeOffering()));

        CatalogueQuoteResponseRequest body = new CatalogueQuoteResponseRequest();
        body.setResponseMessage("AED 440,000 all-in");
        body.setQuotedPrice(new BigDecimal("440000"));

        GenericResponse response = service.acceptQuoteRequest(QUOTE_ID, SHOWROOM_OWNER_ID, body);

        assertEquals("200", response.getCode());
        ArgumentCaptor<CatalogueQuoteRequest> captor = ArgumentCaptor.forClass(CatalogueQuoteRequest.class);
        verify(quoteRepository).save(captor.capture());
        CatalogueQuoteRequest saved = captor.getValue();
        assertEquals(CatalogueQuoteRequestStatus.ACCEPTED, saved.getStatus());
        assertEquals("AED 440,000 all-in", saved.getResponseMessage());
        assertEquals(new BigDecimal("440000"), saved.getQuotedPrice());
        verify(notificationService).sendQuoteRequestAccepted(any());
    }

    @Test
    void reject_happyPath_setsReason() {
        CatalogueQuoteRequest quote = pendingQuote();
        when(quoteRepository.findByIdAndIsDeletedFalse(QUOTE_ID)).thenReturn(Optional.of(quote));
        when(companyAuth.isOwner(COMPANY_ID, SHOWROOM_OWNER_ID)).thenReturn(true);
        when(quoteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.of(activeOffering()));

        CatalogueQuoteRejectionRequest body = new CatalogueQuoteRejectionRequest();
        body.setRejectionReason("Model not available this quarter");

        GenericResponse response = service.rejectQuoteRequest(QUOTE_ID, SHOWROOM_OWNER_ID, body);

        assertEquals("200", response.getCode());
        ArgumentCaptor<CatalogueQuoteRequest> captor = ArgumentCaptor.forClass(CatalogueQuoteRequest.class);
        verify(quoteRepository).save(captor.capture());
        assertEquals(CatalogueQuoteRequestStatus.REJECTED, captor.getValue().getStatus());
        assertEquals("Model not available this quarter", captor.getValue().getRejectionReason());
    }

    @Test
    void cancel_notBuyer_returns403() {
        when(quoteRepository.findByIdAndIsDeletedFalse(QUOTE_ID)).thenReturn(Optional.of(pendingQuote()));

        GenericResponse response = service.cancelQuoteRequest(QUOTE_ID, 999);

        assertEquals("403", response.getCode());
        verify(quoteRepository, never()).save(any());
    }

    @Test
    void cancel_notPending_returns400() {
        CatalogueQuoteRequest quote = pendingQuote();
        quote.setStatus(CatalogueQuoteRequestStatus.ACCEPTED);
        when(quoteRepository.findByIdAndIsDeletedFalse(QUOTE_ID)).thenReturn(Optional.of(quote));

        GenericResponse response = service.cancelQuoteRequest(QUOTE_ID, BUYER_ID);

        assertEquals("400", response.getCode());
    }

    @Test
    void getById_nonParticipant_returns403() {
        when(quoteRepository.findByIdAndIsDeletedFalse(QUOTE_ID)).thenReturn(Optional.of(pendingQuote()));
        when(companyAuth.isOwner(COMPANY_ID, 999)).thenReturn(false);

        GenericResponse response = service.getQuoteRequestById(QUOTE_ID, 999);

        assertEquals("403", response.getCode());
    }

    @Test
    void getQuoteRequestsForOffering_checksOwnerOfOfferingsCompany() {
        when(offeringRepository.findByIdAndIsDeletedFalse(OFFERING_ID)).thenReturn(Optional.of(activeOffering()));
        when(quoteRepository.findByOfferingIdAndIsDeletedFalseOrderByCreatedAtDesc(OFFERING_ID))
                .thenReturn(List.of());

        GenericResponse response = service.getQuoteRequestsForOffering(OFFERING_ID, SHOWROOM_OWNER_ID);

        assertEquals("200", response.getCode());
        verify(companyAuth).requireOwner(COMPANY_ID, SHOWROOM_OWNER_ID);
    }

    @Test
    void mapper_contactFieldsNullBeforeAcceptance() {
        // Sanity check on the response contract expectation (mapper is mocked elsewhere,
        // here we verify the DTO default).
        CatalogueQuoteRequestResponse dto = CatalogueQuoteRequestResponse.builder()
                .status(CatalogueQuoteRequestStatus.PENDING)
                .build();
        assertNull(dto.getBuyerContactEmail());
        assertNull(dto.getShowroomContactEmail());
    }
}
