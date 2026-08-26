package com.autoadda.apis.vehicle.response;

import com.autoadda.apis.vehicle.enums.RentalListingStatus;
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
public class CarRentalListingResponse {

    private Long id;
    private Integer vehicleId;
    private Integer ownerId;
    private BigDecimal pricePerHour;
    private BigDecimal pricePerDay;
    private Integer kmLimitPerDay;
    private BigDecimal excessKmPenaltyRate;
    private BigDecimal lateReturnPenaltyRate;
    private String description;
    private RentalListingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Vehicle summary, resolved from VehicleDetails for display purposes.
    private String vehicleBrand;
    private String vehicleModel;
    private String vehicleYear;
    private String vehicleNickName;
}
