package com.autoadda.apis.vehicle.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyRequest {
    private Integer companyOwnerId;
    private String name;
    private String tradeLicenseNumber;
    private String tradeLicenseExpiry;
    private Long profileImageId;

    // Company types as strings (e.g. "USED_CAR_DEALER"). Null/omitted on create -> defaults
    // to FLEET_OWNER. On update: null/omitted leaves the existing set untouched; a present
    // set replaces it (and must not be empty).
    private Set<String> types;

    // Required when types contains NEW_CAR_SHOWROOM, optional otherwise.
    private String showroomAddress;
    private String rtaApprovalNumber;
    private Set<String> authorizedBrands;
}
