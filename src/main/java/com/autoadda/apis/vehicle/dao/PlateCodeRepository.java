package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.PlateCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlateCodeRepository extends JpaRepository<PlateCode, Long> {

    List<PlateCode> findByIsActiveTrueOrderByDisplayOrderAscIdAsc();

    List<PlateCode> findByPlateTypeIdAndIsActiveTrueOrderByDisplayOrderAscIdAsc(Long plateTypeId);

    Optional<PlateCode> findByPlateTypeIdAndCodeIgnoreCase(Long plateTypeId, String code);

    boolean existsByPlateTypeIdAndIsActiveTrue(Long plateTypeId);
}
