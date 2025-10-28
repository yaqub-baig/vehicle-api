package com.autoadda.apis.vehicle.response;

import com.autoadda.apis.vehicle.entity.MediaFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
