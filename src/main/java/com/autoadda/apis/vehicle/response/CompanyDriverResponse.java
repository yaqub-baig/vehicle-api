package com.autoadda.apis.vehicle.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyDriverResponse {

    private Long id;
    private Long companyId;
    private Integer driverUserId;
    private String driverName;
    private String driverMobileNumber;
    private LocalDateTime createdAt;
}
