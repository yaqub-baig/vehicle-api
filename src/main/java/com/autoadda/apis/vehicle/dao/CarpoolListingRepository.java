package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.CarpoolListing;
import com.autoadda.apis.vehicle.enums.CarpoolListingStatus;
import com.autoadda.apis.vehicle.enums.CarpoolListingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CarpoolListingRepository extends JpaRepository<CarpoolListing, Long> {

    Optional<CarpoolListing> findByIdAndIsDeletedFalse(Long id);

    List<CarpoolListing> findByUserIdAndDiscoverableTrueAndIsDeletedFalseOrderByCreatedAtDesc(Integer userId);

    List<CarpoolListing> findByUserIdAndTypeAndDiscoverableTrueAndIsDeletedFalseOrderByCreatedAtDesc(
            Integer userId, CarpoolListingType type);

    List<CarpoolListing> findByStatusAndTravelDateTimeBeforeAndIsDeletedFalse(
            CarpoolListingStatus status, LocalDateTime cutoff);

    @Query("SELECT l FROM CarpoolListing l WHERE l.type = :type AND l.status = :status AND l.isDeleted = false " +
           "AND l.discoverable = true " +
           "AND (:originTerritoryAreaId IS NULL OR l.originAddress.territoryArea.id = :originTerritoryAreaId) " +
           "AND (:destinationTerritoryAreaId IS NULL OR l.destinationAddress.territoryArea.id = :destinationTerritoryAreaId) " +
           "ORDER BY l.travelDateTime ASC")
    List<CarpoolListing> browseListings(@Param("type") CarpoolListingType type,
                                         @Param("originTerritoryAreaId") Long originTerritoryAreaId,
                                         @Param("destinationTerritoryAreaId") Long destinationTerritoryAreaId,
                                         @Param("status") CarpoolListingStatus status);

    @Query("SELECT l FROM CarpoolListing l WHERE l.type = 'OFFER' AND l.status = 'ACTIVE' AND l.isDeleted = false " +
           "AND l.discoverable = true " +
           "AND l.originAddress.territoryArea.id = :originTerritoryAreaId " +
           "AND l.destinationAddress.territoryArea.id = :destinationTerritoryAreaId " +
           "AND l.userId <> :excludeUserId AND l.budget <= :seekerBudget AND l.travelDateTime >= :now " +
           "ORDER BY l.travelDateTime ASC")
    List<CarpoolListing> findMatchingOffersForSeeker(@Param("originTerritoryAreaId") Long originTerritoryAreaId,
                                                       @Param("destinationTerritoryAreaId") Long destinationTerritoryAreaId,
                                                       @Param("excludeUserId") Integer excludeUserId,
                                                       @Param("seekerBudget") BigDecimal seekerBudget,
                                                       @Param("now") LocalDateTime now);

    @Query("SELECT l FROM CarpoolListing l WHERE l.type = 'SEEKER' AND l.status = 'ACTIVE' AND l.isDeleted = false " +
           "AND l.discoverable = true " +
           "AND l.originAddress.territoryArea.id = :originTerritoryAreaId " +
           "AND l.destinationAddress.territoryArea.id = :destinationTerritoryAreaId " +
           "AND l.userId <> :excludeUserId AND l.budget >= :offerPricePerSeat AND l.travelDateTime >= :now " +
           "ORDER BY l.travelDateTime ASC")
    List<CarpoolListing> findMatchingSeekersForOffer(@Param("originTerritoryAreaId") Long originTerritoryAreaId,
                                                       @Param("destinationTerritoryAreaId") Long destinationTerritoryAreaId,
                                                       @Param("excludeUserId") Integer excludeUserId,
                                                       @Param("offerPricePerSeat") BigDecimal offerPricePerSeat,
                                                       @Param("now") LocalDateTime now);
}
