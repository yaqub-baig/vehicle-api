package com.platform.profileservice.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.platform.profileservice.entity.GarageSubscription;

@Repository
public interface GarageSubscriptionRepository extends JpaRepository<GarageSubscription, Integer> {
	
	GarageSubscription findByGarageId(Integer garageId);
}
