package com.autoadda.apis.vehicle.enums;

public enum TransferStatus {
    PENDING,    // Requested by current borrower, awaiting recipient action
    ACCEPTED,   // Recipient accepted; borrowerId rotated to recipient
    REJECTED,   // Recipient declined
    CANCELLED   // Requesting borrower withdrew before acceptance
}
