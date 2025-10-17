package com.autoadda.apis.vehicle.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyDocumentRequest {

    private Long companyId;
    private Long tradeLicenseImageId;
    private Long emiratesIdImageId;
}
