package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.VehicleListing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleListingRepository extends JpaRepository<VehicleListing, Integer> ,VehicleListingCustomRepository{

    List<VehicleListing> findByVehicleCustomerId(Integer customerId);
    List<VehicleListing> findAllByOrderByCreatedAtDesc();

    // Paginated method
    Page<VehicleListing> findAllByOrderByCreatedAtDesc(Pageable pageable);

}
