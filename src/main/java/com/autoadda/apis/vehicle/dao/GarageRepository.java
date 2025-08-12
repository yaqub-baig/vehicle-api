package com.autoadda.apis.vehicle.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.vehicle.entity.Garage;

@Repository
public interface GarageRepository extends JpaRepository<Garage, Integer> {
}
