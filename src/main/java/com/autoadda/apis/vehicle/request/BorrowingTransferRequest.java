package com.autoadda.apis.vehicle.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowingTransferRequest {

    @NotNull(message = "Recipient borrower ID is required")
    private Integer toBorrowerId;

    private String reason;

    private String notes;

    private Long transferImageId;
}
