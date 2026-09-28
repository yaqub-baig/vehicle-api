package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.StateTerritory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StateTerritoryRepository extends JpaRepository<StateTerritory, Long> {

    List<StateTerritory> findByIsActiveTrue();

    List<StateTerritory> findByCountryIdAndIsActiveTrue(Long countryId);

    Optional<StateTerritory> findByCountryIdAndCodeIgnoreCase(Long countryId, String code);
}
