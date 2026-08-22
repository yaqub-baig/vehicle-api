package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.NumberPlateInterest;
import com.autoadda.apis.vehicle.enums.NumberPlateInterestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NumberPlateInterestRepository extends JpaRepository<NumberPlateInterest, Long> {

    Optional<NumberPlateInterest> findByIdAndIsDeletedFalse(Long id);

    List<NumberPlateInterest> findByListingIdAndIsDeletedFalseOrderByCreatedAtDesc(Long listingId);

    boolean existsByBuyerUserIdAndListingIdAndStatusInAndIsDeletedFalse(
            Integer buyerUserId, Long listingId, List<NumberPlateInterestStatus> statuses);

    @Query("SELECT i FROM NumberPlateInterest i WHERE (i.buyerUserId = :userId OR i.sellerUserId = :userId) " +
           "AND i.isDeleted = false ORDER BY i.createdAt DESC")
    List<NumberPlateInterest> findAllForUser(@Param("userId") Integer userId);

    @Query("SELECT i FROM NumberPlateInterest i WHERE (i.buyerUserId = :userId OR i.sellerUserId = :userId) " +
           "AND i.status = :status AND i.isDeleted = false ORDER BY i.createdAt DESC")
    List<NumberPlateInterest> findAllForUserAndStatus(@Param("userId") Integer userId,
                                                        @Param("status") NumberPlateInterestStatus status);
}
