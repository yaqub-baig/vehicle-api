package com.autoadda.apis.vehicle.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OwnerInitiatedBorrowingRequest {

    @NotNull(message = "Vehicle ID is required")
    private Integer vehicleId;

    @NotNull(message = "Borrower ID is required")
    private Integer borrowerId;

    @NotNull(message = "Expected return time is required")
    private LocalDateTime expectedReturnTime;

    private List<Long> borrowingImageIds;

    private String borrowingPurpose;
}
