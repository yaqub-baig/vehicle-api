package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.BorrowingTransfer;
import com.autoadda.apis.vehicle.enums.TransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowingTransferRepository extends JpaRepository<BorrowingTransfer, Long> {

    List<BorrowingTransfer> findByBorrowingIdAndIsDeletedFalseOrderByCreatedAtAsc(Long borrowingId);

    Optional<BorrowingTransfer> findByBorrowingIdAndStatusAndIsDeletedFalse(Long borrowingId, TransferStatus status);

    @Query("SELECT COUNT(bt) > 0 FROM BorrowingTransfer bt WHERE bt.borrowingId = :borrowingId " +
           "AND bt.status = 'PENDING' AND bt.isDeleted = false")
    boolean hasPendingTransfer(@Param("borrowingId") Long borrowingId);

    List<BorrowingTransfer> findByToBorrowerIdAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(
            Integer toBorrowerId, TransferStatus status);
}
