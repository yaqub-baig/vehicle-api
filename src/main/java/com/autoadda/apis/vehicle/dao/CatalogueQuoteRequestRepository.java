package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.CatalogueQuoteRequest;
import com.autoadda.apis.vehicle.enums.CatalogueQuoteRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CatalogueQuoteRequestRepository extends JpaRepository<CatalogueQuoteRequest, Long> {

    Optional<CatalogueQuoteRequest> findByIdAndIsDeletedFalse(Long id);

    List<CatalogueQuoteRequest> findByOfferingIdAndIsDeletedFalseOrderByCreatedAtDesc(Long offeringId);

    boolean existsByBuyerUserIdAndOfferingIdAndStatusInAndIsDeletedFalse(
            Integer buyerUserId, Long offeringId, List<CatalogueQuoteRequestStatus> statuses);

    @Query("SELECT q FROM CatalogueQuoteRequest q WHERE q.buyerUserId = :buyerUserId " +
            "AND q.isDeleted = false ORDER BY q.createdAt DESC")
    List<CatalogueQuoteRequest> findAllForBuyer(@Param("buyerUserId") Integer buyerUserId);

    @Query("SELECT q FROM CatalogueQuoteRequest q WHERE q.buyerUserId = :buyerUserId " +
            "AND q.status = :status AND q.isDeleted = false ORDER BY q.createdAt DESC")
    List<CatalogueQuoteRequest> findAllForBuyerAndStatus(@Param("buyerUserId") Integer buyerUserId,
                                                         @Param("status") CatalogueQuoteRequestStatus status);

    @Query("SELECT q FROM CatalogueQuoteRequest q WHERE q.companyId = :companyId " +
            "AND q.isDeleted = false ORDER BY q.createdAt DESC")
    List<CatalogueQuoteRequest> findAllForCompany(@Param("companyId") Long companyId);
}
