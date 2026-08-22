package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.NumberPlateListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface NumberPlateListingRepository extends JpaRepository<NumberPlateListing, Long> {

    Optional<NumberPlateListing> findByIdAndIsDeletedFalse(Long id);

    List<NumberPlateListing> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(Integer userId);

    @Query("SELECT l FROM NumberPlateListing l WHERE l.status = 'ACTIVE' AND l.isDeleted = false " +
           "AND (:emirate IS NULL OR LOWER(l.emirate) = LOWER(:emirate)) " +
           "AND (:category IS NULL OR LOWER(l.category) = LOWER(:category)) " +
           "AND (:minPrice IS NULL OR l.expectedPrice >= :minPrice) " +
           "AND (:maxPrice IS NULL OR l.expectedPrice <= :maxPrice) " +
           "ORDER BY l.createdAt DESC")
    List<NumberPlateListing> browseListings(@Param("emirate") String emirate,
                                             @Param("category") String category,
                                             @Param("minPrice") BigDecimal minPrice,
                                             @Param("maxPrice") BigDecimal maxPrice);
}
