package com.service.vehicle.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GenericResponse {
	
	private String status;
	private String code;
	private String message;
	private Object body;

}
