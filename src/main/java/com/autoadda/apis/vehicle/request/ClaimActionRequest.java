package com.autoadda.apis.vehicle.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimActionRequest {

    private String notes; // Owner's notes when approving/rejecting
}
