package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.TrustedBorrower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrustedBorrowerRepository extends JpaRepository<TrustedBorrower, Long> {

    Optional<TrustedBorrower> findByOwnerIdAndBorrowerId(Integer ownerId, Integer borrowerId);

    List<TrustedBorrower> findByOwnerIdAndIsDeletedFalseOrderByCreatedAtDesc(Integer ownerId);

    boolean existsByOwnerIdAndBorrowerIdAndIsDeletedFalse(Integer ownerId, Integer borrowerId);
}
