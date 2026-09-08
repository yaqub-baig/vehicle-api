package com.autoadda.apis.vehicle.mapper;

import com.autoadda.apis.vehicle.clients.AuthClient;
import com.autoadda.apis.vehicle.dao.CompanyRepository;
import com.autoadda.apis.vehicle.entity.CatalogueOffering;
import com.autoadda.apis.vehicle.entity.CatalogueQuoteRequest;
import com.autoadda.apis.vehicle.enums.CatalogueQuoteRequestStatus;
import com.autoadda.apis.vehicle.request.CatalogueQuoteRequestRequest;
import com.autoadda.apis.vehicle.response.CatalogueQuoteRequestResponse;
import com.autoadda.apis.vehicle.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CatalogueQuoteRequestMapper {

    private final AuthClient authClient;
    private final CompanyRepository companyRepository;
    private final CatalogueOfferingMapper offeringMapper;

    public CatalogueQuoteRequest toEntity(CatalogueQuoteRequestRequest request, Integer buyerUserId, Long companyId) {
        return CatalogueQuoteRequest.builder()
                .offeringId(request.getOfferingId())
                .companyId(companyId)
                .buyerUserId(buyerUserId)
                .message(request.getMessage())
                .status(CatalogueQuoteRequestStatus.PENDING)
                .isDeleted(false)
                .build();
    }

    public CatalogueQuoteRequestResponse toResponse(CatalogueQuoteRequest entity, CatalogueOffering offering) {
        if (entity == null) {
            return null;
        }

        CatalogueQuoteRequestResponse.CatalogueQuoteRequestResponseBuilder builder =
                CatalogueQuoteRequestResponse.builder()
                        .id(entity.getId())
                        .offeringId(entity.getOfferingId())
                        .companyId(entity.getCompanyId())
                        .buyerUserId(entity.getBuyerUserId())
                        .status(entity.getStatus())
                        .message(entity.getMessage())
                        .responseMessage(entity.getResponseMessage())
                        .quotedPrice(entity.getQuotedPrice())
                        .rejectionReason(entity.getRejectionReason())
                        .respondedAt(entity.getRespondedAt())
                        .createdAt(entity.getCreatedAt())
                        .updatedAt(entity.getUpdatedAt())
                        .offering(offeringMapper.toResponse(offering));

        try {
            UserResponse buyer = authClient.getUser(entity.getBuyerUserId());
            if (buyer != null) {
                builder.buyerName(buyer.getName());
            }
        } catch (Exception e) {
            log.warn("Failed to fetch buyer name for user {}: {}", entity.getBuyerUserId(), e.getMessage());
        }

        if (entity.getStatus() == CatalogueQuoteRequestStatus.ACCEPTED) {
            try {
                UserResponse buyer = authClient.getUser(entity.getBuyerUserId());
                if (buyer != null) {
                    builder.buyerContactEmail(buyer.getEmail());
                    builder.buyerContactPhone(buyer.getMobileNumber());
                }
            } catch (Exception e) {
                log.warn("Failed to fetch buyer contact for user {}: {}", entity.getBuyerUserId(), e.getMessage());
            }

            try {
                Integer showroomOwnerId = companyRepository.findById(entity.getCompanyId())
                        .map(c -> c.getCompanyOwnerId())
                        .orElse(null);
                if (showroomOwnerId != null) {
                    UserResponse showroomOwner = authClient.getUser(showroomOwnerId);
                    if (showroomOwner != null) {
                        builder.showroomContactEmail(showroomOwner.getEmail());
                        builder.showroomContactPhone(showroomOwner.getMobileNumber());
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to fetch showroom contact for company {}: {}", entity.getCompanyId(), e.getMessage());
            }
        }

        return builder.build();
    }
}
