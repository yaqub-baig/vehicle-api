package com.autoadda.apis.vehicle.entity;

import com.autoadda.apis.vehicle.enums.CatalogueQuoteRequestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A buyer's enquiry against a {@link CatalogueOffering}. The showroom accepts (with a quoted
 * price and message) or rejects. Contact details are only shared once ACCEPTED. Mirrors
 * NumberPlateInterest.
 */
@Entity
@Table(name = "catalogue_quote_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogueQuoteRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "offering_id", nullable = false)
    private Long offeringId;

    // Denormalized copy of the offering's owning showroom, so queries/authorization don't
    // need a join back to the offering.
    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "buyer_user_id", nullable = false)
    private Integer buyerUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CatalogueQuoteRequestStatus status;

    @Column(length = 1000)
    private String message;

    @Column(name = "response_message", length = 1000)
    private String responseMessage;

    @Column(name = "quoted_price", precision = 12, scale = 2)
    private BigDecimal quotedPrice;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isDeleted == null) {
            isDeleted = false;
        }
        if (status == null) {
            status = CatalogueQuoteRequestStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
