package com.autoadda.apis.vehicle.request;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private String brandRequestReferenceId;
    private String nickName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate regExpiry;
    private String insuCompany;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate insuExpiry;
    private String insuPolicy;
    private Long mulkiaImageFront;
    private Long mulkiaImageBack;
    private List<Long> vehicleImages;
    private Long mainImageId;

}