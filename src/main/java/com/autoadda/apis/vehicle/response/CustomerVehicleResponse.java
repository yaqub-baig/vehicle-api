package com.autoadda.apis.vehicle.response;

import java.util.List;

import com.autoadda.apis.vehicle.entity.Customer;
import com.autoadda.apis.vehicle.entity.VehicleDetails;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerVehicleResponse {
	
	private Customer customer;
	private List<VehicleDetails> vehicles;
}
