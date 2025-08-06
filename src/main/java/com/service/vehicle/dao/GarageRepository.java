package com.service.vehicle.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.service.vehicle.entity.Garage;

@Repository
public interface GarageRepository extends JpaRepository<Garage, Integer> {
}
