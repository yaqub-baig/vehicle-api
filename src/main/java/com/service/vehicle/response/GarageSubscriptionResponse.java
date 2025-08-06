package com.service.vehicle.response;

import com.service.vehicle.entity.Garage;
import com.service.vehicle.entity.GarageSubscription;

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
public class GarageSubscriptionResponse {
	private Garage garage;
	private GarageSubscription subscription;
}
