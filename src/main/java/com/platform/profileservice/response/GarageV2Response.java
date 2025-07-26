package com.platform.profileservice.response;

import com.platform.profileservice.entity.GarageAddress;
import com.platform.profileservice.entity.MediaFile;
import com.platform.profileservice.entity.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GarageV2Response {
    private Integer id;
    private String name;
    private String garageBrandName;
    private String garageArabicName;
    private String trnNumber;
    private String website;
    private MediaFile garageLogo;
    private List<MediaFile> tradeLicenseAttachment;
    private List<MediaFile> vatCertificate;
    private String phoneNumber;
    private String mobileNumber;
    private String email;
    private GarageAddress garageAddress;
    private String brands;
    private String models;
    private List<ServiceType> serviceTypes;
    private List<MediaFile> garageImages;
    private UserResponse owner;
    private LocalDateTime createdAt;
}
