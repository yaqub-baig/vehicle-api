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
@Table(name = "insurance_details")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class InsuranceDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "company_name")
	private String companyName;
	
	@Column(name = "type")
	private String type;
	
	@Column(name = "policy_number")
	private String policyNumber;
	
	@Column(name = "policy_name")
	private String policyName;
	
	@Column(name = "email")
	private String email;
	
	@Column(name = "phone_number")
	private String phoneNumber;
	
	@Column(name = "start_date")
	private Date startDate;
	
	@Column(name = "end_date")
	private Date endDate;
	
	@Column(name = "policy_amount")
	private Double policyAmount;
	
	@Column(name = "faulty")
	private Boolean faulty;
}
