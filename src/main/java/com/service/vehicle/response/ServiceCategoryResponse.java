package com.service.vehicle.response;

import java.util.List;

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
public class ServiceCategoryResponse {
	
	private Integer id;
	private String category;
	private String desc;
	private List<PlatformService> services;

}
