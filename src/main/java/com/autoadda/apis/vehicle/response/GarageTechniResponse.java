package com.autoadda.apis.vehicle.response;

import com.autoadda.apis.vehicle.entity.AuthenticationDetails;
import com.autoadda.apis.vehicle.entity.GarageTechnicians;

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
