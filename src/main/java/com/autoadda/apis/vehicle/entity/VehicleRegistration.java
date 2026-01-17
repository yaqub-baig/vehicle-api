package com.autoadda.apis.vehicle.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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
