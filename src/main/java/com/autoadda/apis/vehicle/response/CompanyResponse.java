package com.autoadda.apis.vehicle.response;

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
}
