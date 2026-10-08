package com.autoadda.apis.vehicle.response;

import com.autoadda.apis.vehicle.entity.MediaFile;
import com.autoadda.apis.vehicle.enums.NumberPlateListingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NumberPlateListingResponse {

    private Long id;
    private Integer userId;
    private String sellerName;
    private String plateNumber;
    private PlateRefResponse plateSource;
    private PlateRefResponse plateType;
    private PlateRefResponse plateCode;
    private BigDecimal expectedPrice;
    private String description;
    private List<MediaFile> images;
    private NumberPlateListingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
