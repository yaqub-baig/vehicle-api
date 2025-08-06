package com.service.vehicle.request;

import com.service.vehicle.entity.GarageOwner;

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
public class GarageOwnerRequest {
	private Integer garageId;
	private GarageOwner ownerDetails;
}
