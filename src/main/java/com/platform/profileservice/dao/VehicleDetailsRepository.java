/**
 * 
 */
package com.platform.profileservice.dao;

import java.util.List;

import com.platform.profileservice.entity.MediaFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.platform.profileservice.entity.VehicleDetails;

@Repository
public interface VehicleDetailsRepository extends JpaRepository<VehicleDetails, Integer> {

	List<VehicleDetails> findByCustomerId(Integer customerId);

	List<VehicleDetails> findByCustomerMobileNumber(String userId);

	@Query("SELECT m FROM VehicleDetails v JOIN v.vehicleImages m WHERE v.id = :vehicleId")
	List<MediaFile> findImagesByVehicleId(@Param("vehicleId") Integer vehicleId);
}
