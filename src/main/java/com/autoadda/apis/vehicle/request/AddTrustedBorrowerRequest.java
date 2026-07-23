package com.autoadda.apis.vehicle.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddTrustedBorrowerRequest {

    @NotNull(message = "Borrower ID is required")
    private Integer borrowerId;
}
