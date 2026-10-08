package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.PlateSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlateSourceRepository extends JpaRepository<PlateSource, Long> {

    List<PlateSource> findByIsActiveTrueOrderByDisplayOrderAscIdAsc();

    Optional<PlateSource> findByCodeIgnoreCase(String code);
}
