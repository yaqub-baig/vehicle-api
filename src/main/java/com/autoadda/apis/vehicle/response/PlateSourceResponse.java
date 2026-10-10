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
public class PlateSourceResponse {

    private Long id;
    private String code;
    private String name;
    private Integer displayOrder;
    private MediaFile image;
    private Boolean isActive;
}
