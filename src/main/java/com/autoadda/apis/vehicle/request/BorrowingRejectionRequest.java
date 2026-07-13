package com.autoadda.apis.vehicle.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowingRejectionRequest {

    private String rejectionReason;

    // Only used when this DTO is submitted to /reject-return: the owner's own
    // observation of whether the key was physically handed back. Ignored for
    // the original PENDING-request rejection flow.
    private Boolean keyReturned;
}
