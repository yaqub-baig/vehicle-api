package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.PlateType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlateTypeRepository extends JpaRepository<PlateType, Long> {

    List<PlateType> findByIsActiveTrueOrderByDisplayOrderAscIdAsc();

    List<PlateType> findByPlateSourceIdAndIsActiveTrueOrderByDisplayOrderAscIdAsc(Long plateSourceId);

    Optional<PlateType> findByPlateSourceIdAndCodeIgnoreCase(Long plateSourceId, String code);
}
