package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.VehicleBorrowing;
import com.autoadda.apis.vehicle.enums.BorrowingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleBorrowingRepository extends JpaRepository<VehicleBorrowing, Long> {

    @Query("SELECT COUNT(vb) > 0 FROM VehicleBorrowing vb WHERE vb.vehicleId = :vehicleId " +
           "AND vb.status IN ('PENDING', 'APPROVED', 'ACTIVE', 'RETURN_INITIATED') " +
           "AND vb.isDeleted = false")
    boolean hasActiveBorrowing(@Param("vehicleId") Integer vehicleId);

    Optional<VehicleBorrowing> findByVehicleIdAndStatusInAndIsDeletedFalse(
            Integer vehicleId, List<BorrowingStatus> statuses);

    List<VehicleBorrowing> findByBorrowerIdAndIsDeletedFalseOrderByCreatedAtDesc(Integer borrowerId);

    List<VehicleBorrowing> findByOwnerIdAndIsDeletedFalseOrderByCreatedAtDesc(Integer ownerId);

    List<VehicleBorrowing> findByVehicleIdAndIsDeletedFalseOrderByCreatedAtDesc(Integer vehicleId);

    List<VehicleBorrowing> findByStatusAndIsDeletedFalse(BorrowingStatus status);

    @Query("SELECT vb FROM VehicleBorrowing vb WHERE vb.ownerId = :ownerId " +
           "AND vb.status = 'PENDING' AND vb.isDeleted = false ORDER BY vb.createdAt DESC")
    List<VehicleBorrowing> findPendingBorrowingsForOwner(@Param("ownerId") Integer ownerId);
}
