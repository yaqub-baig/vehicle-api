package com.autoadda.apis.vehicle.entity;


import javax.persistence.*;

import com.autoadda.apis.vehicle.util.Status;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleListing {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne
	private Garage garage;

	private String vin;
	private String make;
	private String model;
	private int year;
	private double price;
	private String description;

	@Enumerated(EnumType.STRING)
	private Status status; // LISTED, SOLD, REMOVED

	private LocalDate listedDate;

	@OneToOne(mappedBy = "vehicleListing", cascade = CascadeType.ALL)
	private VehicleSale sale;
}
