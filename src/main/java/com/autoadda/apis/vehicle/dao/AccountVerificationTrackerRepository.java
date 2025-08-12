package com.autoadda.apis.vehicle.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.vehicle.entity.AccountVerificationTracker;

@Repository
public interface AccountVerificationTrackerRepository extends JpaRepository<AccountVerificationTracker, Integer>{

	AccountVerificationTracker findByToken(String token);

	AccountVerificationTracker findByTokenAndStatus(String token, String status);

	AccountVerificationTracker findByMemberIdAndMemberTypeAndStatus(int memberId, String memberType, String string);

}
