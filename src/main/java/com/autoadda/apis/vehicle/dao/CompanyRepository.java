package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.Company;
import com.autoadda.apis.vehicle.enums.CompanyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company,Long> {
    Optional<Company> findByTradeLicenseNumber(String tradeLicenseNumber);
    List<Company> findByCompanyOwnerId(Integer companyOwnerId);

    @Query("SELECT c FROM Company c JOIN c.types t WHERE t = :type AND c.isActive = true")
    List<Company> findActiveByType(@Param("type") CompanyType type);
}
