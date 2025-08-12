package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.GarageAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GarageAddressRepository extends JpaRepository<GarageAddress, Long> {
}
