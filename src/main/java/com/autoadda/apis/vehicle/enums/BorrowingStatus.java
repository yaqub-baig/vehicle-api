package com.autoadda.apis.vehicle.enums;

public enum BorrowingStatus {
    PENDING,           // Request created, awaiting owner approval
    APPROVED,          // Owner approved, not yet started
    ACTIVE,            // Currently borrowed
    TRANSFER_REQUESTED,// Current borrower proposed handover to another user; awaiting recipient accept/reject
    RETURN_INITIATED,  // Borrower initiated return
    RETURNED,          // Both confirmed, borrowing complete
    REJECTED,          // Owner rejected the request
    CANCELLED          // Borrower cancelled the request
}
