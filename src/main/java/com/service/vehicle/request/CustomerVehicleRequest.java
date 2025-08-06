package com.service.vehicle.request;

import com.service.vehicle.entity.VehicleDetails;

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
