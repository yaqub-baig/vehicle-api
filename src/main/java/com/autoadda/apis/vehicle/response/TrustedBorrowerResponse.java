package com.autoadda.apis.vehicle.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrustedBorrowerResponse {

    private Long id;
    private Integer ownerId;
    private Integer borrowerId;
    private String borrowerName;
    private String borrowerMobileNumber;
    private LocalDateTime createdAt;
}
