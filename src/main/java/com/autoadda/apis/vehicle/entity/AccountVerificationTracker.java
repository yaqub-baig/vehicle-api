package com.autoadda.apis.vehicle.entity;

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
@Table(name = "account_verification_tracker")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountVerificationTracker {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "member_id")
	private Integer memberId;
	
	@Column(name = "member_type")
	private String memberType;
	
	@Column(name = "token")
	private String token;
	
	@Column(name = "status")
	private String status;
	
	@Column(name = "verify_start_date")
	private Date verifyStartDate;
	
	@Column(name = "verify_end_date")
	private Date verifyEndDate;
	
	@Column(name = "verified_date")
	private Date verifiedDate;
	

}
