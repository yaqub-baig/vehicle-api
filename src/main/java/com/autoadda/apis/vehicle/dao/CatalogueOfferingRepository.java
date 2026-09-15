package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.CatalogueOffering;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CatalogueOfferingRepository extends JpaRepository<CatalogueOffering, Long> {

    Optional<CatalogueOffering> findByIdAndIsDeletedFalse(Long id);

    List<CatalogueOffering> findByCompanyIdAndIsDeletedFalseOrderByCreatedAtDesc(Long companyId);

    @Query(value = "SELECT o FROM CatalogueOffering o WHERE o.status = 'ACTIVE' AND o.isDeleted = false " +
            "AND (:companyId IS NULL OR o.companyId = :companyId) " +
            "AND (:brand IS NULL OR LOWER(o.brand) = LOWER(:brand)) " +
            "AND (:model IS NULL OR LOWER(o.model) = LOWER(:model)) " +
            "AND (:minPrice IS NULL OR o.indicativePrice >= :minPrice) " +
            "AND (:maxPrice IS NULL OR o.indicativePrice <= :maxPrice) " +
            "AND (:fromYear IS NULL OR o.modelYear >= :fromYear) " +
            "AND (:toYear IS NULL OR o.modelYear <= :toYear)",
            countQuery = "SELECT COUNT(o) FROM CatalogueOffering o WHERE o.status = 'ACTIVE' AND o.isDeleted = false " +
                    "AND (:companyId IS NULL OR o.companyId = :companyId) " +
                    "AND (:brand IS NULL OR LOWER(o.brand) = LOWER(:brand)) " +
                    "AND (:model IS NULL OR LOWER(o.model) = LOWER(:model)) " +
                    "AND (:minPrice IS NULL OR o.indicativePrice >= :minPrice) " +
                    "AND (:maxPrice IS NULL OR o.indicativePrice <= :maxPrice) " +
                    "AND (:fromYear IS NULL OR o.modelYear >= :fromYear) " +
                    "AND (:toYear IS NULL OR o.modelYear <= :toYear)")
    Page<CatalogueOffering> browseOfferings(@Param("companyId") Long companyId,
                                            @Param("brand") String brand,
                                            @Param("model") String model,
                                            @Param("minPrice") BigDecimal minPrice,
                                            @Param("maxPrice") BigDecimal maxPrice,
                                            @Param("fromYear") Integer fromYear,
                                            @Param("toYear") Integer toYear,
                                            Pageable pageable);
}
