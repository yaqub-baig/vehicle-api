package com.autoadda.apis.vehicle.entity;

import javax.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InspectionReport {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private LocalDate inspectionDate;

	private String inspectorName;

	private String remarks;

	private Boolean passed;

	@OneToOne
	@JoinColumn(name = "vehicle_sale_id")
	private VehicleSale vehicleSale;
}
