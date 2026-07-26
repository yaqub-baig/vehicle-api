package com.autoadda.apis.vehicle.response;

import com.autoadda.apis.vehicle.enums.CarpoolConnectionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarpoolConnectionResponse {

    private Long id;
    private CarpoolConnectionStatus status;
    private String message;
    private String rejectionReason;
    private LocalDateTime respondedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private CarpoolListingResponse requesterListing;
    private CarpoolListingResponse targetListing;

    // Populated only once status == ACCEPTED; null otherwise so contact info is never leaked early.
    private String requesterContactEmail;
    private String requesterContactPhone;
    private String targetContactEmail;
    private String targetContactPhone;
}
