package com.platform.profileservice.request;

import com.platform.profileservice.entity.VehicleDetails;

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
