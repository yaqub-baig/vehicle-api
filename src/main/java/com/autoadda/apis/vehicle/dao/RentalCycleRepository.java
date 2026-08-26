package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.RentalCycle;
import com.autoadda.apis.vehicle.enums.RentalCycleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RentalCycleRepository extends JpaRepository<RentalCycle, Long> {

    Optional<RentalCycle> findByIdAndIsDeletedFalse(Long id);

    Optional<RentalCycle> findByRentalRequestIdAndIsDeletedFalse(Long rentalRequestId);

    List<RentalCycle> findByRenterIdAndIsDeletedFalseOrderByCreatedAtDesc(Integer renterId);

    List<RentalCycle> findByOwnerIdAndIsDeletedFalseOrderByCreatedAtDesc(Integer ownerId);

    boolean existsByVehicleIdAndStatusInAndIsDeletedFalse(Integer vehicleId, List<RentalCycleStatus> statuses);
}
