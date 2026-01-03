package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.VehicleOwnershipClaim;
import com.autoadda.apis.vehicle.enums.ClaimStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleOwnershipClaimRepository extends JpaRepository<VehicleOwnershipClaim, Long> {

    // Find claims by chassis number
    List<VehicleOwnershipClaim> findByChassisNo(String chassisNo);

    // Find claims by requester
    List<VehicleOwnershipClaim> findByRequesterId(Integer requesterId);

    // Find claims by current owner
    List<VehicleOwnershipClaim> findByCurrentOwnerId(Integer currentOwnerId);

    // Find pending claims for a specific owner
    List<VehicleOwnershipClaim> findByCurrentOwnerIdAndStatus(Integer currentOwnerId, ClaimStatusEnum status);

    // Find pending claims by chassis number
    Optional<VehicleOwnershipClaim> findByChassisNoAndStatus(String chassisNo, ClaimStatusEnum status);

    // Check if requester has pending claim for chassis
    @Query("SELECT COUNT(c) > 0 FROM VehicleOwnershipClaim c WHERE c.chassisNo = :chassisNo AND c.requesterId = :requesterId AND c.status = 'PENDING'")
    boolean hasActiveClaim(@Param("chassisNo") String chassisNo, @Param("requesterId") Integer requesterId);

    // Find all pending claims for vehicles owned by user
    @Query("SELECT c FROM VehicleOwnershipClaim c WHERE c.currentOwnerId = :ownerId AND c.status = 'PENDING' AND c.isDeleted = false ORDER BY c.requestDate DESC")
    List<VehicleOwnershipClaim> findPendingClaimsForOwner(@Param("ownerId") Integer ownerId);

    // Find claim history for a vehicle
    @Query("SELECT c FROM VehicleOwnershipClaim c WHERE c.vehicleId = :vehicleId ORDER BY c.requestDate DESC")
    List<VehicleOwnershipClaim> findClaimHistoryByVehicleId(@Param("vehicleId") Integer vehicleId);
}
