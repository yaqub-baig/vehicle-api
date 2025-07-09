package com.platform.profileservice.mapper;

import com.platform.profileservice.entity.VehicleDetails;
import com.platform.profileservice.request.VehicleRegistrationRequest;
import com.platform.profileservice.response.VehicleDetailResponse;
import com.platform.profileservice.util.PolicyTypeEnum;
import com.platform.profileservice.util.VehicleTypeCategoryEnum;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class VehicleMapper {

    public VehicleDetails toEntity(VehicleRegistrationRequest request) {
        VehicleDetails vehicleDetails = new VehicleDetails ( );
        vehicleDetails.setCode ( request.getCode ( ) );
        vehicleDetails.setYear ( request.getYear ( ) );
        vehicleDetails.setState ( request.getState ( ) );
        vehicleDetails.setBrand ( request.getBrand ( ) );
        vehicleDetails.setModel ( request.getModel ( ) );
        vehicleDetails.setOrigin ( request.getOrigin ( ) );
        vehicleDetails.setNumber ( request.getNumber ( ) );
        vehicleDetails.setTcNumber ( request.getTcNumber ( ) );
        vehicleDetails.setNickName ( request.getNickName ( ) );
        vehicleDetails.setRegExpiry ( request.getRegExpiry ( ) );
        vehicleDetails.setChassisNo ( request.getChassisNo ( ) );
        vehicleDetails.setInsuExpiry ( request.getInsuExpiry ( ) );
        vehicleDetails.setInsuPolicy ( request.getInsuPolicy ( ) );
        vehicleDetails.setEngineType ( request.getEngineType ( ) );
        vehicleDetails.setVehicleType ( request.getVehicleType ( ) );
        vehicleDetails.setVehicleColor ( request.getVehicleColor ( ) );
        vehicleDetails.setNumOfPassenger ( request.getNumOfPassenger ( ) );
        // PolicyTypeEnum validation and exception handling
        vehicleDetails.setPolicyType (
                Optional.ofNullable ( request.getPolicyType ( ) )
                        .filter ( s -> ! s.isBlank ( ) )
                        .map ( PolicyTypeEnum::valueOf )
                        .orElseThrow ( () -> new IllegalArgumentException ( "Policy type must not be null or empty" ) )
        );
        vehicleDetails.setVehicleTypeCategory (
                Optional.ofNullable ( request.getVehicleTypeCategory ( ) )
                        .filter ( s -> ! s.isBlank ( ) )
                        .map ( VehicleTypeCategoryEnum::valueOf )
                        .orElseThrow ( () -> new IllegalArgumentException ( "Vehicle type category must not be null or empty" ) )
        );
        return vehicleDetails;
    }

    public VehicleDetailResponse toResponse(VehicleDetails vehicleDetails) {
        VehicleDetailResponse vehicleDetailResponse = new VehicleDetailResponse ( );
        vehicleDetailResponse.setId ( vehicleDetails.getId ( ) != null ? vehicleDetails.getId ( ) : null );
        vehicleDetailResponse.setCode ( vehicleDetails.getCode ( ) != null ? vehicleDetails.getCode ( ) : null );
        vehicleDetailResponse.setYear ( vehicleDetails.getYear ( ) != null ? vehicleDetails.getYear ( ) : null );
        vehicleDetailResponse.setBrand ( vehicleDetails.getBrand ( ) != null ? vehicleDetails.getBrand ( ) : null );
        vehicleDetailResponse.setModel ( vehicleDetails.getModel ( ) != null ? vehicleDetails.getModel ( ) : null );
        vehicleDetailResponse.setState ( vehicleDetails.getState ( ) != null ? vehicleDetails.getState ( ) : null );
        vehicleDetailResponse.setOrigin ( vehicleDetails.getOrigin ( ) != null ? vehicleDetails.getOrigin ( ) : null );
        vehicleDetailResponse.setNumber ( vehicleDetails.getNumber ( ) != null ? vehicleDetails.getNumber ( ) : null );
        vehicleDetailResponse.setNickName ( vehicleDetails.getNickName ( ) != null ? vehicleDetails.getNickName ( ) : null );
        vehicleDetailResponse.setTcNumber ( vehicleDetails.getTcNumber ( ) != null ? vehicleDetails.getTcNumber ( ) : null );
        vehicleDetailResponse.setChassisNo ( vehicleDetails.getChassisNo ( ) != null ? vehicleDetails.getChassisNo ( ) : null );
        vehicleDetailResponse.setRegExpiry ( vehicleDetails.getRegExpiry ( ) != null ? vehicleDetails.getRegExpiry ( ) : null );
        vehicleDetailResponse.setPolicyType ( vehicleDetails.getPolicyType ( ) != null ? vehicleDetails.getPolicyType ( ) : null );
        vehicleDetailResponse.setEngineType ( vehicleDetails.getEngineType ( ) != null ? vehicleDetails.getEngineType ( ) : null );
        vehicleDetailResponse.setInsuExpiry ( vehicleDetails.getInsuExpiry ( ) != null ? vehicleDetails.getInsuExpiry ( ) : null );
        vehicleDetailResponse.setInsuPolicy ( vehicleDetails.getInsuPolicy ( ) != null ? vehicleDetails.getInsuPolicy ( ) : null );
        vehicleDetailResponse.setVehicleType ( vehicleDetails.getVehicleType ( ) != null ? vehicleDetails.getVehicleType ( ) : null );
        vehicleDetailResponse.setVehicleColor ( vehicleDetails.getVehicleColor ( ) != null ? vehicleDetails.getVehicleColor ( ) : null );
        vehicleDetailResponse.setNumOfPassenger ( vehicleDetails.getNumOfPassenger ( ) != null ? vehicleDetails.getNumOfPassenger ( ) : null );
        vehicleDetailResponse.setVehicleTypeCategory ( vehicleDetails.getVehicleTypeCategory ( ) != null ? vehicleDetails.getVehicleTypeCategory ( ) : null );
        return vehicleDetailResponse;
    }
}

