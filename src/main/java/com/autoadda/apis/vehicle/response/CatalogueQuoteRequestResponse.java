package com.autoadda.apis.vehicle.response;

import com.autoadda.apis.vehicle.enums.CatalogueQuoteRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogueQuoteRequestResponse {

    private Long id;
    private Long offeringId;
    private Long companyId;
    private Integer buyerUserId;
    private CatalogueQuoteRequestStatus status;
    private String message;
    private String responseMessage;
    private BigDecimal quotedPrice;
    private String rejectionReason;
    private LocalDateTime respondedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String buyerName;
    private CatalogueOfferingResponse offering;

    // Populated only once status == ACCEPTED; null otherwise so contact info is never leaked early.
    private String buyerContactEmail;
    private String buyerContactPhone;
    private String showroomContactEmail;
    private String showroomContactPhone;
}
