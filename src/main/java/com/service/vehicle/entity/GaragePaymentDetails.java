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
@Table(name = "garage_payment_details")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class GaragePaymentDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "garage_id")
	private Integer garageId;
	
	@Column(name = "card_number")
	private String cardNumber;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "exp_dte")
	private String expDate;

	@Column(name = "insert_tms")
	private Date insertTms;
}
