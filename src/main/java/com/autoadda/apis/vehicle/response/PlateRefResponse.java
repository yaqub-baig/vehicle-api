package com.autoadda.apis.vehicle.response;

import com.autoadda.apis.vehicle.entity.MediaFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Compact {id, code, name} reference to a plate master row, embedded in listing responses.
// image is set on the source ref (emirate template) and on the code ref when that code overrides
// it; colorHex only on the code ref. Lets the frontend draw the plate preview without extra calls.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlateRefResponse {

    private Long id;
    private String code;
    private String name;
    private MediaFile image;
    private String colorHex;
}
