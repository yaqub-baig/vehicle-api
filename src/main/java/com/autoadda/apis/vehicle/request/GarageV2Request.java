package com.autoadda.apis.vehicle.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GarageV2Request {
    private String name;
    private String garageBrandName;
    private String garageArabicName;
    private String trnNumber;
    private String website;
    private Long garageLogo;
    private List<Long> tradeLicenseAttachmentIds;
    private List<Long> vatCertificateIds;
    private String phoneNumber;
    private String mobileNumber;
    private String email;
    private GarageAddressRequest garageAddress;
    private String brands;
    private String models;
    private List<Integer> serviceTypeIds;
    private List<Long> garageImageIds;
}
