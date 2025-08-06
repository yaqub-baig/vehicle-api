package com.service.vehicle.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.service.vehicle.entity.GaragePendingActions;

@Repository
public interface GaragePendingActionsRepository extends JpaRepository<GaragePendingActions, Integer> {

	GaragePendingActions findByGarageId(Integer garageId);
	
	List<GaragePendingActions> findByLicenseUpdateIndAndTechUpdateIndAndOwnerUpdateIndAndEmailVerifyIndAndSearchInd(String lcnInd, String techInd, String ownInd, String emInd, String srInd);

	List<GaragePendingActions> findByLicenseUpdateIndAndTechUpdateIndAndOwnerUpdateIndAndEmailVerifyIndAndServiceUpdateIndAndSearchInd(
			String lcnInd, String techInd, String ownInd, String emInd,String serInd, String srInd);

	List<GaragePendingActions> findByLicenseUpdateIndAndTechUpdateIndAndEmailVerifyIndAndServiceUpdateIndAndSearchInd(
			String lcnInd, String techInd, String emInd,String serInd, String srInd);
}
