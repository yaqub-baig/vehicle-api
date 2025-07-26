package com.platform.profileservice.dao;

import com.platform.profileservice.entity.GarageAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GarageAddressRepository extends JpaRepository<GarageAddress, Long> {
}
