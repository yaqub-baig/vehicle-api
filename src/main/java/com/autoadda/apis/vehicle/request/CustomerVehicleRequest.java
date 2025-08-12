package com.autoadda.apis.vehicle.request;

import com.autoadda.apis.vehicle.entity.VehicleDetails;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class CustomerVehicleRequest {
	private Integer customerId;
	private VehicleDetails vehicle;

}
