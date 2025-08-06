package com.service.vehicle.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.service.vehicle.entity.PlatformService;

@Repository
public interface OldServiceRepository extends JpaRepository<PlatformService, Integer> {

	List<PlatformService> findByIdIn(List<Integer> serviceIds);

}
