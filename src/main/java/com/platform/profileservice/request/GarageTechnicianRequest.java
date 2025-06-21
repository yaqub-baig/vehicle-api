package com.platform.profileservice.request;

import com.platform.profileservice.entity.GarageTechnicians;

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
