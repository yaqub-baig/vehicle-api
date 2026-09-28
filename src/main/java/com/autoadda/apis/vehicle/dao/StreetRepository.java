package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.Street;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StreetRepository extends JpaRepository<Street, Long> {

    List<Street> findByIsActiveTrue();

    List<Street> findByTerritoryAreaIdAndIsActiveTrue(Long territoryAreaId);

    Optional<Street> findByTerritoryAreaIdAndCodeIgnoreCase(Long territoryAreaId, String code);
}
