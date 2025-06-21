package com.platform.profileservice.response;

import java.util.List;

import com.platform.profileservice.entity.Customer;
import com.platform.profileservice.entity.VehicleDetails;

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
