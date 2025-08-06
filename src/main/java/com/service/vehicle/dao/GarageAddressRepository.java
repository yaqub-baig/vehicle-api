package com.service.vehicle.dao;

import com.service.vehicle.entity.GarageAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GarageAddressRepository extends JpaRepository<GarageAddress, Long> {
}
