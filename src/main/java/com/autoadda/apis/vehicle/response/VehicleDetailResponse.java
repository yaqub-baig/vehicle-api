package com.autoadda.apis.vehicle.response;

import com.autoadda.apis.vehicle.util.PolicyTypeEnum;
import com.autoadda.apis.vehicle.util.VehicleTypeCategoryEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleDetailResponse {
	private Integer id;
	private String brand;
	private String model;
	private String year;
	private String odometer;
	private String engineType;
	private String engineNumber;
	private String chassisNo;
	private String origin;
	private String numOfPassenger;
	private String vehicleType;
	private String tcNumber;
	private PolicyTypeEnum policyType;
	private VehicleTypeCategoryEnum vehicleTypeCategory;
	private String code;
	private String state;
	private String number;
	private String vehicleColor;
	private String nickName;
	private LocalDate regExpiry;
	private LocalDate insuExpiry;
	private String insuPolicy;
	private String insuCompany;
	private String brandRequestReferenceId;
	private MediaFileResponse mulkiaImageFront;
	private MediaFileResponse mulkiaImageBack;
	private List<MediaFileResponse> vehicleImages;
	private MediaFileResponse mainImage;
	private UserResponse owner;
    private Long companyId;
    private CompanyResponse companyResponse;

}
