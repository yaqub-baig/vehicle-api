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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
        VehicleDetails existing = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));

        vehicleMapper.updateEntity(existing, request);

        List<Long> filesToCleanup = new ArrayList<>();

        handleMulkiaImageFront(request, existing, filesToCleanup);
        handleMulkiaImageBack(request, existing, filesToCleanup);
        handleVehicleImages(request, existing, filesToCleanup);

        VehicleDetails save = vehicleRepository.save(existing);

        if (!filesToCleanup.isEmpty()) {
            scheduleMediaFileCleanup(filesToCleanup);
        }

        VehicleDetailResponse response = vehicleMapper.toResponse(save);
        return GenericResponse.builder()
                .body(response)
                .code("200")
                .message("success")
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

    @Override
    public VehicleDetailResponse getVehicleDetailsByVehicleIdForIsc(Integer vehicleId) {
        VehicleDetails vehicleDetails = vehicleRepository.findById ( vehicleId ).orElseThrow ( () -> new ResourceNotFoundException ( "Vehicle not found" ) );
        if ( vehicleDetails.isDeleted ( ) ) {
            throw new ResourceNotFoundException ( "Vehicle deleted" );
        }

        return vehicleMapper.toResponse ( vehicleDetails );

    }

    private void handleMulkiaImageFront(VehicleRegistrationRequest request, VehicleDetails existing, List<Long> filesToCleanup) {
        if (request.getMulkiaImageFront() != null) {
            Long prevImageId = existing.getMulkiaImageFront() != null ? existing.getMulkiaImageFront().getId() : null;
            MediaFile mediaFile = mediaFileRepository.findById(request.getMulkiaImageFront())
                    .orElseThrow(() -> new ResourceNotFoundException("Mulkia front image not found"));
            existing.setMulkiaImageFront(mediaFile);

            if (prevImageId != null && !prevImageId.equals(mediaFile.getId())) {
                filesToCleanup.add(prevImageId);
            }
        }
    }

    private void handleMulkiaImageBack(VehicleRegistrationRequest request, VehicleDetails existing, List<Long> filesToCleanup) {
        if (request.getMulkiaImageBack() != null) {
            Long prevImageId = existing.getMulkiaImageBack() != null ? existing.getMulkiaImageBack().getId() : null;
            MediaFile mediaFile = mediaFileRepository.findById(request.getMulkiaImageBack())
                    .orElseThrow(() -> new ResourceNotFoundException("Mulkia back image not found"));
            existing.setMulkiaImageBack(mediaFile);

            if (prevImageId != null && !prevImageId.equals(mediaFile.getId())) {
                filesToCleanup.add(prevImageId);
            }
        }
    }

    private void handleVehicleImages(VehicleRegistrationRequest request, VehicleDetails existing, List<Long> filesToCleanup) {
        if (request.getVehicleImages() != null && !request.getVehicleImages().isEmpty()) {
            List<Long> oldVehicleImageIds = existing.getVehicleImages().stream()
                    .map(MediaFile::getId)
                    .toList();
            if (!oldVehicleImageIds.isEmpty()) {
                filesToCleanup.addAll(oldVehicleImageIds);
            }

            List<MediaFile> newVehicleImages = new ArrayList<>();
            request.getVehicleImages().forEach(imageId -> {
                MediaFile mediaFile = mediaFileRepository.findById(imageId)
                        .orElseThrow(() -> new ResourceNotFoundException("Vehicle image not found"));
                newVehicleImages.add(mediaFile);
            });
            existing.setVehicleImages(newVehicleImages);
        }
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void scheduleMediaFileCleanup(List<Long> mediaFileIds) {
        try {
            Thread.sleep(3000);

            for (Long fileId : mediaFileIds) {
                try {
                    if (!isMediaFileStillReferenced(fileId)) {
                        mediaFileRepository.deleteById(fileId);
                        log.info("Successfully cleaned up media file: {}", fileId);
                    } else {
                        log.info("Media file {} still referenced, skipping cleanup", fileId);
                    }
                } catch (Exception e) {
                    log.warn("Failed to cleanup media file {}: {}", fileId, e.getMessage());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Media file cleanup interrupted");
        }
    }

    private boolean isMediaFileStillReferenced(Long mediaFileId) {
        long mulkiaFrontCount = vehicleRepository.countByMulkiaImageFrontId(mediaFileId);
        long mulkiaBackCount = vehicleRepository.countByMulkiaImageBackId(mediaFileId);
        long vehicleImageCount = vehicleRepository.countByVehicleImageId(mediaFileId);
        long totalReferences = mulkiaFrontCount + mulkiaBackCount + vehicleImageCount;
        return totalReferences > 0;
    }
}
