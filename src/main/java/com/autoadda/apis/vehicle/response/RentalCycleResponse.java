package com.autoadda.apis.vehicle.response;

import com.autoadda.apis.vehicle.enums.RentalCycleStatus;
import com.autoadda.apis.vehicle.enums.RentalRateType;
import com.autoadda.apis.vehicle.enums.RentalType;
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
public class RentalCycleResponse {

    private Long id;
    private Long rentalRequestId;
    private Long listingId;
    private Integer vehicleId;
    private Integer renterId;
    private Integer ownerId;
    private RentalRateType rateType;
    private RentalType rentalType;
    private Integer durationValue;
    private BigDecimal rateAtStart;
    private Integer kmLimitPerDayAtStart;
    private BigDecimal excessKmPenaltyRateAtStart;
    private BigDecimal lateReturnPenaltyRateAtStart;
    private BigDecimal discountAmount;
    private Integer startOdometer;
    private Integer endOdometer;
    private LocalDateTime startedAt;
    private LocalDateTime expectedReturnAt;
    private LocalDateTime actualReturnAt;
    private RentalCycleStatus status;
    private BigDecimal basePrice;
    private BigDecimal excessKmPenalty;
    private BigDecimal lateReturnPenalty;
    private BigDecimal finalPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
