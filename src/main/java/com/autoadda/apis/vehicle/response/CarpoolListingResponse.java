package com.autoadda.apis.vehicle.response;

import com.autoadda.apis.vehicle.enums.CarpoolListingStatus;
import com.autoadda.apis.vehicle.enums.CarpoolListingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarpoolListingResponse {

    private Long id;
    private Integer userId;
    private String posterName;
    private CarpoolListingType type;
    private String originCity;
    private String destinationCity;
    private LocalDateTime travelDateTime;
    private BigDecimal budget;
    private Integer availableSeats;
    private String carDescription;
    private CarpoolListingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
