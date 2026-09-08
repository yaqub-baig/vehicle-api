package com.autoadda.apis.vehicle.response;

import com.autoadda.apis.vehicle.entity.MediaFile;
import com.autoadda.apis.vehicle.enums.CompanyType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyResponse {
    private Long id;
    private String name;
    private UserResponse companyOwner;
    private String tradeLicenseNumber;
    private String tradeLicenseExpiry;
    private MediaFile profileImage;
    private Set<CompanyType> types;
    private String showroomAddress;
    private String rtaApprovalNumber;
    private Set<String> authorizedBrands;
}
