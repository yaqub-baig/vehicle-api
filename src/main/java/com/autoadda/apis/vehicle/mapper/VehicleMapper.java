package com.autoadda.apis.vehicle.mapper;

import com.autoadda.apis.vehicle.clients.AuthClient;
import com.autoadda.apis.vehicle.clients.PartClient;
import com.autoadda.apis.vehicle.entity.VehicleDetails;
import com.autoadda.apis.vehicle.request.VehicleRegistrationRequest;
import com.autoadda.apis.vehicle.response.VehicleDetailResponse;
import com.autoadda.apis.vehicle.util.PolicyTypeEnum;
import com.autoadda.apis.vehicle.util.VehicleTypeCategoryEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class VehicleMapper {
    private final PartClient partClient;
    private final MediaFileMapper mediaFileMapper;
    private final AuthClient identityClient;

    public VehicleDetails toEntity(VehicleRegistrationRequest request) {
        VehicleDetails vehicleDetails = new VehicleDetails();
        vehicleDetails.setCode(request.getCode());
        vehicleDetails.setYear(request.getYear());
        vehicleDetails.setState(request.getState());
        vehicleDetails.setBrand(request.getBrand() != null && !request.getBrand().isBlank() ? request.getBrand() : null);
        vehicleDetails.setModel(request.getModel() != null && !request.getModel().isBlank() ? request.getModel() : null);
        vehicleDetails.setOrigin(request.getOrigin());
        vehicleDetails.setNumber(request.getNumber());
        vehicleDetails.setTcNumber(request.getTcNumber());
        vehicleDetails.setNickName(request.getNickName());
        vehicleDetails.setOdometer(request.getOdometer());
        vehicleDetails.setRegExpiry(request.getRegExpiry());
        vehicleDetails.setChassisNo(request.getChassisNo());
        vehicleDetails.setInsuExpiry(request.getInsuExpiry());
        vehicleDetails.setInsuPolicy(request.getInsuPolicy());
        vehicleDetails.setEngineType(request.getEngineType() != null && !request.getEngineType().isBlank() ? request.getEngineType() : null);
        vehicleDetails.setInsuCompany(request.getInsuCompany());
        vehicleDetails.setMainImageId(request.getMainImageId());
        vehicleDetails.setIsForSale(Boolean.FALSE);
        vehicleDetails.setVehicleType(request.getVehicleType());
        vehicleDetails.setEngineNumber(request.getEngineNumber());
        vehicleDetails.setBrandRequestReferenceId(request.getBrandRequestReferenceId() != null && !request.getBrandRequestReferenceId().isBlank() ? request.getBrandRequestReferenceId() : null);
        vehicleDetails.setVehicleColor(request.getVehicleColor());
        vehicleDetails.setNumOfPassenger(request.getNumOfPassenger());
        // PolicyTypeEnum validation and exception handling
        vehicleDetails.setPolicyType(
                Optional.ofNullable(request.getPolicyType())
                        .filter(s -> !s.isBlank())
                        .map(PolicyTypeEnum::valueOf)
                        .orElseThrow(() -> new IllegalArgumentException("Policy type must not be null or empty"))
        );
        vehicleDetails.setVehicleTypeCategory(
                Optional.ofNullable(request.getVehicleTypeCategory())
                        .filter(s -> !s.isBlank())
                        .map(VehicleTypeCategoryEnum::valueOf)
                        .orElseThrow(() -> new IllegalArgumentException("Vehicle type category must not be null or empty"))
        );
        return vehicleDetails;
    }

    public VehicleDetailResponse toResponse(VehicleDetails vehicleDetails) {
        VehicleDetailResponse vehicleDetailResponse = new VehicleDetailResponse();
        mapBasicFields(vehicleDetails, vehicleDetailResponse);
        mapRegistrationFields(vehicleDetails, vehicleDetailResponse);
        mapInsuranceFields(vehicleDetails, vehicleDetailResponse);
        mapAdditionalFields(vehicleDetails, vehicleDetailResponse);
        mapMediaFiles(vehicleDetails, vehicleDetailResponse);
        vehicleDetailResponse.setOwner(identityClient.getUser(vehicleDetails.getCustomerId()));
        return vehicleDetailResponse;
    }

    private void mapBasicFields(VehicleDetails vehicleDetails, VehicleDetailResponse response) {
        response.setId(vehicleDetails.getId());
        response.setCode(vehicleDetails.getCode());
        response.setYear(vehicleDetails.getYear());
        response.setBrand(vehicleDetails.getBrand());
        response.setModel(vehicleDetails.getModel());
        response.setEngineType(vehicleDetails.getEngineType());
        response.setEngineNumber(vehicleDetails.getEngineNumber());
        response.setVehicleType(vehicleDetails.getVehicleType());
        response.setVehicleColor(vehicleDetails.getVehicleColor());
        response.setVehicleTypeCategory(vehicleDetails.getVehicleTypeCategory());
    }

    private void mapRegistrationFields(VehicleDetails vehicleDetails, VehicleDetailResponse response) {
        response.setState(vehicleDetails.getState());
        response.setOrigin(vehicleDetails.getOrigin());
        response.setNumber(vehicleDetails.getNumber());
        response.setTcNumber(vehicleDetails.getTcNumber());
        response.setChassisNo(vehicleDetails.getChassisNo());
        response.setRegExpiry(vehicleDetails.getRegExpiry());
        response.setPolicyType(vehicleDetails.getPolicyType());
    }

    private void mapInsuranceFields(VehicleDetails vehicleDetails, VehicleDetailResponse response) {
        response.setInsuCompany(vehicleDetails.getInsuCompany());
        response.setInsuExpiry(vehicleDetails.getInsuExpiry());
        response.setInsuPolicy(vehicleDetails.getInsuPolicy());
    }

    private void mapAdditionalFields(VehicleDetails vehicleDetails, VehicleDetailResponse response) {
        response.setOdometer(vehicleDetails.getOdometer());
        response.setNickName(vehicleDetails.getNickName());
        response.setCompanyId(vehicleDetails.getCompanyId());
        response.setIsForSale(vehicleDetails.getIsForSale());
        response.setNumOfPassenger(vehicleDetails.getNumOfPassenger());
        response.setBrandRequestReferenceId(vehicleDetails.getBrandRequestReferenceId());
    }

    private void mapMediaFiles(VehicleDetails vehicleDetails, VehicleDetailResponse response) {
        response.setMulkiaImageFront(vehicleDetails.getMulkiaImageFront() == null ? null :
                mediaFileMapper.toResponse(vehicleDetails.getMulkiaImageFront()));
        response.setMulkiaImageBack(vehicleDetails.getMulkiaImageBack() == null ? null :
                mediaFileMapper.toResponse(vehicleDetails.getMulkiaImageBack()));
        response.setVehicleImages(vehicleDetails.getVehicleImages() == null ? null :
                vehicleDetails.getVehicleImages().stream().map(mediaFileMapper::toResponse).toList());
        response.setMainImage(
                vehicleDetails.getVehicleImages().stream()
                        .filter(m -> m.getId().equals(vehicleDetails.getMainImageId()))
                        .map(mediaFileMapper::toResponse)
                        .findFirst()
                        .orElse(null)
        );
    }

    public void updateEntity(VehicleDetails vehicleDetails, VehicleRegistrationRequest request) {
        updateBasicDetails(vehicleDetails, request);
        updateRegistrationDetails(vehicleDetails, request);
        updateInsuranceDetails(vehicleDetails, request);
        updateAdditionalDetails(vehicleDetails, request);
    }

    private void updateBasicDetails(VehicleDetails vehicleDetails, VehicleRegistrationRequest request) {
        if (request.getYear() != null) {
            vehicleDetails.setYear(request.getYear());
        }
        if (request.getBrand() != null) {
            vehicleDetails.setBrand(request.getBrand());
        }
        if (request.getModel() != null) {
            vehicleDetails.setModel(request.getModel());
        }
        if (request.getEngineType() != null) {
            vehicleDetails.setEngineType(request.getEngineType());
        }
    }

    private void updateRegistrationDetails(VehicleDetails vehicleDetails, VehicleRegistrationRequest request) {
        if (request.getState() != null) {
            vehicleDetails.setState(request.getState());
        }
        if (request.getOrigin() != null) {
            vehicleDetails.setOrigin(request.getOrigin());
        }
        if (request.getNumber() != null) {
            vehicleDetails.setNumber(request.getNumber());
        }
        if (request.getTcNumber() != null) {
            vehicleDetails.setTcNumber(request.getTcNumber());
        }
        if (request.getRegExpiry() != null) {
            vehicleDetails.setRegExpiry(request.getRegExpiry());
        }
    }

    private void updateInsuranceDetails(VehicleDetails vehicleDetails, VehicleRegistrationRequest request) {
        if (request.getInsuCompany() != null) {
            vehicleDetails.setInsuCompany(request.getInsuCompany());
        }
        if (request.getInsuExpiry() != null) {
            vehicleDetails.setInsuExpiry(request.getInsuExpiry());
        }
        if (request.getInsuPolicy() != null) {
            vehicleDetails.setInsuPolicy(request.getInsuPolicy());
        }
    }

    private void updateAdditionalDetails(VehicleDetails vehicleDetails, VehicleRegistrationRequest request) {
        if (request.getOdometer() != null) {
            vehicleDetails.setOdometer(request.getOdometer());
        }
        if (request.getNickName() != null) {
            vehicleDetails.setNickName(request.getNickName());
        }
        if (request.getMainImageId() != null) {
            vehicleDetails.setMainImageId(request.getMainImageId());
        }
        if (request.getVehicleColor() != null) {
            vehicleDetails.setVehicleColor(request.getVehicleColor());
        }
        if (request.getVehicleType() != null) {
            vehicleDetails.setVehicleType(request.getVehicleType());
        }
    }
}

