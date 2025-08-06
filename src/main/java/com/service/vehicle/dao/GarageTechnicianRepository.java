package com.service.vehicle.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.service.vehicle.entity.GarageTechnicians;

@Repository
public interface GarageTechnicianRepository extends JpaRepository<GarageTechnicians, Integer> {

	List<GarageTechnicians> findByGarageId(Integer garageId);
}
