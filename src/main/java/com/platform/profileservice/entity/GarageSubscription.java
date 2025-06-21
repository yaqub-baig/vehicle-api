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
@Table(name = "garage_subscription")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GarageSubscription {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private int id;
	
	@Column(name = "garage_id")
	private int garageId;
	
	@Column(name = "subscription_id")
	private Integer subscriptionId;
	
	@Column(name = "subscription_type")
	private String subscriptionType;
	
	@Column(name = "eff_dte")
	private Date effectiveDate;
	
	@Column(name = "exp_dte")
	private Date expDate;
	
	@Column(name = "active_ind")
	private String activeInd;
	
	@Column(name = "insert_tms")
	private Date insertTms;
	
}
