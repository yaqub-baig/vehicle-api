package com.platform.profileservice.request;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LicenseDetails {
	private String licenseNumber;
	private String companyName;
	private String tradeName;
	private String legalType;
	private Date issueDate;
	private Date expiryDate;
	private String mainLicenseNumber;
	private String registerNumber;
	private Integer dcciNumber;
	private Integer share;
	private String role;
	private String nationality;
	private String name;
	private Integer numbur;
	private String activities;
	private Integer addressId;
	private String parcelId;
	private String phoneNumber;
	private String faxNumber;
	private String mobileNumber;
	private String email;
	private String remarks;
}
