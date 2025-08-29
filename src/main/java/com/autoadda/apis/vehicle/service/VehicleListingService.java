package com.autoadda.apis.vehicle.service;

import com.autoadda.apis.vehicle.dao.VehicleDetailsRepository;
import com.autoadda.apis.vehicle.dao.VehicleListingRepository;
import com.autoadda.apis.vehicle.entity.VehicleListing;
import com.autoadda.apis.vehicle.exception.ResourceNotFoundException;
import com.autoadda.apis.vehicle.mapper.VehicleListingMapper;
import com.autoadda.apis.vehicle.request.VehicleListingRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.response.VehicleListingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class VehicleListingService {
    private final VehicleListingRepository vehicleListingRepository;
    private final VehicleDetailsRepository vehicleDetailsRepository;
    private final VehicleListingMapper vehicleListingMapper;

    public GenericResponse createVehicleListing(VehicleListingRequest vehicleListingRequest) {
        VehicleListing vehicleListing = vehicleListingMapper.requestToEntity ( vehicleListingRequest );
        vehicleListing.setVehicle ( vehicleDetailsRepository.findById ( vehicleListingRequest.getVehicleId ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "invalid vehicle id" ) ) );
        VehicleListing listedVehicle = vehicleListingRepository.save ( vehicleListing );
        return GenericResponse.builder ( ).code ( "200" ).status ( "success" ).message ( "Vehicle listed successfully" ).body ( vehicleListingMapper.entityToResponse ( listedVehicle ) ).build ( );
    }

    public GenericResponse getVehicleListings(String location) {
        if(location!=null&& !location.isBlank ()){
            List<VehicleListing> byCity = vehicleListingRepository.findByCity ( location );
            List<VehicleListingResponse> vehicleListingResponseStream = byCity.stream ( ).map ( vehicleListingMapper::entityToResponse ).toList();
            return GenericResponse.builder().body(vehicleListingResponseStream).code("200").message("success").build();
        }
        else{
            List<VehicleListing> vehicleListings = vehicleListingRepository.findAll ( ).stream ().sorted ( ( v1, v2 ) -> v2.getCreatedAt ( ).compareTo ( v1.getCreatedAt ( ) ) ).toList ( );
            List<VehicleListingResponse> vehicleListingResponseStream = vehicleListings.stream ( ).map ( vehicleListingMapper::entityToResponse ).toList();
            return GenericResponse.builder ( ).body ( vehicleListingResponseStream ).code ( "200" ).message ( "success" ).build ( );
        }
    }

    public GenericResponse getVehicleListingById(Integer id) {
        return GenericResponse.builder ( ).status ( "success" ).code ( "200" ).message ( "vehicle retrieved successfully" ).body ( vehicleListingRepository.findById ( id ).map ( vehicleListingMapper::entityToResponse ).orElseThrow ( () -> new ResourceNotFoundException ( "invalid vehicle listing id" ) ) ).build ( );
    }

    public GenericResponse getVehicleListingsByUserId(Integer userId) {
        List<VehicleListing> byVehicleCustomerId = vehicleListingRepository.findByVehicleCustomerId ( userId );
        List<VehicleListingResponse> vehicleListingResponseStream = byVehicleCustomerId.stream ( ).map ( vehicleListingMapper::entityToResponse ).toList();
        return GenericResponse.builder().body(vehicleListingResponseStream).code("200").message("success").build();
    }

    public GenericResponse updateVehicleListing(Integer id, VehicleListingRequest vehicleListingRequest) {
        VehicleListing vehicleListing = vehicleListingRepository.findById ( id ).orElseThrow ( () -> new ResourceNotFoundException ( "invalid vehicle listing id" ) );
        if(vehicleListingRequest.getCity ()!=null){
            vehicleListing.setCity ( vehicleListingRequest.getCity ( ) );
        }
        if(vehicleListingRequest.getDescription ()!=null){
            vehicleListing.setDescription ( vehicleListingRequest.getDescription ( ) );
        }
        if(vehicleListingRequest.getPrice ()!=null){
            vehicleListing.setPrice ( vehicleListingRequest.getPrice ( ) );
        }
        VehicleListing save = vehicleListingRepository.save ( vehicleListing );
        VehicleListingResponse body = vehicleListingMapper.entityToResponse ( save );
        return GenericResponse.builder().body( body ).code("200").message("success").build();
    }
}
