package com.autoadda.apis.vehicle.response;

import java.util.List;

import com.autoadda.apis.vehicle.entity.Garage;
import com.autoadda.apis.vehicle.entity.PlatformService;

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
public class GarageServicesResponse {
	
	private Garage garage;
	private List<PlatformService> services;

}
