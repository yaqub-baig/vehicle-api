package com.platform.profileservice.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleRegistrationRequest {

    private String odometer;
    private String brand;
    private String model;
    private String year;
    private String engineType;
    private String engineNumber;
    private String chassisNo;
    private String origin;
    private String numOfPassenger;
    private String vehicleType;
    private String tcNumber;
    private String policyType;
    private String vehicleTypeCategory;
    private String code;
    private String state;
    private String number;
    private String vehicleColor;
    private String nickName;
    private LocalDate regExpiry;
    private String insuCompany;
    private LocalDate insuExpiry;
    private String insuPolicy;
    private Long mulkiaImageFront;
    private Long mulkiaImageBack;
    private List<Long> vehicleImages;

}