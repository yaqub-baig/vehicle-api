package com.autoadda.apis.vehicle.service.impl;

import com.autoadda.apis.vehicle.dao.MediaFileRepository;
import com.autoadda.apis.vehicle.dao.VehicleOwnerDocumentRepository;
import com.autoadda.apis.vehicle.entity.MediaFile;
import com.autoadda.apis.vehicle.entity.VehicleOwnerDocumentation;
import com.autoadda.apis.vehicle.exception.ResourceNotFoundException;
import com.autoadda.apis.vehicle.request.VehicleOwnerDocumentRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleOwnerDocumentService {
    private final VehicleOwnerDocumentRepository vehicleDocumentRepository;
    private final MediaFileRepository mediaFileRepository;

    public GenericResponse uploadVehicleOwnerDocument(VehicleOwnerDocumentRequest vehicleOwnerDocumentRequest) {
        VehicleOwnerDocumentation documentation = new VehicleOwnerDocumentation ( );
        documentation.setVehicleOwnerId ( vehicleOwnerDocumentRequest.getVehicleOwnerId ( ) );

        if ( vehicleOwnerDocumentRequest.getEmirateId ( ) != null ) {
            MediaFile emirateId = mediaFileRepository.findById ( vehicleOwnerDocumentRequest.getEmirateId ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "emirate image not found" ) );
            documentation.setEmiratesId ( emirateId );

        }
        if ( vehicleOwnerDocumentRequest.getTradeLicenceId ( ) != null ) {
            MediaFile tradeLicenceId = mediaFileRepository.findById ( vehicleOwnerDocumentRequest.getTradeLicenceId ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "trade licence image not found" ) );
            documentation.setTradeLicence ( tradeLicenceId );

        }
        if ( vehicleOwnerDocumentRequest.getDrivingLicenceId ( ) != null ) {
            MediaFile drivingLicence = mediaFileRepository.findById ( vehicleOwnerDocumentRequest.getDrivingLicenceId ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "driving licence image not found" ) );
            documentation.setDrivingLicence ( drivingLicence );

        }
        VehicleOwnerDocumentation save = vehicleDocumentRepository.save ( documentation );

        return GenericResponse.builder ( )
                .status ( "success" )
                .code ( "200" )
                .message ( "Vehicle owner document uploaded successfully" )
                .body ( save )
                .build ( );
    }


    public GenericResponse getVehicleOwnerDocument(Integer vehicleOwnerId) {
        VehicleOwnerDocumentation documentation = vehicleDocumentRepository.findByVehicleOwnerId ( vehicleOwnerId ).orElseThrow ( () -> new ResourceNotFoundException ( "Vehicle owner document not found" ) );
        return GenericResponse.builder ( )
                .status ( "success" )
                .code ( "200" )
                .message ( "Vehicle owner document fetched successfully" )
                .body ( documentation )
                .build ( );
    }

    public GenericResponse updateVehicleOwnerDocument(Integer vehicleOwnerId, VehicleOwnerDocumentRequest vehicleOwnerDocumentRequest) {
        VehicleOwnerDocumentation documentation;
        documentation = vehicleDocumentRepository.findByVehicleOwnerId ( vehicleOwnerId ).orElse (  new VehicleOwnerDocumentation ( ) );
        documentation.setVehicleOwnerId ( vehicleOwnerId );
        if ( vehicleOwnerDocumentRequest.getDrivingLicenceId ( ) != null ) {
            MediaFile drivingLicence = mediaFileRepository.findById ( vehicleOwnerDocumentRequest.getDrivingLicenceId ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "driving licence image not found" ) );
            documentation.setDrivingLicence ( drivingLicence );
        }
        if ( vehicleOwnerDocumentRequest.getEmirateId ( ) != null ) {
            MediaFile emirateId = mediaFileRepository.findById ( vehicleOwnerDocumentRequest.getEmirateId ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "emirate image not found" ) );
            documentation.setEmiratesId ( emirateId );

        }
        if ( vehicleOwnerDocumentRequest.getTradeLicenceId ( ) != null ) {
            MediaFile tradeLicenceId = mediaFileRepository.findById ( vehicleOwnerDocumentRequest.getTradeLicenceId ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "trade licence image not found" ) );
            documentation.setTradeLicence ( tradeLicenceId );
        }
        vehicleDocumentRepository.save ( documentation );
        return GenericResponse.builder ( )
                .status ( "success" )
                .code ( "200" )
                .message ( "Vehicle owner document updated successfully" )
                .body ( documentation )
                .build ( );
    }
}
