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
public class VehicleSale {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne
	private VehicleDetails vehicle;

	private Integer previousOwnerId;

	private Integer newOwnerId;

	private LocalDate saleDate;

	private Double salePrice;

	@OneToOne
	private Customer buyer;

	private String paymentMode; // e.g., CASH, CREDIT, LOAN

	@Enumerated(EnumType.STRING)
	private Status status; // ACTIVE, REMOVED

	@OneToOne(mappedBy = "vehicleSale", cascade = CascadeType.ALL)
	private InspectionReport inspectionReport;

	@OneToOne(mappedBy = "vehicleSale", cascade = CascadeType.ALL)
	private VehicleSaleInvoice invoice;

	@OneToOne
	@JoinColumn(name = "vehicle_listing_id")
	private VehicleListing vehicleListing;
}
