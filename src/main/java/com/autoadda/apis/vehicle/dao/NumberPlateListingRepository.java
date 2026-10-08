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
           "AND (:plateSourceId IS NULL OR l.plateSource.id = :plateSourceId) " +
           "AND (:plateTypeId IS NULL OR l.plateType.id = :plateTypeId) " +
           "AND (:plateCodeId IS NULL OR l.plateCode.id = :plateCodeId) " +
           "AND (:minPrice IS NULL OR l.expectedPrice >= :minPrice) " +
           "AND (:maxPrice IS NULL OR l.expectedPrice <= :maxPrice) " +
           "ORDER BY l.createdAt DESC")
    List<NumberPlateListing> browseListings(@Param("plateSourceId") Long plateSourceId,
                                             @Param("plateTypeId") Long plateTypeId,
                                             @Param("plateCodeId") Long plateCodeId,
                                             @Param("minPrice") BigDecimal minPrice,
                                             @Param("maxPrice") BigDecimal maxPrice);
}
