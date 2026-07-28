package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.CompanyDriver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyDriverRepository extends JpaRepository<CompanyDriver, Long> {

    Optional<CompanyDriver> findByCompanyIdAndDriverUserId(Long companyId, Integer driverUserId);

    List<CompanyDriver> findByCompanyIdAndIsDeletedFalseOrderByCreatedAtDesc(Long companyId);

    boolean existsByCompanyIdAndDriverUserIdAndIsDeletedFalse(Long companyId, Integer driverUserId);
}
