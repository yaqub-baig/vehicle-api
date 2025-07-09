package com.platform.profileservice.mapper;

import com.platform.profileservice.entity.VehicleDetails;
import com.platform.profileservice.request.VehicleRegistrationRequest;
import com.platform.profileservice.response.VehicleDetailResponse;
import com.platform.profileservice.util.PolicyTypeEnum;
import com.platform.profileservice.util.VehicleTypeCategoryEnum;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {

    public VehicleDetails toEntity(VehicleRegistrationRequest request) {
        VehicleDetails vehicleDetails = new VehicleDetails ( );
        vehicleDetails.setBrand ( request.getBrand ( ) );
        vehicleDetails.setModel ( request.getModel ( ) );
        vehicleDetails.setYear ( request.getYear ( ) );
        vehicleDetails.setEngineType ( request.getEngineType ( ) );
        vehicleDetails.setChassisNo ( request.getChassisNo ( ) );
        vehicleDetails.setOrigin ( request.getOrigin ( ) );
        vehicleDetails.setNumOfPassenger ( request.getNumOfPassenger ( ) );
        vehicleDetails.setVehicleType ( request.getVehicleType ( ) );
        vehicleDetails.setTcNumber ( request.getTcNumber ( ) );
        vehicleDetails.setPolicyType ( PolicyTypeEnum.valueOf ( request.getPolicyType ( ) ) );
        vehicleDetails.setVehicleTypeCategory ( VehicleTypeCategoryEnum.valueOf ( request.getVehicleTypeCategory ( ) ) );
        vehicleDetails.setCode ( request.getCode ( ) );
        vehicleDetails.setState ( request.getState ( ) );
        vehicleDetails.setNumber ( request.getNumber ( ) );
        vehicleDetails.setVehicleColor ( request.getVehicleColor ( ) );
        vehicleDetails.setNickName ( request.getNickName ( ) );
        vehicleDetails.setRegExpiry ( request.getRegExpiry ( ) );
        vehicleDetails.setInsuExpiry ( request.getInsuExpiry ( ) );
        vehicleDetails.setInsuPolicy ( request.getInsuPolicy ( ) );

        return vehicleDetails;
    }

    public VehicleDetailResponse toResponse(VehicleDetails vehicleDetails) {
        VehicleDetailResponse vehicleDetailResponse = new VehicleDetailResponse ( );
        vehicleDetailResponse.setId ( vehicleDetails.getId ( ) != null ? vehicleDetails.getId ( ) : null );
        vehicleDetailResponse.setBrand ( vehicleDetails.getBrand ( ) != null ? vehicleDetails.getBrand ( ) : null );
        vehicleDetailResponse.setModel ( vehicleDetails.getModel ( ) != null ? vehicleDetails.getModel ( ) : null );
        vehicleDetailResponse.setYear ( vehicleDetails.getYear ( ) != null ? vehicleDetails.getYear ( ) : null );
        vehicleDetailResponse.setEngineType ( vehicleDetails.getEngineType ( ) != null ? vehicleDetails.getEngineType ( ) : null );
        vehicleDetailResponse.setChassisNo ( vehicleDetails.getChassisNo ( ) != null ? vehicleDetails.getChassisNo ( ) : null );
        vehicleDetailResponse.setOrigin ( vehicleDetails.getOrigin ( ) != null ? vehicleDetails.getOrigin ( ) : null );
        vehicleDetailResponse.setNumOfPassenger ( vehicleDetails.getNumOfPassenger ( ) != null ? vehicleDetails.getNumOfPassenger ( ) : null );
        vehicleDetailResponse.setVehicleType ( vehicleDetails.getVehicleType ( ) != null ? vehicleDetails.getVehicleType ( ) : null );
        vehicleDetailResponse.setTcNumber ( vehicleDetails.getTcNumber ( ) != null ? vehicleDetails.getTcNumber ( ) : null );
        vehicleDetailResponse.setPolicyType ( vehicleDetails.getPolicyType ( ) != null ? vehicleDetails.getPolicyType ( ) : null );
        vehicleDetailResponse.setVehicleTypeCategory ( vehicleDetails.getVehicleTypeCategory ( ) != null ? vehicleDetails.getVehicleTypeCategory ( ) : null );
        vehicleDetailResponse.setCode ( vehicleDetails.getCode ( ) != null ? vehicleDetails.getCode ( ) : null );
        vehicleDetailResponse.setState ( vehicleDetails.getState ( ) != null ? vehicleDetails.getState ( ) : null );
        vehicleDetailResponse.setNumber ( vehicleDetails.getNumber ( ) != null ? vehicleDetails.getNumber ( ) : null );
        vehicleDetailResponse.setVehicleColor ( vehicleDetails.getVehicleColor ( ) != null ? vehicleDetails.getVehicleColor ( ) : null );
        vehicleDetailResponse.setNickName ( vehicleDetails.getNickName ( ) != null ? vehicleDetails.getNickName ( ) : null );
        vehicleDetailResponse.setRegExpiry ( vehicleDetails.getRegExpiry ( ) != null ? vehicleDetails.getRegExpiry ( ) : null );
        vehicleDetailResponse.setInsuExpiry ( vehicleDetails.getInsuExpiry ( ) != null ? vehicleDetails.getInsuExpiry ( ) : null );
        vehicleDetailResponse.setInsuPolicy ( vehicleDetails.getInsuPolicy ( ) != null ? vehicleDetails.getInsuPolicy ( ) : null );
        return vehicleDetailResponse;
    }
}
