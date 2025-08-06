package com.service.vehicle.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "garage_license_details")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class GarageLicenseDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "garage_id")
	private Integer garageId;

	@Column(name = "license_number")
	private String licenseNumber;

	@Column(name = "company_name")
	private String companyName;

	@Column(name = "trade_name")
	private String tradeName;

	@Column(name = "legal_type")
	private String legalType;
	
	@Column(name = "issue_date")
	private Date issueDate;
	
	@Column(name = "expiry_date")
	private Date expiryDate;

	@Column(name = "main_license_number")
	private String mainLicenseNumber;

	@Column(name = "register_number")
	private String registerNumber;
	
	@Column(name = "dcci_number")
	private Integer dcciNumber;
	
	@Column(name = "share")
	private Integer share;
	
	@Column(name = "role")
	private String role;
	
	@Column(name = "nationality")
	private String nationality;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "numbur")
	private Integer numbur;
	
	@Column(name = "activities")
	private String activities;
	
	@Column(name = "address_id")
	private Integer addressId;
	
	@Column(name = "parcel_id")
	private String parcelId;
	
	@Column(name = "phone_number")
	private String phoneNumber;
	
	@Column(name = "fax_number")
	private String faxNumber;
	
	@Column(name = "mobile_number")
	private String mobileNumber;
	
	@Column(name = "email")
	private String email;
	
	@Column(name = "remarks")
	private String remarks;

	@Column(name = "insert_tms")
	private Date insertTms;
	
}
