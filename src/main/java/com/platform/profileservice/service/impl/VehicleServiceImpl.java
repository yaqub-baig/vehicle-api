package com.platform.profileservice.service.impl;

import com.platform.profileservice.dao.MediaFileRepository;
import com.platform.profileservice.dao.UserRepository;
import com.platform.profileservice.dao.VehicleDetailsRepository;
import com.platform.profileservice.entity.MediaFile;
import com.platform.profileservice.entity.User;
import com.platform.profileservice.entity.VehicleDetails;
import com.platform.profileservice.exception.ResourceNotFoundException;
import com.platform.profileservice.mapper.MediaFileMapper;
import com.platform.profileservice.mapper.VehicleMapper;
import com.platform.profileservice.request.VehicleRegistrationRequest;
import com.platform.profileservice.response.GenericResponse;
import com.platform.profileservice.response.MediaFileResponse;
import com.platform.profileservice.response.VehicleDetailResponse;
import com.platform.profileservice.service.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
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
    public GenericResponse addVehicleDetails(VehicleRegistrationRequest request, String mobileNumber) {
        User user = userRepository.findByMobileNumber ( mobileNumber ).orElseThrow ( () -> new ResourceNotFoundException ( "User not found" ) );
        VehicleDetails entity = vehicleMapper.toEntity ( request );

        if ( request.getMulkiaImageFront ( ) != null ) {
            MediaFile mulkiaImageFront = mediaFileRepository.findById ( request.getMulkiaImageFront ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "Mulkia image not found" ) );
            entity.setMulkiaImageFront ( mulkiaImageFront );
        } else {
            throw new ResourceNotFoundException ( "mulkia image front is required" );
        }
        if ( request.getMulkiaImageBack ( ) != null ) {
            MediaFile mulkiaImageBack = mediaFileRepository.findById ( request.getMulkiaImageBack ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "Mulkia image not found" ) );
            entity.setMulkiaImageBack ( mulkiaImageBack );
        } else {
            throw new ResourceNotFoundException ( "mulkia image back is required" );
        }
        if ( request.getVehicleImages ( ) != null ) {
            List<MediaFile> vehicleImages = request.getVehicleImages ( ).stream ( )
                    .map ( id -> mediaFileRepository.findById ( id ).orElseThrow ( () -> new ResourceNotFoundException ( "Vehicle image not found" ) ) ).toList ( );
            entity.setVehicleImages ( vehicleImages );
        } else {
            throw new ResourceNotFoundException ( "vehicle images are required" );
        }
        entity.setCustomer ( user );
        vehicleRepository.save ( entity );
        return GenericResponse.builder ( )
                .status ( "success" )
                .code ( "200" )
                .message ( "Vehicle added successfully" )
                .build ( );
    }

    @Override
    public GenericResponse getVehicleDetails(String userId) {
        log.info ( "1. Getting vehicle details for user id: {}", userId );
        List<VehicleDetails> byCustomerMobileNumber = vehicleRepository.findByCustomerMobileNumber ( userId );
        log.info ( "2. Vehicle list size: {}", byCustomerMobileNumber.size ( ) );
        List<VehicleDetailResponse> list1 = byCustomerMobileNumber.stream ( )
                .sorted ( (o1, o2) -> o2.getId ( ).compareTo ( o1.getId ( ) ) )
                .filter ( vehicleDetails -> !vehicleDetails.isDeleted ( ))
                .map ( vehicleMapper::toResponse ).toList ( );

        return GenericResponse.builder ( )
                .body ( list1 )
                .code ( "200" )
                .message ( "success" )
                .status ( "success" )
                .build ( );
    }

    @Override
    public GenericResponse getVehicleDetailsByVehicleId(Integer vehicleId, String userId, String userRole) {
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
    public GenericResponse getVehicleImagesByVehicleId(Integer vehicleId, String userId, String userRole) {
        List<MediaFileResponse> list = vehicleRepository.findImagesByVehicleId ( vehicleId )
                .stream ( ).map ( mediaFileMapper::toResponse ).toList ( );
        return GenericResponse.builder ( ).body ( list.isEmpty ( ) ? "No images found" : list )
                .code ( "200" ).message ( "success" ).build ( );
    }

    @Override
    public GenericResponse updateVehicleDetails(Integer vehicleId, VehicleRegistrationRequest request, String userId) {
        VehicleDetails vehicleDetails = vehicleRepository.findById ( vehicleId ).orElseThrow ( () -> new ResourceNotFoundException ( "Vehicle not found" ) );
        Long prevId=null;
        vehicleMapper.updateEntity ( vehicleDetails, request );
        if(request.getMulkiaImageFront ()!=null){
            MediaFile front = mediaFileRepository.findById ( request.getMulkiaImageFront ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "Vehicle image not found" ) );
            log.info("-----------1");
            prevId=vehicleDetails.getMulkiaImageFront ().getId ();
            log.info("-----------2");
            vehicleDetails.setMulkiaImageFront (null  );
            log.info("-----------3");
            vehicleDetails.setMulkiaImageFront (front );
            log.info("-----------4");
            mediaFileRepository.deleteById ( prevId );
            log.info("-----------5");
        }
        if(request.getMulkiaImageBack ()!=null){
            MediaFile back = mediaFileRepository.findById ( request.getMulkiaImageBack ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "Vehicle image not found" ) );
            prevId=vehicleDetails.getMulkiaImageBack ().getId ();
            vehicleDetails.setMulkiaImageBack (null);
            vehicleDetails.setMulkiaImageBack (back );
            mediaFileRepository.deleteById ( prevId );
        }

        if ( request.getVehicleImages ( ) != null ) {
            log.info ( "1         Vehicle images size: {}", request.getVehicleImages ( ).size ( ) );
            vehicleDetails.getVehicleImages ( ).clear ( );
            List<MediaFile> vehicleImages = request.getVehicleImages ( ).stream ( )
                    .map ( id -> mediaFileRepository.findById ( id ).orElseThrow ( () -> new ResourceNotFoundException ( "Vehicle image not found" ) ) ).collect ( Collectors.toList ( ) );
            log.info ( "2 ------>Vehicle images size: {}", vehicleImages.size ( ) );
            vehicleDetails.getVehicleImages (  ).addAll ( vehicleImages );
        }
        VehicleDetails save = vehicleRepository.save ( vehicleDetails );
        VehicleDetailResponse response = vehicleMapper.toResponse ( save );
        return GenericResponse.builder ( ).body ( response ).code ( "200" ).message ( "success" ).status ( "success" ).build ( );
    }

    @Override
    public GenericResponse deleteVehicleDetails(Integer vehicleId, String userId) {
        VehicleDetails vehicleDetails = vehicleRepository.findById ( vehicleId ).orElseThrow ( () -> new ResourceNotFoundException ( "Vehicle not found" ) );
        vehicleDetails.setDeleted ( true );
        vehicleRepository.save ( vehicleDetails );
        return GenericResponse.builder ( ).body ( "Vehicle deleted" ).code ( "200" ).message ( "success" ).status ( "success" ).build ( );
    }
}
