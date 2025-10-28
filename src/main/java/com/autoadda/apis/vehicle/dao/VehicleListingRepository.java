package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.VehicleListing;
import com.autoadda.apis.vehicle.request.VehicleSearchCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleListingRepository extends JpaRepository<VehicleListing, Integer> ,VehicleListingCustomRepository{

    List<VehicleListing> findByVehicleCustomerId(Integer customerId);
    List<VehicleListing> findAllByOrderByCreatedAtDesc();

}
