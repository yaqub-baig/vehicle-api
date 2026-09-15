package com.autoadda.apis.vehicle.service.impl;

import com.autoadda.apis.vehicle.entity.CatalogueQuoteRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Stub for catalogue quote-request notifications. Logs the events for now; wire up email / push
 * / SMS later using the same AuthClient machinery as NumberPlateNotificationService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogueQuoteNotificationService {

    @Async
    public void sendQuoteRequestCreated(CatalogueQuoteRequest quote) {
        log.info("[notify] quote request {} created for offering {} (showroom company {}) by buyer {}",
                quote.getId(), quote.getOfferingId(), quote.getCompanyId(), quote.getBuyerUserId());
    }

    @Async
    public void sendQuoteRequestAccepted(CatalogueQuoteRequest quote) {
        log.info("[notify] quote request {} accepted; contact details shared between buyer {} and showroom company {}",
                quote.getId(), quote.getBuyerUserId(), quote.getCompanyId());
    }

    @Async
    public void sendQuoteRequestRejected(CatalogueQuoteRequest quote) {
        log.info("[notify] quote request {} rejected (reason: {})", quote.getId(), quote.getRejectionReason());
    }

    @Async
    public void sendQuoteRequestCancelled(CatalogueQuoteRequest quote) {
        log.info("[notify] quote request {} cancelled by buyer {}", quote.getId(), quote.getBuyerUserId());
    }
}
