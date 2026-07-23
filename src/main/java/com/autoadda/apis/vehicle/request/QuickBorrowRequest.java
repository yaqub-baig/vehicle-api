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
public class QuickBorrowRequest {

    @NotNull(message = "Vehicle ID is required")
    private Integer vehicleId;

    // Required only when caller is the vehicle owner assigning to someone else.
    // Ignored (forced to caller) when caller is not the owner.
    private Integer borrowerId;

    private LocalDateTime expectedReturnTime;

    private List<Long> borrowingImageIds;

    private String borrowingPurpose;
}
