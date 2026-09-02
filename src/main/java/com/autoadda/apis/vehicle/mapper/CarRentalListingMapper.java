package com.autoadda.apis.vehicle.mapper;

import com.autoadda.apis.vehicle.dao.MediaFileRepository;
import com.autoadda.apis.vehicle.entity.CarRentalListing;
import com.autoadda.apis.vehicle.entity.MediaFile;
import com.autoadda.apis.vehicle.entity.VehicleDetails;
import com.autoadda.apis.vehicle.enums.RentalListingStatus;
import com.autoadda.apis.vehicle.request.CarRentalListingRequest;
import com.autoadda.apis.vehicle.response.CarRentalListingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CarRentalListingMapper {

    private final MediaFileRepository mediaFileRepository;

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
                .images(resolveImages(request.getImageIds()))
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
        if (request.getImageIds() != null) {
            listing.setImages(resolveImages(request.getImageIds()));
        }
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
                .images(entity.getImages())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        if (vehicle != null) {
            builder.vehicleBrand(vehicle.getBrand())
                    .vehicleModel(vehicle.getModel())
                    .vehicleYear(vehicle.getYear())
                    .vehicleNickName(vehicle.getNickName());
        }

        return builder.build();
    }

    private List<MediaFile> resolveImages(List<Long> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) {
            return null;
        }
        return mediaFileRepository.findAllById(imageIds);
    }
}
