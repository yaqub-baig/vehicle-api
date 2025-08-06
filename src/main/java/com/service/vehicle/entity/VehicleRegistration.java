package com.service.vehicle.entity;

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
@Table(name = "vehicle_registration")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class VehicleRegistration {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "state")
	private String state;
	
	@Column(name = "code")
	private String code;
	
	@Column(name = "category")
	private String category;
	
	@Column(name = "plate_number")
	private String plateNumber;
	
	@Column(name = "registered_name")
	private String registeredName;
}
