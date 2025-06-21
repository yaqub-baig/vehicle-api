/**
 * 
 */
package com.platform.profileservice.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.platform.profileservice.entity.InsuranceDetails;

@Repository
public interface InsuranceDetailsRepository extends JpaRepository<InsuranceDetails, String> {}
