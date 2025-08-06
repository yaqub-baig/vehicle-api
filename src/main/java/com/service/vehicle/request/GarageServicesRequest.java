package com.service.vehicle.request;

import java.util.List;

import com.service.vehicle.entity.GarageServices;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class GarageServicesRequest {
	private Integer garageId;
	private List<GarageServices> services;
}
