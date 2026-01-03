package com.autoadda.apis.vehicle.enums;

public enum ClaimStatusEnum {
    PENDING,    // Awaiting owner approval
    APPROVED,   // Owner approved, ownership transferred
    REJECTED,   // Owner rejected the claim
    CANCELLED   // Requester cancelled the claim
}
