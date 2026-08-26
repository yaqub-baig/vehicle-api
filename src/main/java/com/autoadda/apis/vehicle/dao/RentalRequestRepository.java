package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.RentalRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RentalRequestRepository extends JpaRepository<RentalRequest, Long> {

    Optional<RentalRequest> findByIdAndIsDeletedFalse(Long id);

    List<RentalRequest> findByListingIdAndIsDeletedFalseOrderByCreatedAtDesc(Long listingId);

    @Query("SELECT r FROM RentalRequest r WHERE (r.renterId = :userId OR r.ownerId = :userId) " +
           "AND r.isDeleted = false ORDER BY r.createdAt DESC")
    List<RentalRequest> findAllForUser(@Param("userId") Integer userId);

    @Query("SELECT r FROM RentalRequest r WHERE r.ownerId = :ownerId AND r.status = 'PENDING' " +
           "AND r.isDeleted = false ORDER BY r.createdAt DESC")
    List<RentalRequest> findPendingForOwner(@Param("ownerId") Integer ownerId);

    @Query("SELECT COUNT(r) > 0 FROM RentalRequest r WHERE r.renterId = :renterId AND r.listingId = :listingId " +
           "AND r.status = 'PENDING' AND r.isDeleted = false")
    boolean hasActiveRequest(@Param("renterId") Integer renterId, @Param("listingId") Long listingId);
}
