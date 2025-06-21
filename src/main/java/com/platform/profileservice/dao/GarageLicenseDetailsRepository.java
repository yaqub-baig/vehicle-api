package com.platform.profileservice.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.platform.profileservice.entity.GarageLicenseDetails;

@Repository
public interface GarageLicenseDetailsRepository extends JpaRepository<GarageLicenseDetails, Integer> {

	GarageLicenseDetails findByGarageId(Integer garageId);

}
