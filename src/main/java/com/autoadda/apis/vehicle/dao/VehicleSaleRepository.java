package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.VehicleSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleSaleRepository extends JpaRepository<VehicleSale,Integer> {
}
