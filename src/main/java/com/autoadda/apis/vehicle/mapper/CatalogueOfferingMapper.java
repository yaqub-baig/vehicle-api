package com.autoadda.apis.vehicle.mapper;

import com.autoadda.apis.vehicle.dao.CompanyRepository;
import com.autoadda.apis.vehicle.dao.MediaFileRepository;
import com.autoadda.apis.vehicle.entity.CatalogueOffering;
import com.autoadda.apis.vehicle.entity.Company;
import com.autoadda.apis.vehicle.entity.MediaFile;
import com.autoadda.apis.vehicle.enums.CatalogueOfferingStatus;
import com.autoadda.apis.vehicle.exception.InvalidInputException;
import com.autoadda.apis.vehicle.request.CatalogueOfferingRequest;
import com.autoadda.apis.vehicle.response.CatalogueOfferingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CatalogueOfferingMapper {

    private final MediaFileRepository mediaFileRepository;
    private final CompanyRepository companyRepository;

    public CatalogueOffering toEntity(CatalogueOfferingRequest request, Long companyId) {
        return CatalogueOffering.builder()
                .companyId(companyId)
                .brand(request.getBrand())
                .model(request.getModel())
                .trimName(request.getTrimName())
                .modelYear(request.getModelYear())
                .indicativePrice(request.getIndicativePrice())
                .description(request.getDescription())
                .images(resolveImages(request.getImageIds()))
                .status(CatalogueOfferingStatus.ACTIVE)
                .isDeleted(false)
                .build();
    }

    public void applyUpdate(CatalogueOffering offering, CatalogueOfferingRequest request) {
        offering.setBrand(request.getBrand());
        offering.setModel(request.getModel());
        offering.setTrimName(request.getTrimName());
        offering.setModelYear(request.getModelYear());
        offering.setIndicativePrice(request.getIndicativePrice());
        offering.setDescription(request.getDescription());
        if (request.getImageIds() != null) {
            offering.setImages(resolveImages(request.getImageIds()));
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            offering.setStatus(parseStatus(request.getStatus()));
        }
    }

    public CatalogueOfferingResponse toResponse(CatalogueOffering entity) {
        if (entity == null) {
            return null;
        }

        CatalogueOfferingResponse.CatalogueOfferingResponseBuilder builder = CatalogueOfferingResponse.builder()
                .id(entity.getId())
                .companyId(entity.getCompanyId())
                .brand(entity.getBrand())
                .model(entity.getModel())
                .trimName(entity.getTrimName())
                .modelYear(entity.getModelYear())
                .indicativePrice(entity.getIndicativePrice())
                .description(entity.getDescription())
                .images(entity.getImages())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        if (entity.getCompanyId() != null) {
            companyRepository.findById(entity.getCompanyId())
                    .map(Company::getName)
                    .ifPresent(builder::companyName);
        }

        return builder.build();
    }

    public List<CatalogueOfferingResponse> toResponseList(List<CatalogueOffering> entities) {
        return entities.stream().map(this::toResponse).toList();
    }

    public CatalogueOfferingStatus parseStatus(String value) {
        try {
            return CatalogueOfferingStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid catalogue offering status: " + value);
        }
    }

    private List<MediaFile> resolveImages(List<Long> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) {
            return null;
        }
        return mediaFileRepository.findAllById(imageIds);
    }
}
