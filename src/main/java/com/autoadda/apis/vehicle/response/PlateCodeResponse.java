package com.autoadda.apis.vehicle.response;

import com.autoadda.apis.vehicle.entity.MediaFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlateCodeResponse {

    private Long id;
    private String code;
    private String colorHex;
    private MediaFile image;
    private Integer displayOrder;
    private Boolean isActive;
    private PlateTypeResponse plateType;
}
