package com.autoadda.apis.vehicle.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnershipClaimRequest {

    @NotBlank(message = "Chassis number is required")
    private String chassisNo;

    private String reason; // Why ownership is being requested
}
