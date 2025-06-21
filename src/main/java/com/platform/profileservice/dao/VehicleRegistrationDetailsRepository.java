/**
 * 
 */
package com.platform.profileservice.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.platform.profileservice.entity.InsuranceDetails;
import com.platform.profileservice.entity.VehicleRegistration;

@Repository
public interface VehicleRegistrationDetailsRepository extends JpaRepository<VehicleRegistration, Integer> {}
