package com.service.vehicle.response;

import com.service.vehicle.entity.AuthenticationDetails;
import com.service.vehicle.entity.GarageTechnicians;

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
public class GarageTechniResponse {
	
	private GarageTechnicians technician;
	private AuthenticationDetails authInfo;
}
