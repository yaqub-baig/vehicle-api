package com.autoadda.apis.vehicle.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Compact {id, code, name} reference to a plate master row, embedded in listing responses.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlateRefResponse {

    private Long id;
    private String code;
    private String name;
}
