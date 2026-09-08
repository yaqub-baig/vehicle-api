package com.autoadda.apis.vehicle.service.impl;

import com.autoadda.apis.vehicle.dao.CatalogueOfferingRepository;
import com.autoadda.apis.vehicle.entity.CatalogueOffering;
import com.autoadda.apis.vehicle.enums.CatalogueOfferingStatus;
import com.autoadda.apis.vehicle.enums.CompanyType;
import com.autoadda.apis.vehicle.exception.InvalidInputException;
import com.autoadda.apis.vehicle.mapper.CatalogueOfferingMapper;
import com.autoadda.apis.vehicle.request.CatalogueOfferingRequest;
import com.autoadda.apis.vehicle.request.CatalogueSearchCriteria;
import com.autoadda.apis.vehicle.response.CatalogueOfferingResponse;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.response.PagedResponse;
import com.autoadda.apis.vehicle.service.CatalogueOfferingService;
import com.autoadda.apis.vehicle.service.CompanyAuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogueOfferingServiceImpl implements CatalogueOfferingService {

    private static final String ERROR = "error";
    private static final String SUCCESS = "success";
    private static final String OFFERING_NOT_FOUND = "Catalogue offering not found";

    private final CatalogueOfferingRepository offeringRepository;
    private final CatalogueOfferingMapper offeringMapper;
    private final CompanyAuthorizationService companyAuth;

    @Override
    @Transactional
    public GenericResponse createOffering(Long companyId, Integer callerId, CatalogueOfferingRequest request) {
        log.info("Creating catalogue offering for company {} by user {}", companyId, callerId);

        companyAuth.requireOwnerWithType(companyId, callerId, CompanyType.NEW_CAR_SHOWROOM);

        CatalogueOffering offering = offeringMapper.toEntity(request, companyId);
        CatalogueOffering saved = offeringRepository.save(offering);

        log.info("Catalogue offering created with ID: {}", saved.getId());

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("201")
                .message("Catalogue offering created successfully")
                .body(offeringMapper.toResponse(saved))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse updateOffering(Long offeringId, Integer callerId, CatalogueOfferingRequest request) {
        Optional<CatalogueOffering> offeringOpt = offeringRepository.findByIdAndIsDeletedFalse(offeringId);
        if (offeringOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(OFFERING_NOT_FOUND).build();
        }

        CatalogueOffering offering = offeringOpt.get();
        companyAuth.requireOwnerWithType(offering.getCompanyId(), callerId, CompanyType.NEW_CAR_SHOWROOM);

        offeringMapper.applyUpdate(offering, request);
        CatalogueOffering saved = offeringRepository.save(offering);

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Catalogue offering updated successfully")
                .body(offeringMapper.toResponse(saved))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse setStatus(Long offeringId, Integer callerId, String status) {
        CatalogueOfferingStatus newStatus = offeringMapper.parseStatus(status);

        Optional<CatalogueOffering> offeringOpt = offeringRepository.findByIdAndIsDeletedFalse(offeringId);
        if (offeringOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(OFFERING_NOT_FOUND).build();
        }

        CatalogueOffering offering = offeringOpt.get();
        companyAuth.requireOwnerWithType(offering.getCompanyId(), callerId, CompanyType.NEW_CAR_SHOWROOM);

        offering.setStatus(newStatus);
        CatalogueOffering saved = offeringRepository.save(offering);

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Catalogue offering status updated to " + newStatus)
                .body(offeringMapper.toResponse(saved))
                .build();
    }

    @Override
    public GenericResponse getOfferingById(Long offeringId) {
        Optional<CatalogueOffering> offeringOpt = offeringRepository.findByIdAndIsDeletedFalse(offeringId);
        if (offeringOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(OFFERING_NOT_FOUND).build();
        }

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Catalogue offering retrieved successfully")
                .body(offeringMapper.toResponse(offeringOpt.get()))
                .build();
    }

    @Override
    public GenericResponse getOfferingsForCompany(Long companyId) {
        List<CatalogueOffering> offerings =
                offeringRepository.findByCompanyIdAndIsDeletedFalseOrderByCreatedAtDesc(companyId);

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Catalogue offerings retrieved successfully")
                .body(offeringMapper.toResponseList(offerings))
                .build();
    }

    @Override
    public GenericResponse browse(CatalogueSearchCriteria criteria) {
        String sortBy;
        String sortDirection;
        try {
            criteria.validateRanges();
            sortBy = criteria.getSortByOrDefault();
            sortDirection = criteria.getSortDirectionOrDefault();
        } catch (InvalidInputException | IllegalArgumentException e) {
            return GenericResponse.builder().status(ERROR).code("400").message(e.getMessage()).build();
        }

        PageRequest pageRequest = PageRequest.of(
                criteria.getPageOrDefault(),
                criteria.getSizeOrDefault(),
                Sort.by(Sort.Direction.fromString(sortDirection), sortBy));

        Page<CatalogueOffering> page = offeringRepository.browseOfferings(
                criteria.getCompanyId(),
                emptyToNull(criteria.getBrand()),
                emptyToNull(criteria.getModel()),
                criteria.getMinPrice(),
                criteria.getMaxPrice(),
                criteria.getFromYear(),
                criteria.getToYear(),
                pageRequest);

        List<CatalogueOfferingResponse> content = page.getContent().stream()
                .map(offeringMapper::toResponse)
                .toList();

        PagedResponse<CatalogueOfferingResponse> pagedResponse = PagedResponse.<CatalogueOfferingResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .numberOfElements(page.getNumberOfElements())
                .build();

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Catalogue offerings retrieved successfully")
                .body(pagedResponse)
                .build();
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
