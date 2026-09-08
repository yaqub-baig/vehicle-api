package com.autoadda.apis.vehicle.service;

import com.autoadda.apis.vehicle.request.CatalogueOfferingRequest;
import com.autoadda.apis.vehicle.request.CatalogueSearchCriteria;
import com.autoadda.apis.vehicle.response.GenericResponse;

public interface CatalogueOfferingService {

    GenericResponse createOffering(Long companyId, Integer callerId, CatalogueOfferingRequest request);

    GenericResponse updateOffering(Long offeringId, Integer callerId, CatalogueOfferingRequest request);

    GenericResponse setStatus(Long offeringId, Integer callerId, String status);

    GenericResponse getOfferingById(Long offeringId);

    GenericResponse getOfferingsForCompany(Long companyId);

    GenericResponse browse(CatalogueSearchCriteria criteria);
}
