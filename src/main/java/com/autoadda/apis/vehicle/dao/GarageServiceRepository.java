package com.autoadda.apis.vehicle.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.vehicle.entity.GarageServices;

@Repository
public interface GarageServiceRepository extends JpaRepository<GarageServices, Integer> {

	List<GarageServices> findByGarageId(Integer garageId);

	GarageServices findByGarageIdAndServiceId(Integer garageId, Integer serviceId);

}
