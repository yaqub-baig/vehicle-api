package com.platform.profileservice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.platform.profileservice.entity.GarageOwner;

@Repository
public interface GarageOwnerRepository extends JpaRepository<GarageOwner, Integer> {

	List<GarageOwner> findByGarageId(Integer garageId);
}
