package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.CarpoolConnection;
import com.autoadda.apis.vehicle.enums.CarpoolConnectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarpoolConnectionRepository extends JpaRepository<CarpoolConnection, Long> {

    Optional<CarpoolConnection> findByIdAndIsDeletedFalse(Long id);

    @Query("SELECT c FROM CarpoolConnection c WHERE (c.requesterUserId = :userId OR c.targetUserId = :userId) " +
           "AND c.isDeleted = false ORDER BY c.createdAt DESC")
    List<CarpoolConnection> findAllForUser(@Param("userId") Integer userId);

    @Query("SELECT c FROM CarpoolConnection c WHERE (c.requesterUserId = :userId OR c.targetUserId = :userId) " +
           "AND c.status = :status AND c.isDeleted = false ORDER BY c.createdAt DESC")
    List<CarpoolConnection> findAllForUserAndStatus(@Param("userId") Integer userId,
                                                      @Param("status") CarpoolConnectionStatus status);

    boolean existsByRequesterListingIdAndTargetListingIdAndStatusInAndIsDeletedFalse(
            Long requesterListingId, Long targetListingId, List<CarpoolConnectionStatus> statuses);

    List<CarpoolConnection> findByStatusAndIsDeletedFalse(CarpoolConnectionStatus status);
}
