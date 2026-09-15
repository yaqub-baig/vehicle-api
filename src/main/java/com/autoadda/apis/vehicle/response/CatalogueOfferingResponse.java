package com.autoadda.apis.vehicle.response;

import com.autoadda.apis.vehicle.entity.MediaFile;
import com.autoadda.apis.vehicle.enums.CatalogueOfferingStatus;
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
public class CatalogueOfferingResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private String brand;
    private String model;
    private String trimName;
    private Integer modelYear;
    private BigDecimal indicativePrice;
    private String description;
    private List<MediaFile> images;
    private CatalogueOfferingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
