package com.autoadda.apis.vehicle.mapper;

import com.autoadda.apis.vehicle.entity.VehicleListing;
import com.autoadda.apis.vehicle.request.VehicleListingRequest;
import com.autoadda.apis.vehicle.response.VehicleListingResponse;
import com.autoadda.apis.vehicle.util.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class VehicleListingMapper {

    private final VehicleMapper vehicleDetailsMapper;

    public VehicleListing requestToEntity(VehicleListingRequest vehicleListingRequest) {
        VehicleListing vehicleListing = new VehicleListing ( );
        vehicleListing.setPrice ( vehicleListingRequest.getPrice ( ) );
        vehicleListing.setDescription ( vehicleListingRequest.getDescription ( ) );
        vehicleListing.setStatus ( Status.LISTED );
        vehicleListing.setListedDate ( LocalDate.now ( ) );
        vehicleListing.setCity(vehicleListingRequest.getCity());
        return vehicleListing;
    }

    public VehicleListingResponse entityToResponse(VehicleListing vehicleListing) {
        VehicleListingResponse vehicleListingResponse = new VehicleListingResponse ( );
        vehicleListingResponse.setId ( vehicleListing.getId ( ) );
        vehicleListingResponse.setPrice ( vehicleListing.getPrice ( ) );
        vehicleListingResponse.setDescription ( vehicleListing.getDescription ( ) );
        vehicleListingResponse.setStatus ( vehicleListing.getStatus ( ) );
        vehicleListingResponse.setListedDate ( vehicleListing.getListedDate ( ) );
        vehicleListingResponse.setCity(vehicleListing.getCity());
        vehicleListingResponse.setVehicle ( vehicleDetailsMapper.toResponse ( vehicleListing.getVehicle ( ) ) );
        return vehicleListingResponse;
    }
}