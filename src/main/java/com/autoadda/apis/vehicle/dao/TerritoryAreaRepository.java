package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.TerritoryArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TerritoryAreaRepository extends JpaRepository<TerritoryArea, Long> {

    List<TerritoryArea> findByIsActiveTrue();

    List<TerritoryArea> findByStateTerritoryIdAndIsActiveTrue(Long stateTerritoryId);

    Optional<TerritoryArea> findByStateTerritoryIdAndCodeIgnoreCase(Long stateTerritoryId, String code);
}
