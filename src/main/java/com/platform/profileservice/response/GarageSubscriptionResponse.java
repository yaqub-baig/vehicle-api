package com.platform.profileservice.response;

import com.platform.profileservice.entity.Garage;
import com.platform.profileservice.entity.GarageSubscription;

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
