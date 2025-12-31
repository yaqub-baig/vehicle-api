package com.autoadda.apis.vehicle.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccidentReportRequest {
    private Integer vehicleId;
    private Integer reportedBy;
    private LocalDate accidentDate;
    private String location;
    private String description;
    private Long policeReportId;
    private List<Long> accidentImageIds;
    private String status;  // Optional, for updates
}
