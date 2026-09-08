package com.autoadda.apis.vehicle.service.impl;

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
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.CatalogueQuoteRequestService;
import com.autoadda.apis.vehicle.service.CompanyAuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogueQuoteRequestServiceImpl implements CatalogueQuoteRequestService {

    private static final String ERROR = "error";
    private static final String SUCCESS = "success";
    private static final String QUOTE_NOT_FOUND = "Quote request not found";
    private static final String OFFERING_NOT_FOUND = "Catalogue offering not found";
    private static final String ONLY_SHOWROOM_CAN_RESPOND = "Only the showroom can respond to this quote request";
    private static final String ALREADY_RESPONDED = "This quote request has already been responded to";

    private final CatalogueQuoteRequestRepository quoteRepository;
    private final CatalogueOfferingRepository offeringRepository;
    private final CatalogueQuoteRequestMapper quoteMapper;
    private final CompanyAuthorizationService companyAuth;
    private final CatalogueQuoteNotificationService notificationService;

    @Override
    @Transactional
    public GenericResponse createQuoteRequest(CatalogueQuoteRequestRequest request, Integer buyerUserId) {
        log.info("Creating quote request for offering {} by user {}", request.getOfferingId(), buyerUserId);

        Optional<CatalogueOffering> offeringOpt = offeringRepository.findByIdAndIsDeletedFalse(request.getOfferingId());
        if (offeringOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(OFFERING_NOT_FOUND).build();
        }

        CatalogueOffering offering = offeringOpt.get();

        if (offering.getStatus() != CatalogueOfferingStatus.ACTIVE) {
            return GenericResponse.builder()
                    .status(ERROR).code("400")
                    .message("This offering is no longer active")
                    .build();
        }

        if (companyAuth.isOwner(offering.getCompanyId(), buyerUserId)) {
            return GenericResponse.builder()
                    .status(ERROR).code("400")
                    .message("You cannot request a quote on your own showroom")
                    .build();
        }

        boolean alreadyRequested = quoteRepository.existsByBuyerUserIdAndOfferingIdAndStatusInAndIsDeletedFalse(
                buyerUserId, offering.getId(),
                List.of(CatalogueQuoteRequestStatus.PENDING, CatalogueQuoteRequestStatus.ACCEPTED));
        if (alreadyRequested) {
            return GenericResponse.builder()
                    .status(ERROR).code("400")
                    .message("You already have an active quote request for this offering")
                    .build();
        }

        CatalogueQuoteRequest quote = quoteMapper.toEntity(request, buyerUserId, offering.getCompanyId());
        CatalogueQuoteRequest saved = quoteRepository.save(quote);

        log.info("Quote request created with ID: {}", saved.getId());

        notificationService.sendQuoteRequestCreated(saved);

        return GenericResponse.builder()
                .status(SUCCESS).code("201")
                .message("Quote request sent to the showroom")
                .body(quoteMapper.toResponse(saved, offering))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse acceptQuoteRequest(Long quoteRequestId, Integer callerId, CatalogueQuoteResponseRequest response) {
        Optional<CatalogueQuoteRequest> quoteOpt = quoteRepository.findByIdAndIsDeletedFalse(quoteRequestId);
        if (quoteOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(QUOTE_NOT_FOUND).build();
        }

        CatalogueQuoteRequest quote = quoteOpt.get();

        if (!companyAuth.isOwner(quote.getCompanyId(), callerId)) {
            return GenericResponse.builder().status(ERROR).code("403").message(ONLY_SHOWROOM_CAN_RESPOND).build();
        }

        if (quote.getStatus() != CatalogueQuoteRequestStatus.PENDING) {
            return GenericResponse.builder().status(ERROR).code("400").message(ALREADY_RESPONDED).build();
        }

        quote.setStatus(CatalogueQuoteRequestStatus.ACCEPTED);
        quote.setRespondedAt(LocalDateTime.now());
        if (response != null) {
            quote.setResponseMessage(response.getResponseMessage());
            quote.setQuotedPrice(response.getQuotedPrice());
        }
        CatalogueQuoteRequest saved = quoteRepository.save(quote);

        notificationService.sendQuoteRequestAccepted(saved);

        return GenericResponse.builder()
                .status(SUCCESS).code("200")
                .message("Quote accepted. Contact details have been shared with both parties.")
                .body(mapWithOffering(saved))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse rejectQuoteRequest(Long quoteRequestId, Integer callerId, CatalogueQuoteRejectionRequest request) {
        Optional<CatalogueQuoteRequest> quoteOpt = quoteRepository.findByIdAndIsDeletedFalse(quoteRequestId);
        if (quoteOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(QUOTE_NOT_FOUND).build();
        }

        CatalogueQuoteRequest quote = quoteOpt.get();

        if (!companyAuth.isOwner(quote.getCompanyId(), callerId)) {
            return GenericResponse.builder().status(ERROR).code("403").message(ONLY_SHOWROOM_CAN_RESPOND).build();
        }

        if (quote.getStatus() != CatalogueQuoteRequestStatus.PENDING) {
            return GenericResponse.builder().status(ERROR).code("400").message(ALREADY_RESPONDED).build();
        }

        quote.setStatus(CatalogueQuoteRequestStatus.REJECTED);
        quote.setRespondedAt(LocalDateTime.now());
        if (request != null) {
            quote.setRejectionReason(request.getRejectionReason());
        }
        CatalogueQuoteRequest saved = quoteRepository.save(quote);

        notificationService.sendQuoteRequestRejected(saved);

        return GenericResponse.builder()
                .status(SUCCESS).code("200")
                .message("Quote request declined")
                .body(mapWithOffering(saved))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse cancelQuoteRequest(Long quoteRequestId, Integer buyerUserId) {
        Optional<CatalogueQuoteRequest> quoteOpt = quoteRepository.findByIdAndIsDeletedFalse(quoteRequestId);
        if (quoteOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(QUOTE_NOT_FOUND).build();
        }

        CatalogueQuoteRequest quote = quoteOpt.get();

        if (!quote.getBuyerUserId().equals(buyerUserId)) {
            return GenericResponse.builder()
                    .status(ERROR).code("403")
                    .message("Only the buyer can cancel this quote request")
                    .build();
        }

        if (quote.getStatus() != CatalogueQuoteRequestStatus.PENDING) {
            return GenericResponse.builder()
                    .status(ERROR).code("400")
                    .message("Only pending quote requests can be cancelled")
                    .build();
        }

        quote.setStatus(CatalogueQuoteRequestStatus.CANCELLED);
        quote.setRespondedAt(LocalDateTime.now());
        CatalogueQuoteRequest saved = quoteRepository.save(quote);

        notificationService.sendQuoteRequestCancelled(saved);

        return GenericResponse.builder()
                .status(SUCCESS).code("200")
                .message("Quote request cancelled")
                .body(mapWithOffering(saved))
                .build();
    }

    @Override
    public GenericResponse getQuoteRequestById(Long quoteRequestId, Integer userId) {
        Optional<CatalogueQuoteRequest> quoteOpt = quoteRepository.findByIdAndIsDeletedFalse(quoteRequestId);
        if (quoteOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(QUOTE_NOT_FOUND).build();
        }

        CatalogueQuoteRequest quote = quoteOpt.get();

        boolean participant = quote.getBuyerUserId().equals(userId)
                || companyAuth.isOwner(quote.getCompanyId(), userId);
        if (!participant) {
            return GenericResponse.builder()
                    .status(ERROR).code("403")
                    .message("You are not a participant in this quote request")
                    .build();
        }

        return GenericResponse.builder()
                .status(SUCCESS).code("200")
                .message("Quote request retrieved successfully")
                .body(mapWithOffering(quote))
                .build();
    }

    @Override
    public GenericResponse getMyQuoteRequests(Integer buyerUserId, CatalogueQuoteRequestStatus statusFilter) {
        List<CatalogueQuoteRequest> quotes = statusFilter != null
                ? quoteRepository.findAllForBuyerAndStatus(buyerUserId, statusFilter)
                : quoteRepository.findAllForBuyer(buyerUserId);

        return GenericResponse.builder()
                .status(SUCCESS).code("200")
                .message("Quote requests retrieved successfully")
                .body(quotes.stream().map(this::mapWithOffering).toList())
                .build();
    }

    @Override
    public GenericResponse getQuoteRequestsForOffering(Long offeringId, Integer callerId) {
        Optional<CatalogueOffering> offeringOpt = offeringRepository.findByIdAndIsDeletedFalse(offeringId);
        if (offeringOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(OFFERING_NOT_FOUND).build();
        }

        CatalogueOffering offering = offeringOpt.get();
        companyAuth.requireOwner(offering.getCompanyId(), callerId);

        List<CatalogueQuoteRequest> quotes =
                quoteRepository.findByOfferingIdAndIsDeletedFalseOrderByCreatedAtDesc(offeringId);

        return GenericResponse.builder()
                .status(SUCCESS).code("200")
                .message("Quote requests retrieved successfully")
                .body(quotes.stream().map(q -> quoteMapper.toResponse(q, offering)).toList())
                .build();
    }

    @Override
    public GenericResponse getQuoteRequestsForCompany(Long companyId, Integer callerId) {
        companyAuth.requireOwner(companyId, callerId);

        List<CatalogueQuoteRequest> quotes = quoteRepository.findAllForCompany(companyId);

        return GenericResponse.builder()
                .status(SUCCESS).code("200")
                .message("Quote requests retrieved successfully")
                .body(quotes.stream().map(this::mapWithOffering).toList())
                .build();
    }

    private com.autoadda.apis.vehicle.response.CatalogueQuoteRequestResponse mapWithOffering(CatalogueQuoteRequest quote) {
        CatalogueOffering offering = offeringRepository.findById(quote.getOfferingId()).orElse(null);
        return quoteMapper.toResponse(quote, offering);
    }
}
