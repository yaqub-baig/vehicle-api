package com.autoadda.apis.vehicle.mapper;

import com.autoadda.apis.vehicle.entity.AccidentReport;
import com.autoadda.apis.vehicle.request.AccidentReportRequest;
import com.autoadda.apis.vehicle.response.AccidentReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccidentReportMapper {

    private final VehicleMapper vehicleMapper;
    private final MediaFileMapper mediaFileMapper;

    public AccidentReport requestToEntity(AccidentReportRequest request) {
        return AccidentReport.builder()
                .reportedBy(request.getReportedBy())
                .accidentDate(request.getAccidentDate())
                .build();
    }

    public AccidentReportResponse entityToResponse(AccidentReport entity) {
        AccidentReportResponse response = new AccidentReportResponse();
        response.setId(entity.getId());
        response.setReportedBy(entity.getReportedBy());
        response.setAccidentDate(entity.getAccidentDate());
        response.setLocation(entity.getLocation());
        response.setDescription(entity.getDescription());
        response.setStatus(entity.getStatus());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());

        // Map vehicle
        if (entity.getVehicle() != null) {
            response.setVehicle(vehicleMapper.toResponse(entity.getVehicle()));
        }

        // Map police report
        if (entity.getPoliceReport() != null) {
            response.setPoliceReport(mediaFileMapper.toResponse(entity.getPoliceReport()));
        }

        // Map accident images
        if (entity.getAccidentImages() != null && !entity.getAccidentImages().isEmpty()) {
            response.setAccidentImages(
                    entity.getAccidentImages().stream()
                            .map(mediaFileMapper::toResponse)
                           .toList()
            );
        }

        return response;
    }
}
