package com.platform.profileservice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.platform.profileservice.entity.GarageDocumentDetails;

@Repository
public interface GarageDocumentDetailsRepository extends JpaRepository<GarageDocumentDetails, Integer> {

	List<GarageDocumentDetails> findByGarageId(Integer garageId);
}
