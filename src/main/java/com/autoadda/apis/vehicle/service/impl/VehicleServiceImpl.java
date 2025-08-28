package com.autoadda.apis.vehicle.service.impl;

import com.autoadda.apis.vehicle.dao.MediaFileRepository;
import com.autoadda.apis.vehicle.dao.UserRepository;
import com.autoadda.apis.vehicle.dao.VehicleDetailsRepository;
import com.autoadda.apis.vehicle.entity.MediaFile;
import com.autoadda.apis.vehicle.entity.VehicleDetails;
import com.autoadda.apis.vehicle.exception.ResourceNotFoundException;
import com.autoadda.apis.vehicle.mapper.MediaFileMapper;
import com.autoadda.apis.vehicle.mapper.VehicleMapper;
import com.autoadda.apis.vehicle.request.VehicleRegistrationRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.response.MediaFileResponse;
import com.autoadda.apis.vehicle.response.VehicleDetailResponse;
import com.autoadda.apis.vehicle.service.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleServiceImpl implements VehicleService {
    private final VehicleDetailsRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;
    private final MediaFileMapper mediaFileMapper;
    private final UserRepository userRepository;
    private final MediaFileRepository mediaFileRepository;

    @Override
    public GenericResponse addVehicleDetails(VehicleRegistrationRequest request, String autoAddaId) {
        Optional<VehicleDetails> byChassisNo = vehicleRepository.findByChassisNo ( request.getChassisNo ( ) );
        if(byChassisNo.isPresent ()){
            throw new ResourceNotFoundException ( "Vehicle chassis number already exists" );
        }
        VehicleDetails entity = vehicleMapper.toEntity ( request );
        entity.setCustomerId ( Integer.parseInt ( autoAddaId ) );

        if ( request.getMulkiaImageFront ( ) != null ) {
            MediaFile mulkiaImageFront = mediaFileRepository.findById ( request.getMulkiaImageFront ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "Mulkia image not found" ) );
            entity.setMulkiaImageFront ( mulkiaImageFront );
        }
        if ( request.getMulkiaImageBack ( ) != null ) {
            MediaFile mulkiaImageBack = mediaFileRepository.findById ( request.getMulkiaImageBack ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "Mulkia image not found" ) );
            entity.setMulkiaImageBack ( mulkiaImageBack );
        }
        if ( request.getVehicleImages ( ) != null ) {
            List<MediaFile> vehicleImages = request.getVehicleImages ( ).stream ( )
                    .map ( id -> mediaFileRepository.findById ( id ).orElseThrow ( () -> new ResourceNotFoundException ( "Vehicle image not found" ) ) ).toList ( );
            entity.setVehicleImages ( vehicleImages );
        }
        vehicleRepository.save ( entity );
        return GenericResponse.builder ( )
                .status ( "success" )
                .code ( "200" )
                .message ( "Vehicle added successfully" )
                .build ( );
    }

    @Override
    public GenericResponse getVehicleDetails(String autoAddaId) {

        List<VehicleDetails> byCustomerMobileNumber = vehicleRepository.findByCustomerId ( Integer.parseInt ( autoAddaId ) );
        log.info ( "2. Vehicle list size: {}", byCustomerMobileNumber.size ( ) );
        List<VehicleDetailResponse> list1 = byCustomerMobileNumber.stream ( )
                .sorted ( (o1, o2) -> o2.getId ( ).compareTo ( o1.getId ( ) ) )
                .filter ( vehicleDetails -> ! vehicleDetails.isDeleted ( ) )
                .map ( vehicleMapper::toResponse ).toList ( );

        return GenericResponse.builder ( )
                .body ( list1 )
                .code ( "200" )
                .message ( "success" )
                .status ( "success" )
                .build ( );
    }

    @Override
    public GenericResponse getVehicleDetailsByVehicleId(Integer vehicleId) {
        VehicleDetails vehicleDetails = vehicleRepository.findById ( vehicleId ).orElseThrow ( () -> new ResourceNotFoundException ( "Vehicle not found" ) );
        if ( vehicleDetails.isDeleted ( ) ) {
            throw new ResourceNotFoundException ( "Vehicle deleted" );
        }

        VehicleDetailResponse response = vehicleMapper.toResponse ( vehicleDetails );
        return GenericResponse.builder ( ).body ( response )
                .code ( "200" )
                .message ( "success" ).build ( );

    }

    @Override
    public GenericResponse getVehicleImagesByVehicleId(Integer vehicleId) {
        List<MediaFileResponse> list = vehicleRepository.findImagesByVehicleId ( vehicleId )
                .stream ( ).map ( mediaFileMapper::toResponse ).toList ( );
        return GenericResponse.builder ( ).body ( list.isEmpty ( ) ? "No images found" : list )
                .code ( "200" ).message ( "success" ).build ( );
    }

    @Override
    @Transactional
    public GenericResponse updateVehicleDetails(Integer vehicleId, VehicleRegistrationRequest request, String userId) {
        VehicleDetails vehicleDetails = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        // Store old media file references
        MediaFile oldFrontImage = vehicleDetails.getMulkiaImageFront();
        MediaFile oldBackImage = vehicleDetails.getMulkiaImageBack();
        List<MediaFile> oldVehicleImages = new ArrayList<>(vehicleDetails.getVehicleImages());

        // Update basic vehicle details
        vehicleMapper.updateEntity(vehicleDetails, request);

        // Handle front mulkia image update
        if (request.getMulkiaImageFront() != null) {
            MediaFile newFrontImage = mediaFileRepository.findById(request.getMulkiaImageFront())
                    .orElseThrow(() -> new ResourceNotFoundException("Front mulkia image not found"));
            vehicleDetails.setMulkiaImageFront(newFrontImage);
        }
        
        // Handle back mulkia image update
        if (request.getMulkiaImageBack() != null) {
            MediaFile newBackImage = mediaFileRepository.findById(request.getMulkiaImageBack())
                    .orElseThrow(() -> new ResourceNotFoundException("Back mulkia image not found"));
            vehicleDetails.setMulkiaImageBack(newBackImage);
        }
        
        // Handle vehicle images update
        if (request.getVehicleImages() != null && !request.getVehicleImages().isEmpty()) {
            vehicleDetails.getVehicleImages().clear();
            List<MediaFile> vehicleImages = request.getVehicleImages().stream()
                    .map(id -> mediaFileRepository.findById(id)
                            .orElseThrow(() -> new ResourceNotFoundException("Vehicle image not found with id: " + id)))
                    .toList();
            vehicleDetails.getVehicleImages().addAll(vehicleImages);
        }
        
        // Save the vehicle details
        VehicleDetails savedVehicle = vehicleRepository.save(vehicleDetails);
        
        // Delete old media files that are no longer referenced
        if (oldFrontImage != null && !oldFrontImage.equals(savedVehicle.getMulkiaImageFront())) {
            mediaFileRepository.delete(oldFrontImage);
        }
        if (oldBackImage != null && !oldBackImage.equals(savedVehicle.getMulkiaImageBack())) {
            mediaFileRepository.delete(oldBackImage);
        }
        if (request.getVehicleImages() != null) {
            oldVehicleImages.stream()
                    .filter(oldImg -> !savedVehicle.getVehicleImages().contains(oldImg))
                    .forEach(mediaFileRepository::delete);
        }
        
        VehicleDetailResponse response = vehicleMapper.toResponse(savedVehicle);
        return GenericResponse.builder()
                .body(response)
                .code("200")
                .message("Vehicle updated successfully")
                .status("success")
                .build();
    }

    @Override
    public GenericResponse deleteVehicleDetails(Integer vehicleId, String userId) {
        VehicleDetails vehicleDetails = vehicleRepository.findById ( vehicleId ).orElseThrow ( () -> new ResourceNotFoundException ( "Vehicle not found" ) );
        vehicleDetails.setDeleted ( true );
        vehicleRepository.save ( vehicleDetails );
        return GenericResponse.builder ( ).body ( "Vehicle deleted" ).code ( "200" ).message ( "success" ).status ( "success" ).build ( );
    }
}
