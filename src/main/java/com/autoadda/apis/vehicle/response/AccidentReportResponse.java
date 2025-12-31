package com.autoadda.apis.vehicle.response;

import com.autoadda.apis.vehicle.util.Status;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AccidentReportResponse {
    private Integer id;
    private VehicleDetailResponse vehicle;
    private Integer reportedBy;
    private LocalDate accidentDate;
    private String location;
    private String description;
    private MediaFileResponse policeReport;
    private List<MediaFileResponse> accidentImages;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
