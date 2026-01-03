package com.autoadda.apis.vehicle.service.impl;

import com.autoadda.apis.vehicle.dao.AccidentReportRepository;
import com.autoadda.apis.vehicle.dao.MediaFileRepository;
import com.autoadda.apis.vehicle.dao.VehicleDetailsRepository;
import com.autoadda.apis.vehicle.entity.AccidentReport;
import com.autoadda.apis.vehicle.entity.MediaFile;
import com.autoadda.apis.vehicle.entity.VehicleDetails;
import com.autoadda.apis.vehicle.exception.ResourceNotFoundException;
import com.autoadda.apis.vehicle.mapper.AccidentReportMapper;
import com.autoadda.apis.vehicle.request.AccidentReportRequest;
import com.autoadda.apis.vehicle.response.AccidentReportResponse;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.AccidentReportService;
import com.autoadda.apis.vehicle.util.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccidentReportServiceImpl implements AccidentReportService {

    public static final String ERROR_OCCURRED_WHILE_RETRIEVING_ACCIDENT_REPORTS = "Error occurred while retrieving accident reports";
    public static final String ACCIDENT_REPORT_NOT_FOUND_WITH_ID = "Accident report not found with ID: ";
    public static final String ACCIDENT_REPORTS_RETRIEVED_SUCCESSFULLY = "Accident reports retrieved successfully";
    private final AccidentReportRepository accidentReportRepository;
    private final VehicleDetailsRepository vehicleDetailsRepository;
    private final MediaFileRepository mediaFileRepository;
    private final AccidentReportMapper accidentReportMapper;

    private static final String SUCCESS = "success";
    private static final String FAILED = "failed";

    @Override
    public GenericResponse createAccidentReport(AccidentReportRequest request) {
        try {
            // Validate vehicle exists
            VehicleDetails vehicle = vehicleDetailsRepository.findById(request.getVehicleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Invalid vehicle ID: " + request.getVehicleId()));

            // Map request to entity
            AccidentReport accidentReport = accidentReportMapper.requestToEntity(request);

            if(request.getLocation()!=null){
                accidentReport.setLocation(request.getLocation());
            }
            if(request.getDescription()!=null){
                accidentReport.setDescription(request.getDescription());
            }
            // Set vehicle
            accidentReport.setVehicle(vehicle);

            // Fetch and set police report if provided
            if (request.getPoliceReportId() != null) {
                MediaFile policeReport = mediaFileRepository.findById(request.getPoliceReportId())
                        .orElse(null);
                accidentReport.setPoliceReport(policeReport);
            }

            // Fetch and set accident images if provided
            if (request.getAccidentImageIds() != null && !request.getAccidentImageIds().isEmpty()) {
                List<MediaFile> accidentImages = new ArrayList<>();
                for (Long imageId : request.getAccidentImageIds()) {
                    mediaFileRepository.findById(imageId).ifPresent(accidentImages::add);
                }
                accidentReport.setAccidentImages(accidentImages);
            }

            // Set status if provided, otherwise will default to SUBMITTED via @PrePersist
            if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
                try {
                    accidentReport.setStatus(Status.valueOf(request.getStatus().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid status provided: {}, using default SUBMITTED", request.getStatus());
                }
            }
            // Save entity
            AccidentReport savedReport = accidentReportRepository.save(accidentReport);

            // Map to response
            AccidentReportResponse response = accidentReportMapper.entityToResponse(savedReport);

            return GenericResponse.builder()
                    .code("200")
                    .status(SUCCESS)
                    .message("Accident report created successfully")
                    .body(response)
                    .build();

        } catch (ResourceNotFoundException e) {
            log.error("Vehicle not found: {}", e.getMessage());
            return GenericResponse.builder()
                    .code("404")
                    .status(FAILED)
                    .message(e.getMessage())
                    .body(null)
                    .build();
        } catch (Exception e) {
            log.error("Error creating accident report: {}", e.getMessage(), e);
            return GenericResponse.builder()
                    .code("500")
                    .status(FAILED)
                    .message("Error occurred while creating accident report")
                    .body(null)
                    .build();
        }
    }

    @Override
    public GenericResponse getAccidentReportById(Integer id) {
        try {
            AccidentReport accidentReport = accidentReportRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(ACCIDENT_REPORT_NOT_FOUND_WITH_ID + id));

            AccidentReportResponse response = accidentReportMapper.entityToResponse(accidentReport);

            return GenericResponse.builder()
                    .code("200")
                    .status(SUCCESS)
                    .message("Accident report retrieved successfully")
                    .body(response)
                    .build();

        } catch (ResourceNotFoundException e) {
            log.error("Accident report not found: {}", e.getMessage());
            return GenericResponse.builder()
                    .code("404")
                    .status(FAILED)
                    .message(e.getMessage())
                    .body(null)
                    .build();
        } catch (Exception e) {
            log.error("Error retrieving accident report: {}", e.getMessage(), e);
            return GenericResponse.builder()
                    .code("500")
                    .status(FAILED)
                    .message("Error occurred while retrieving accident report")
                    .body(null)
                    .build();
        }
    }

    @Override
    public GenericResponse getAllAccidentReports() {
        try {
            List<AccidentReport> reports = accidentReportRepository.findAllByOrderByCreatedAtDesc();

            List<AccidentReportResponse> responseList = reports.stream()
                    .map(accidentReportMapper::entityToResponse)
                    .toList();

            return GenericResponse.builder()
                    .code("200")
                    .status(SUCCESS)
                    .message(ACCIDENT_REPORTS_RETRIEVED_SUCCESSFULLY)
                    .body(responseList)
                    .build();

        } catch (Exception e) {
            log.error("Error retrieving accident reports: {}", e.getMessage(), e);
            return GenericResponse.builder()
                    .code("500")
                    .status(FAILED)
                    .message(ERROR_OCCURRED_WHILE_RETRIEVING_ACCIDENT_REPORTS)
                    .body(Collections.emptyList())
                    .build();
        }
    }

    @Override
    public GenericResponse getAccidentReportsByVehicle(Integer vehicleId) {
        try {
            List<AccidentReport> reports = accidentReportRepository.findByVehicleId(vehicleId);

            List<AccidentReportResponse> responseList = reports.stream()
                    .map(accidentReportMapper::entityToResponse)
                    .toList();

            return GenericResponse.builder()
                    .code("200")
                    .status(SUCCESS)
                    .message(ACCIDENT_REPORTS_RETRIEVED_SUCCESSFULLY)
                    .body(responseList)
                    .build();

        } catch (Exception e) {
            log.error("Error retrieving accident reports for vehicle {}: {}", vehicleId, e.getMessage(), e);
            return GenericResponse.builder()
                    .code("500")
                    .status(FAILED)
                    .message(ERROR_OCCURRED_WHILE_RETRIEVING_ACCIDENT_REPORTS)
                    .body(Collections.emptyList())
                    .build();
        }
    }

    @Override
    public GenericResponse getAccidentReportsByReporter(Integer reportedBy) {
        try {
            List<AccidentReport> reports = accidentReportRepository.findByReportedBy(reportedBy);

            List<AccidentReportResponse> responseList = reports.stream()
                    .map(accidentReportMapper::entityToResponse)
                    .toList();

            return GenericResponse.builder()
                    .code("200")
                    .status(SUCCESS)
                    .message(ACCIDENT_REPORTS_RETRIEVED_SUCCESSFULLY)
                    .body(responseList)
                    .build();

        } catch (Exception e) {
            log.error("Error retrieving accident reports for reporter {}: {}", reportedBy, e.getMessage(), e);
            return GenericResponse.builder()
                    .code("500")
                    .status(FAILED)
                    .message(ERROR_OCCURRED_WHILE_RETRIEVING_ACCIDENT_REPORTS)
                    .body(Collections.emptyList())
                    .build();
        }
    }

    @Override
    public GenericResponse updateAccidentReport(Integer id, AccidentReportRequest request) {
        try {
            AccidentReport accidentReport = accidentReportRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(ACCIDENT_REPORT_NOT_FOUND_WITH_ID + id));

            // Update vehicle if provided
            if (request.getVehicleId() != null) {
                VehicleDetails vehicle = vehicleDetailsRepository.findById(request.getVehicleId())
                        .orElseThrow(() -> new ResourceNotFoundException("Invalid vehicle ID: " + request.getVehicleId()));
                accidentReport.setVehicle(vehicle);
            }

            // Update basic fields if provided
            if (request.getReportedBy() != null) {
                accidentReport.setReportedBy(request.getReportedBy());
            }
            if (request.getAccidentDate() != null) {
                accidentReport.setAccidentDate(request.getAccidentDate());
            }
            if (request.getLocation() != null) {
                accidentReport.setLocation(request.getLocation());
            }
            if (request.getDescription() != null) {
                accidentReport.setDescription(request.getDescription());
            }

            // Update police report if provided
            if (request.getPoliceReportId() != null) {
                MediaFile policeReport = mediaFileRepository.findById(request.getPoliceReportId())
                        .orElse(null);
                accidentReport.setPoliceReport(policeReport);
            }

            // Update accident images if provided
            if (request.getAccidentImageIds() != null) {
                List<MediaFile> accidentImages = new ArrayList<>();
                for (Long imageId : request.getAccidentImageIds()) {
                    mediaFileRepository.findById(imageId).ifPresent(accidentImages::add);
                }
                accidentReport.setAccidentImages(accidentImages);
            }

            // Update status if provided
            if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
                try {
                    accidentReport.setStatus(Status.valueOf(request.getStatus().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid status provided: {}, keeping existing status", request.getStatus());
                }
            }

            // Save updated entity
            AccidentReport updatedReport = accidentReportRepository.save(accidentReport);

            // Map to response
            AccidentReportResponse response = accidentReportMapper.entityToResponse(updatedReport);

            return GenericResponse.builder()
                    .code("200")
                    .status(SUCCESS)
                    .message("Accident report updated successfully")
                    .body(response)
                    .build();

        } catch (ResourceNotFoundException e) {
            log.error("Resource not found: {}", e.getMessage());
            return GenericResponse.builder()
                    .code("404")
                    .status(FAILED)
                    .message(e.getMessage())
                    .body(null)
                    .build();
        } catch (Exception e) {
            log.error("Error updating accident report: {}", e.getMessage(), e);
            return GenericResponse.builder()
                    .code("500")
                    .status(FAILED)
                    .message("Error occurred while updating accident report")
                    .body(null)
                    .build();
        }
    }

    @Override
    public GenericResponse deleteAccidentReport(Integer id) {
        try {
            AccidentReport accidentReport = accidentReportRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(ACCIDENT_REPORT_NOT_FOUND_WITH_ID + id));

            accidentReportRepository.delete(accidentReport);

            return GenericResponse.builder()
                    .code("200")
                    .status(SUCCESS)
                    .message("Accident report deleted successfully")
                    .body(null)
                    .build();

        } catch (ResourceNotFoundException e) {
            log.error("Accident report not found: {}", e.getMessage());
            return GenericResponse.builder()
                    .code("404")
                    .status(FAILED)
                    .message(e.getMessage())
                    .body(null)
                    .build();
        } catch (Exception e) {
            log.error("Error deleting accident report: {}", e.getMessage(), e);
            return GenericResponse.builder()
                    .code("500")
                    .status(FAILED)
                    .message("Error occurred while deleting accident report")
                    .body(null)
                    .build();
        }
    }
}
