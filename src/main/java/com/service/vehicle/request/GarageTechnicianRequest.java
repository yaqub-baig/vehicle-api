package com.service.vehicle.request;

import com.service.vehicle.entity.GarageTechnicians;

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
public class GarageTechnicianRequest {
	private Integer garageId;
	private GarageTechnicians technicianDetails;
}
