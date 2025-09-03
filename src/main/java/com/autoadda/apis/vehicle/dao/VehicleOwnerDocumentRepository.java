package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.VehicleOwnerDocumentation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleOwnerDocumentRepository extends JpaRepository<VehicleOwnerDocumentation, Integer> {
    Optional<VehicleOwnerDocumentation> findByVehicleOwnerId(Integer vehicleOwnerId);
}
