package com.platform.profileservice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.platform.profileservice.entity.PlatformService;

@Repository
public interface PlatformServiceRepository extends JpaRepository<PlatformService, Integer> {

	List<PlatformService> findByIdIn(List<Integer> serviceIds);

}
