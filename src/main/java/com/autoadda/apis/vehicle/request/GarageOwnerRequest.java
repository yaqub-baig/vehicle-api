package com.autoadda.apis.vehicle.request;

import com.autoadda.apis.vehicle.entity.GarageOwner;

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
