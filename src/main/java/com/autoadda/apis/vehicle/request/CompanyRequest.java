package com.autoadda.apis.vehicle.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyRequest {
    private Integer companyOwnerId;
    private String name;
    private String tradeLicenseNumber;
    private String tradeLicenseExpiry;
}
