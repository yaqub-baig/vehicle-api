package com.autoadda.apis.vehicle.service.impl;

import com.autoadda.apis.vehicle.dao.VehicleDetailsRepository;
import com.autoadda.apis.vehicle.dao.VehicleListingRepository;
import com.autoadda.apis.vehicle.entity.VehicleDetails;
import com.autoadda.apis.vehicle.entity.VehicleListing;
import com.autoadda.apis.vehicle.exception.InvalidInputException;
import com.autoadda.apis.vehicle.exception.ResourceNotFoundException;
import com.autoadda.apis.vehicle.mapper.VehicleListingMapper;
import com.autoadda.apis.vehicle.request.VehicleListingRequest;
import com.autoadda.apis.vehicle.request.VehicleSearchCriteria;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.response.PagedResponse;
import com.autoadda.apis.vehicle.response.VehicleListingResponse;
import com.autoadda.apis.vehicle.util.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleListingService {
    public static final String SUCCESS = "success";
    private final VehicleListingRepository vehicleListingRepository;
    private final VehicleDetailsRepository vehicleDetailsRepository;
    private final VehicleListingMapper vehicleListingMapper;

    public GenericResponse createVehicleListing(VehicleListingRequest vehicleListingRequest) {
        VehicleListing vehicleListing = vehicleListingMapper.requestToEntity ( vehicleListingRequest );
        VehicleDetails vehicleDetails = vehicleDetailsRepository.findById(vehicleListingRequest.getVehicleId()).orElseThrow(() -> new ResourceNotFoundException("invalid vehicle id"));
        vehicleDetails.setIsForSale(Boolean.TRUE);
        vehicleListing.setVehicle (vehicleDetails);
        VehicleListing listedVehicle = vehicleListingRepository.save ( vehicleListing );
        vehicleDetailsRepository.save(vehicleDetails);
        return GenericResponse.builder ( ).code ( "200" ).status (SUCCESS).message ( "Vehicle listed successfully" ).body ( vehicleListingMapper.entityToResponse ( listedVehicle ) ).build ( );
    }
    public GenericResponse getVehicleListings(VehicleSearchCriteria criteria) {
        try {
            // Validate range filters
            criteria.validateRanges();

            List<VehicleListing> vehicles = findVehiclesByCriteria(criteria);

            if (vehicles.isEmpty()) {
                return GenericResponse.builder()
                        .body(Collections.emptyList())
                        .code("200")
                        .message("No vehicles found matching the criteria")
                        .build();
            }

            List<VehicleListingResponse> responseList = vehicles.stream()
//                    .sorted( Comparator.comparing(VehicleListing::getCreatedAt).reversed())
//                    .filter(vehicleListing -> !vehicleListing.getStatus().equals(Status.REMOVED))
                    .map(vehicleListingMapper::entityToResponse)
                    .toList();

            return GenericResponse.builder()
                    .body(responseList)
                    .code("200")
                    .message(SUCCESS)
                    .build();

        } catch (InvalidInputException e) {
            log.error("Invalid search criteria: {}", e.getMessage(), e);
            return GenericResponse.builder()
                    .body(null)
                    .code("400")
                    .status("failed")
                    .message(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Error fetching vehicle listings: {}", e.getMessage(), e);
            return GenericResponse.builder()
                    .body( Collections.emptyList())
                    .code("500")
                    .message("Error occurred while fetching vehicle listings")
                    .build();
        }
    }

    private List<VehicleListing> findVehiclesByCriteria(VehicleSearchCriteria criteria) {
        if (criteria.hasGlobalSearch() && !criteria.hasSpecificCriteria()) {
            return vehicleListingRepository.findByAdvancedSearch(criteria);
        }

        // Check if any search criteria is provided
        if (criteria.hasAnyCriteria()) {
            return vehicleListingRepository.findByCriteria(criteria);
        } else {
            // Return all active vehicles sorted by creation date (newest first)
            return vehicleListingRepository.findAllByOrderByCreatedAtDesc();
        }
    }

    public GenericResponse getVehicleListingById(Integer id) {
        return GenericResponse.builder ( ).status (SUCCESS)
                .code ( "200" )
                .message ( "vehicle retrieved successfully" )
                .body ( vehicleListingRepository.findById ( id )
                .map ( vehicleListingMapper::entityToResponse )
                .orElseThrow ( () -> new ResourceNotFoundException ( "invalid vehicle listing id" ) ) )
                .build ( );
    }

    public GenericResponse getVehicleListingsByUserId(Integer userId) {
        List<VehicleListing> byVehicleCustomerId = vehicleListingRepository.findByVehicleCustomerId ( userId );
        List<VehicleListingResponse> vehicleListingResponseStream = byVehicleCustomerId.stream ( ).map ( vehicleListingMapper::entityToResponse ).toList();
        return GenericResponse.builder().body(vehicleListingResponseStream).code("200").message(SUCCESS).build();
    }

    public GenericResponse updateVehicleListing(Integer id, VehicleListingRequest vehicleListingRequest) {
        VehicleListing vehicleListing = vehicleListingRepository.findById ( id ).orElseThrow ( () -> new ResourceNotFoundException ( "invalid vehicle listing id" ) );
        if(vehicleListingRequest.getCity ()!=null){
            vehicleListing.setCity ( vehicleListingRequest.getCity ( ) );
        }
        if(vehicleListingRequest.getDescription ()!=null){
            vehicleListing.setDescription ( vehicleListingRequest.getDescription ( ) );
        }
        if(vehicleListingRequest.getPrice ()!=null){
            vehicleListing.setPrice ( vehicleListingRequest.getPrice ( ) );
        }
        if(vehicleListingRequest.getStatus ()!=null){
            vehicleListing.setStatus ( Status.valueOf ( vehicleListingRequest.getStatus ( ).toUpperCase ( ) ));
        }
        if(vehicleListingRequest.getCondition ()!=null){
            vehicleListing.setCondition ( VehicleListingMapper.parseCondition ( vehicleListingRequest.getCondition ( ) ) );
        }
        VehicleListing save = vehicleListingRepository.save ( vehicleListing );
        VehicleListingResponse body = vehicleListingMapper.entityToResponse ( save );
        return GenericResponse.builder().body( body ).code("200").message(SUCCESS).build();
    }

    // Paginated methods
    public GenericResponse getVehicleListingsWithPagination(VehicleSearchCriteria criteria) {
        try {
            // Validate sort parameters (will throw IllegalArgumentException if invalid)
            criteria.getSortByOrDefault();
            criteria.getSortDirectionOrDefault();

            // Validate range filters
            criteria.validateRanges();

            // Find vehicles with pagination
            Page<VehicleListing> page = findVehiclesByCriteriaWithPagination(criteria);

            // Map entities to responses
            List<VehicleListingResponse> responseList = page.getContent().stream()
                    .map(vehicleListingMapper::entityToResponse)
                    .toList();

            // Build paged response
            PagedResponse<VehicleListingResponse> pagedResponse = PagedResponse.<VehicleListingResponse>builder()
                    .content(responseList)
                    .pageNumber(page.getNumber())
                    .pageSize(page.getSize())
                    .totalElements(page.getTotalElements())
                    .totalPages(page.getTotalPages())
                    .first(page.isFirst())
                    .last(page.isLast())
                    .empty(page.isEmpty())
                    .numberOfElements(page.getNumberOfElements())
                    .build();

            return GenericResponse.builder()
                    .body(pagedResponse)
                    .code("200")
                    .message(SUCCESS)
                    .build();

        } catch (InvalidInputException e) {
            log.error("Invalid search criteria: {}", e.getMessage(), e);
            return GenericResponse.builder()
                    .body(null)
                    .code("400")
                    .status("failed")
                    .message(e.getMessage())
                    .build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid pagination parameters: {}", e.getMessage(), e);
            return GenericResponse.builder()
                    .body(null)
                    .code("400")
                    .status("failed")
                    .message(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Error fetching vehicle listings: {}", e.getMessage(), e);
            return GenericResponse.builder()
                    .body(null)
                    .code("500")
                    .status("failed")
                    .message("Error occurred while fetching vehicle listings")
                    .build();
        }
    }

    private Page<VehicleListing> findVehiclesByCriteriaWithPagination(VehicleSearchCriteria criteria) {
        if (criteria.hasGlobalSearch() && !criteria.hasSpecificCriteria()) {
            return vehicleListingRepository.findByAdvancedSearchWithPagination(criteria);
        }

        // For any search criteria or default case
        return vehicleListingRepository.findByCriteriaWithPagination(criteria);
    }
}
