package com.autoadda.apis.vehicle.response;

import com.autoadda.apis.vehicle.enums.RentalRateType;
import com.autoadda.apis.vehicle.enums.RentalRequestStatus;
import com.autoadda.apis.vehicle.enums.RentalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentalRequestResponse {

    private Long id;
    private Long listingId;
    private Integer vehicleId;
    private Integer renterId;
    private Integer ownerId;
    private RentalRateType requestedRateType;
    private RentalType rentalType;
    private Integer durationValue;
    private String message;
    private RentalRequestStatus status;
    private String rejectionReason;
    private LocalDateTime respondedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private CarRentalListingResponse listing;

    // Populated only once status == ACCEPTED; null otherwise so contact info is never leaked early.
    private String renterContactEmail;
    private String renterContactPhone;
    private String ownerContactEmail;
    private String ownerContactPhone;
}
