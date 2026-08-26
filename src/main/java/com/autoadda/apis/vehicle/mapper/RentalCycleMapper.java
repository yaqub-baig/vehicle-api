package com.autoadda.apis.vehicle.mapper;

import com.autoadda.apis.vehicle.entity.CarRentalListing;
import com.autoadda.apis.vehicle.entity.RentalCycle;
import com.autoadda.apis.vehicle.entity.RentalRequest;
import com.autoadda.apis.vehicle.enums.RentalCycleStatus;
import com.autoadda.apis.vehicle.enums.RentalRateType;
import com.autoadda.apis.vehicle.response.RentalCycleResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class RentalCycleMapper {

    public RentalCycle toEntity(RentalRequest rentalRequest, CarRentalListing listing) {
        BigDecimal rateAtStart = rentalRequest.getRequestedRateType() == RentalRateType.HOURLY
                ? listing.getPricePerHour()
                : listing.getPricePerDay();

        return RentalCycle.builder()
                .rentalRequestId(rentalRequest.getId())
                .listingId(listing.getId())
                .vehicleId(listing.getVehicleId())
                .renterId(rentalRequest.getRenterId())
                .ownerId(rentalRequest.getOwnerId())
                .rateType(rentalRequest.getRequestedRateType())
                .rentalType(rentalRequest.getRentalType())
                .durationValue(rentalRequest.getDurationValue())
                .rateAtStart(rateAtStart)
                .kmLimitPerDayAtStart(listing.getKmLimitPerDay())
                .excessKmPenaltyRateAtStart(listing.getExcessKmPenaltyRate())
                .lateReturnPenaltyRateAtStart(listing.getLateReturnPenaltyRate())
                .discountAmount(BigDecimal.ZERO)
                .status(RentalCycleStatus.AWAITING_START)
                .isDeleted(false)
                .build();
    }

    public RentalCycleResponse toResponse(RentalCycle entity) {
        if (entity == null) {
            return null;
        }

        return RentalCycleResponse.builder()
                .id(entity.getId())
                .rentalRequestId(entity.getRentalRequestId())
                .listingId(entity.getListingId())
                .vehicleId(entity.getVehicleId())
                .renterId(entity.getRenterId())
                .ownerId(entity.getOwnerId())
                .rateType(entity.getRateType())
                .rentalType(entity.getRentalType())
                .durationValue(entity.getDurationValue())
                .rateAtStart(entity.getRateAtStart())
                .kmLimitPerDayAtStart(entity.getKmLimitPerDayAtStart())
                .excessKmPenaltyRateAtStart(entity.getExcessKmPenaltyRateAtStart())
                .lateReturnPenaltyRateAtStart(entity.getLateReturnPenaltyRateAtStart())
                .discountAmount(entity.getDiscountAmount())
                .startOdometer(entity.getStartOdometer())
                .endOdometer(entity.getEndOdometer())
                .startedAt(entity.getStartedAt())
                .expectedReturnAt(entity.getExpectedReturnAt())
                .actualReturnAt(entity.getActualReturnAt())
                .status(entity.getStatus())
                .basePrice(entity.getBasePrice())
                .excessKmPenalty(entity.getExcessKmPenalty())
                .lateReturnPenalty(entity.getLateReturnPenalty())
                .finalPrice(entity.getFinalPrice())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
