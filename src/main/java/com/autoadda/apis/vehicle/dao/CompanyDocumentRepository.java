package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.CompanyDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyDocumentRepository extends JpaRepository<CompanyDocument, Long> {
    Optional<CompanyDocument> findByCompanyId(Long companyId);
}
