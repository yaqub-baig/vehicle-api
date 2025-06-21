package com.platform.profileservice.response;

import com.platform.profileservice.entity.AuthenticationDetails;
import com.platform.profileservice.entity.GarageTechnicians;

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
