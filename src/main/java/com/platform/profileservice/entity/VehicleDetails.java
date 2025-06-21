package com.platform.profileservice.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vehicle_details")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "vin")
	private String vin;
	
	@Column(name = "year")
	private String year;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "odometer")
	private String odometer;
	
	@Column(name = "color")
	private String color;
	
	@Column(name = "engine_size")
	private String engineSize;
	
	@Column(name = "no_of_doors")
	private String noOfDoors;
	
	@Column(name = "no_of_seats")
	private String noOfSeats;
	
	@Column(name = "no_of_cylinders")
	private String noOfCylinders;
	
	@Column(name = "no_of_speeds")
	private String noOfSpeeds;
	
	@Column(name = "fuel_type")
	private String fuelType;
	
	@Column(name = "model_id")
	private Integer modelId;
	
	@Column(name = "customer_id")
	private Integer customerId;
	
	@OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "insurance_id", referencedColumnName = "id")
    private InsuranceDetails insurance;
	
	
	@OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "registration_id", referencedColumnName = "id")
    private VehicleRegistration registration;

}
