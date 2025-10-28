package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company,Long> {
    Optional<Company> findByTradeLicenseNumber(String tradeLicenseNumber);
    Optional<Company> findByCompanyOwnerId(Integer companyOwnerId);
}
