package com.autoadda.apis.vehicle.mapper;

import com.autoadda.apis.vehicle.entity.CarRentalListing;
import com.autoadda.apis.vehicle.entity.MediaFile;
import com.autoadda.apis.vehicle.entity.VehicleDetails;
import com.autoadda.apis.vehicle.enums.RentalListingStatus;
import com.autoadda.apis.vehicle.request.CarRentalListingRequest;
import com.autoadda.apis.vehicle.response.CarRentalListingResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CarRentalListingMapper {

    public CarRentalListing toEntity(CarRentalListingRequest request, Integer ownerId) {
        return CarRentalListing.builder()
                .vehicleId(request.getVehicleId())
                .ownerId(ownerId)
                .pricePerHour(request.getPricePerHour())
                .pricePerDay(request.getPricePerDay())
                .kmLimitPerDay(request.getKmLimitPerDay())
                .excessKmPenaltyRate(request.getExcessKmPenaltyRate())
                .lateReturnPenaltyRate(request.getLateReturnPenaltyRate())
                .description(request.getDescription())
                .location(request.getLocation())
                .status(RentalListingStatus.ACTIVE)
                .isDeleted(false)
                .build();
    }

    public void applyUpdate(CarRentalListing listing, CarRentalListingRequest request) {
        listing.setPricePerHour(request.getPricePerHour());
        listing.setPricePerDay(request.getPricePerDay());
        listing.setKmLimitPerDay(request.getKmLimitPerDay());
        listing.setExcessKmPenaltyRate(request.getExcessKmPenaltyRate());
        listing.setLateReturnPenaltyRate(request.getLateReturnPenaltyRate());
        listing.setDescription(request.getDescription());
        listing.setLocation(request.getLocation());
    }

    public CarRentalListingResponse toResponse(CarRentalListing entity, VehicleDetails vehicle) {
        if (entity == null) {
            return null;
        }

        CarRentalListingResponse.CarRentalListingResponseBuilder builder = CarRentalListingResponse.builder()
                .id(entity.getId())
                .vehicleId(entity.getVehicleId())
                .ownerId(entity.getOwnerId())
                .pricePerHour(entity.getPricePerHour())
                .pricePerDay(entity.getPricePerDay())
                .kmLimitPerDay(entity.getKmLimitPerDay())
                .excessKmPenaltyRate(entity.getExcessKmPenaltyRate())
                .lateReturnPenaltyRate(entity.getLateReturnPenaltyRate())
                .description(entity.getDescription())
                .location(entity.getLocation())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        if (vehicle != null) {
            builder.vehicleBrand(vehicle.getBrand())
                    .vehicleModel(vehicle.getModel())
                    .vehicleYear(vehicle.getYear())
                    .vehicleNickName(vehicle.getNickName());

            List<MediaFile> vehicleImages = vehicle.getVehicleImages();
            builder.vehicleImages(vehicleImages)
                    .mainImage(resolveMainImage(vehicleImages, vehicle.getMainImageId()));
        }

        return builder.build();
    }

    private MediaFile resolveMainImage(List<MediaFile> vehicleImages, Long mainImageId) {
        if (vehicleImages == null || mainImageId == null) {
            return null;
        }
        return vehicleImages.stream()
                .filter(image -> mainImageId.equals(image.getId()))
                .findFirst()
                .orElse(null);
    }
}
