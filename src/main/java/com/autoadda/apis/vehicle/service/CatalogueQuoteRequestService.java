package com.autoadda.apis.vehicle.service;

import com.autoadda.apis.vehicle.enums.CatalogueQuoteRequestStatus;
import com.autoadda.apis.vehicle.request.CatalogueQuoteRejectionRequest;
import com.autoadda.apis.vehicle.request.CatalogueQuoteRequestRequest;
import com.autoadda.apis.vehicle.request.CatalogueQuoteResponseRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;

public interface CatalogueQuoteRequestService {

    GenericResponse createQuoteRequest(CatalogueQuoteRequestRequest request, Integer buyerUserId);

    GenericResponse acceptQuoteRequest(Long quoteRequestId, Integer callerId, CatalogueQuoteResponseRequest response);

    GenericResponse rejectQuoteRequest(Long quoteRequestId, Integer callerId, CatalogueQuoteRejectionRequest request);

    GenericResponse cancelQuoteRequest(Long quoteRequestId, Integer buyerUserId);

    GenericResponse getQuoteRequestById(Long quoteRequestId, Integer userId);

    GenericResponse getMyQuoteRequests(Integer buyerUserId, CatalogueQuoteRequestStatus statusFilter);

    GenericResponse getQuoteRequestsForOffering(Long offeringId, Integer callerId);

    GenericResponse getQuoteRequestsForCompany(Long companyId, Integer callerId);
}
