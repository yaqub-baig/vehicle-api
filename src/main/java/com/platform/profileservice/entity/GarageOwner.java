package com.platform.profileservice.entity;

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
@Table(name = "garage_owner")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class GarageOwner {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "emirates_id")
	private String emiratesId;

	@Column(name = "first_name")
	private String firstName;

	@Column(name = "last_name")
	private String lastName;

	@Column(name = "email_id")
	private String emailId;

	@Column(name = "mobile_number")
	private String mobileNumber;

	@Column(name = "vat_number")
	private String vatNumber;

	@Column(name = "account_status")
	private String accountStatus;
	
	@Column(name = "garage_id")
	private Integer garageId;
	
	@Column(name = "ownership_per")
	private Integer ownershipPer;

	@Column(name = "insert_tms")
	private Date insertTms;
}
