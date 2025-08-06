package com.service.vehicle.mapper;

import com.service.vehicle.entity.VehicleDetails;
import com.service.vehicle.request.VehicleRegistrationRequest;
import com.service.vehicle.response.VehicleDetailResponse;
import com.service.vehicle.util.PolicyTypeEnum;
import com.service.vehicle.util.VehicleTypeCategoryEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class VehicleMapper {

    private final MediaFileMapper mediaFileMapper;

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
        vehicleDetails.setOdometer ( request.getOdometer ( ) );
        vehicleDetails.setRegExpiry ( request.getRegExpiry ( ) );
        vehicleDetails.setChassisNo ( request.getChassisNo ( ) );
        vehicleDetails.setInsuExpiry ( request.getInsuExpiry ( ) );
        vehicleDetails.setInsuPolicy ( request.getInsuPolicy ( ) );
        vehicleDetails.setEngineType ( request.getEngineType ( ) );
        vehicleDetails.setInsuCompany ( request.getInsuCompany ( ) );
        vehicleDetails.setVehicleType ( request.getVehicleType ( ) );
        vehicleDetails.setEngineNumber ( request.getEngineNumber ( ) );
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
        vehicleDetailResponse.setOdometer ( vehicleDetails.getOdometer ( ) != null ? vehicleDetails.getOdometer ( ) : null );
        vehicleDetailResponse.setNickName ( vehicleDetails.getNickName ( ) != null ? vehicleDetails.getNickName ( ) : null );
        vehicleDetailResponse.setTcNumber ( vehicleDetails.getTcNumber ( ) != null ? vehicleDetails.getTcNumber ( ) : null );
        vehicleDetailResponse.setChassisNo ( vehicleDetails.getChassisNo ( ) != null ? vehicleDetails.getChassisNo ( ) : null );
        vehicleDetailResponse.setRegExpiry ( vehicleDetails.getRegExpiry ( ) != null ? vehicleDetails.getRegExpiry ( ) : null );
        vehicleDetailResponse.setPolicyType ( vehicleDetails.getPolicyType ( ) != null ? vehicleDetails.getPolicyType ( ) : null );
        vehicleDetailResponse.setEngineType ( vehicleDetails.getEngineType ( ) != null ? vehicleDetails.getEngineType ( ) : null );
        vehicleDetailResponse.setInsuExpiry ( vehicleDetails.getInsuExpiry ( ) != null ? vehicleDetails.getInsuExpiry ( ) : null );
        vehicleDetailResponse.setInsuPolicy ( vehicleDetails.getInsuPolicy ( ) != null ? vehicleDetails.getInsuPolicy ( ) : null );
        vehicleDetailResponse.setInsuCompany ( vehicleDetails.getInsuCompany ( ) != null ? vehicleDetails.getInsuCompany ( ) : null );
        vehicleDetailResponse.setVehicleType ( vehicleDetails.getVehicleType ( ) != null ? vehicleDetails.getVehicleType ( ) : null );
        vehicleDetailResponse.setVehicleColor ( vehicleDetails.getVehicleColor ( ) != null ? vehicleDetails.getVehicleColor ( ) : null );
        vehicleDetailResponse.setEngineNumber ( vehicleDetails.getEngineNumber ( ) != null ? vehicleDetails.getEngineNumber ( ) : null );
        vehicleDetailResponse.setNumOfPassenger ( vehicleDetails.getNumOfPassenger ( ) != null ? vehicleDetails.getNumOfPassenger ( ) : null );
        vehicleDetailResponse.setVehicleTypeCategory ( vehicleDetails.getVehicleTypeCategory ( ) != null ? vehicleDetails.getVehicleTypeCategory ( ) : null );
        vehicleDetailResponse.setMulkiaImageFront ( vehicleDetails.getMulkiaImageFront ( ) == null ? null : mediaFileMapper.toResponse ( vehicleDetails.getMulkiaImageFront ( ) ) );
        vehicleDetailResponse.setMulkiaImageBack ( vehicleDetails.getMulkiaImageBack ( ) == null ? null : mediaFileMapper.toResponse ( vehicleDetails.getMulkiaImageBack ( ) ) );
        vehicleDetailResponse.setVehicleImages ( vehicleDetails.getVehicleImages ( ) == null ? null : vehicleDetails.getVehicleImages ( ).stream ( ).map ( mediaFileMapper::toResponse ).toList ( ) );
        return vehicleDetailResponse;
    }

    public void updateEntity(VehicleDetails vehicleDetails, VehicleRegistrationRequest request) {
        if ( request.getYear ( ) != null ) {
            vehicleDetails.setYear ( request.getYear ( ) );
        }
        if ( request.getBrand ( ) != null ) {
            vehicleDetails.setBrand ( request.getBrand ( ) );
        }
        if ( request.getModel ( ) != null ) {
            vehicleDetails.setModel ( request.getModel ( ) );
        }
        if(request.getEngineType() != null) {
            vehicleDetails.setEngineType ( request.getEngineType ( ) );
        }

        if ( request.getState ( ) != null ) {
            vehicleDetails.setState ( request.getState ( ) );
        }
        if ( request.getOrigin ( ) != null ) {
            vehicleDetails.setOrigin ( request.getOrigin ( ) );
        }
        if ( request.getNumber ( ) != null ) {
            vehicleDetails.setNumber ( request.getNumber ( ) );
        }
        if ( request.getOdometer ( ) != null ) {
            vehicleDetails.setOdometer ( request.getOdometer ( ) );
        }
        if ( request.getNickName ( ) != null ) {
            vehicleDetails.setNickName ( request.getNickName ( ) );
        }
        if ( request.getTcNumber ( ) != null ) {
            vehicleDetails.setTcNumber ( request.getTcNumber ( ) );
        }
        if ( request.getRegExpiry ( ) != null ) {
            vehicleDetails.setRegExpiry ( request.getRegExpiry ( ) );
        }
        if ( request.getInsuCompany ( ) != null ) {
            vehicleDetails.setInsuCompany ( request.getInsuCompany ( ) );
        }
        if ( request.getInsuExpiry ( ) != null ) {
            vehicleDetails.setInsuExpiry ( request.getInsuExpiry ( ) );
        }
        if ( request.getInsuPolicy ( ) != null ) {
            vehicleDetails.setInsuPolicy ( request.getInsuPolicy ( ) );
        }

    }
}

