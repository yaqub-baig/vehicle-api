package com.service.vehicle.response;

import java.util.List;

import com.service.vehicle.entity.Garage;
import com.service.vehicle.entity.PlatformService;

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
