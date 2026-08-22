package com.autoadda.apis.vehicle.response;

import com.autoadda.apis.vehicle.enums.NumberPlateInterestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NumberPlateInterestResponse {

    private Long id;
    private Long listingId;
    private Integer buyerUserId;
    private Integer sellerUserId;
    private NumberPlateInterestStatus status;
    private String message;
    private String rejectionReason;
    private LocalDateTime respondedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private NumberPlateListingResponse listing;

    // Populated only once status == ACCEPTED; null otherwise so contact info is never leaked early.
    private String buyerContactEmail;
    private String buyerContactPhone;
    private String sellerContactEmail;
    private String sellerContactPhone;
}
