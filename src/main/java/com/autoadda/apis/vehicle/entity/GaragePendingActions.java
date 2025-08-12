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
@Table(name = "garage_pending_actions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class GaragePendingActions {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "garage_id")
	private Integer garageId;
	
	@Column(name = "license_update_ind")
	private String licenseUpdateInd;
	
	@Column(name = "payment_ind")
	private String paymentInd;
	
	@Column(name = "tech_update_ind")
	private String techUpdateInd;
	
	@Column(name = "owner_update_ind")
	private String ownerUpdateInd;
	
	@Column(name = "email_verify_ind")
	private String emailVerifyInd;
	
	@Column(name = "service_updte_ind")
	private String serviceUpdateInd;
	
	@Column(name = "search_ind")
	private String searchInd;
	
	@Column(name = "subscription_id")
	private Integer subscriptionId;
	
	@Column(name = "insert_tms")
	private Date insertTms;
	
}
