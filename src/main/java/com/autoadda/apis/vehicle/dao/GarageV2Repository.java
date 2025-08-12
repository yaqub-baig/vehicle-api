package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.GarageV2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GarageV2Repository extends JpaRepository<GarageV2, Integer> {
    List<GarageV2>  findByOwnerId(Integer ownerId);

    long countByGarageLogoId(Long mediaFileId);

}
