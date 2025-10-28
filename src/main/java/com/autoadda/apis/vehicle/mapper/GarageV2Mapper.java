package com.autoadda.apis.vehicle.mapper;

import com.autoadda.apis.vehicle.entity.GarageV2;
import com.autoadda.apis.vehicle.response.GarageV2Response;
import com.autoadda.apis.vehicle.request.GarageV2Request;
import com.autoadda.apis.vehicle.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GarageV2Mapper {

    public GarageV2 toEntity(GarageV2Request request) {
        if (request == null) {
            return null;
        }

        GarageV2 garage = new GarageV2();
        
        // Map basic fields
        garage.setName(request.getName());
        garage.setGarageBrandName(request.getGarageBrandName());
        garage.setGarageArabicName(request.getGarageArabicName());
        garage.setTrnNumber(request.getTrnNumber());
        garage.setWebsite(request.getWebsite());
        garage.setPhoneNumber(request.getPhoneNumber());
        garage.setMobileNumber(request.getMobileNumber());
        garage.setEmail(request.getEmail());
        garage.setBrands(request.getBrands());
        garage.setModels(request.getModels());
        return garage;
    }

    public GarageV2Response toResponse(GarageV2 garage) {
        if (garage == null) {
            return null;
        }

        return GarageV2Response.builder()
                .id(garage.getId())
                .name(garage.getName())
                .garageBrandName(garage.getGarageBrandName())
                .garageArabicName(garage.getGarageArabicName())
                .trnNumber(garage.getTrnNumber())
                .website(garage.getWebsite())
                .garageLogo(garage.getGarageLogo())
                .tradeLicenseAttachment(garage.getTradeLicenseAttachment())
                .vatCertificate(garage.getVatCertificate())
                .phoneNumber(garage.getPhoneNumber())
                .mobileNumber(garage.getMobileNumber())
                .email(garage.getEmail())
                .garageAddress(garage.getGarageAddress())
                .brands(garage.getBrands())
                .models(garage.getModels())
                .serviceTypes(garage.getServiceTypes())
                .garageImages(garage.getGarageImages())
                .owner(garage.getOwner() != null ? 
                        UserResponse.builder()
                                .id(garage.getOwner().getId())
                                .name (garage.getOwner().getFirstName()+" "+garage.getOwner().getLastName())
                                .email(garage.getOwner().getEmail())
                                .mobileNumber(garage.getOwner().getMobileNumber())
                                .isVerified(garage.getOwner().getIsVerified())
                                .build() 
                        : null)
                .createdAt(garage.getCreatedAt())
                .build();
    }

    public void updateGarage(GarageV2 garage, GarageV2Request request) {
        if (request == null) {
            return;
        }
        if (request.getName() != null) {
            garage.setName(request.getName());
        }
        if (request.getGarageBrandName() != null) {
            garage.setGarageBrandName(request.getGarageBrandName());
        }
        if (request.getGarageArabicName() != null) {
            garage.setGarageArabicName(request.getGarageArabicName());
        }
        if (request.getTrnNumber() != null) {
            garage.setTrnNumber(request.getTrnNumber());
        }
        if (request.getWebsite() != null) {
            garage.setWebsite(request.getWebsite());
        }
        if (request.getPhoneNumber() != null) {
            garage.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getMobileNumber() != null) {
            garage.setMobileNumber(request.getMobileNumber());
        }
        if (request.getEmail() != null) {
            garage.setEmail(request.getEmail());
        }
        if (request.getBrands() != null) {
            garage.setBrands(request.getBrands());
        }
        if (request.getModels() != null) {
            garage.setModels(request.getModels());
        }
    }
}
