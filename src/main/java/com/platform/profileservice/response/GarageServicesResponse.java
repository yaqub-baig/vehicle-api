package com.platform.profileservice.response;

import java.util.List;

import com.platform.profileservice.entity.Garage;
import com.platform.profileservice.entity.PlatformService;

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
