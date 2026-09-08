package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.VehicleSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleSaleRepository extends JpaRepository<VehicleSale,Integer> {

    Optional<VehicleSale> findByVehicleListing_Id(Integer listingId);

    Optional<VehicleSale> findByVehicle_Id(Integer vehicleId);

    List<VehicleSale> findByPreviousOwnerIdOrderBySaleDateDesc(Integer previousOwnerId);

    List<VehicleSale> findByNewOwnerIdOrderBySaleDateDesc(Integer newOwnerId);
}
