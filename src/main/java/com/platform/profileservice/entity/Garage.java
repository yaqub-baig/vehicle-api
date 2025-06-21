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
@Table(name = "garage_details")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class Garage {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "legal_name")
	private String legalName;
	
	@Column(name = "garage_name")
	private String garageName;
	
	@Column(name = "email_id")
	private String emailId;
	
	@Column(name = "contact_number")
	private String contactNumber;
	
	@Column(name = "website")
	private String webSite;
	
	@Column(name = "account_status")
	private String accountStatus;
	
	@Column(name = "garage_type")
	private String garageType;
	
	@Column(name = "chain_id")
	private Integer chainId;
	
	@Column(name = "insert_tms")
	private Date insertTms;
	
	
	
}
