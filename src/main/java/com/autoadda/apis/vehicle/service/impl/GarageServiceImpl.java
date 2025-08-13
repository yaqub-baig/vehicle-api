package com.autoadda.apis.vehicle.service.impl;


import com.autoadda.apis.vehicle.dao.*;
import com.autoadda.apis.vehicle.entity.*;

import com.autoadda.apis.vehicle.exception.ResourceNotFoundException;
import com.autoadda.apis.vehicle.mapper.GarageV2Mapper;
import com.autoadda.apis.vehicle.request.GarageV2Request;
import com.autoadda.apis.vehicle.response.GarageV2Response;
import com.autoadda.apis.vehicle.service.GarageAddressService;
import com.autoadda.apis.vehicle.service.GarageService;
import com.autoadda.apis.vehicle.util.ERole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class GarageServiceImpl implements GarageService {
    private final GarageV2Repository garageV2Repository;
    private final GarageV2Mapper garageV2Mapper;
    private final MediaFileRepository mediaFileRepository;
    private final ServiceTypeRepository serviceTypeRepository;
    private final GarageAddressService garageAddressService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public ResponseEntity<GarageV2Response> createGarage(GarageV2Request request, String userId, String userRole) {
        GarageV2 garageV2 = garageV2Mapper.toEntity ( request );
//        log.info ( "1   {}", garageV2.toString ( ) );
        // - garageLogo
        if ( request.getGarageLogo ( ) != null ) {
            MediaFile garageLogo = mediaFileRepository.findById ( request.getGarageLogo ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "garage logo not found" ) );
            garageV2.setGarageLogo ( garageLogo );
            log.info ( "2   {}", garageV2.getGarageLogo ( ).getId ( ) );
        }
        // - tradeLicenseAttachment
        if ( request.getTradeLicenseAttachmentIds ( ) != null ) {
            request.getTradeLicenseAttachmentIds ( ).forEach ( id -> {
                MediaFile mediaFile = mediaFileRepository.findById ( id ).orElseThrow ( () -> new ResourceNotFoundException ( "media file not found" ) );
                garageV2.getTradeLicenseAttachment ( ).add ( mediaFile );
            } );
            log.info ( "3   {}", garageV2.getTradeLicenseAttachment ( ).size ( ) );
        }
        // - vatCertificate
        if ( request.getVatCertificateIds ( ) != null ) {
            request.getVatCertificateIds ( ).forEach ( id -> {
                MediaFile mediaFile = mediaFileRepository.findById ( id ).orElseThrow ( () -> new ResourceNotFoundException ( "media file not found" ) );
                garageV2.getVatCertificate ( ).add ( mediaFile );
            } );
            log.info ( "4   {}", garageV2.getVatCertificate ( ).size ( ) );
        }

        // - garageImages
        if ( request.getGarageImageIds ( ) != null ) {
            request.getGarageImageIds ( ).forEach ( id -> {
                MediaFile mediaFile = mediaFileRepository.findById ( id ).orElseThrow ( () -> new ResourceNotFoundException ( "media file not found" ) );
                garageV2.getGarageImages ( ).add ( mediaFile );
            } );
            log.info ( "4   {}", garageV2.getGarageImages ( ).size ( ) );
        }

        // - serviceTypes
        if ( request.getServiceTypeIds ( ) != null ) {
            request.getServiceTypeIds ( ).forEach ( id -> {
                ServiceType serviceType = serviceTypeRepository.findById ( id ).orElseThrow ( () -> new ResourceNotFoundException ( "service type not found" ) );
                garageV2.getServiceTypes ( ).add ( serviceType );
            } );
            log.info ( "5   {}", garageV2.getGarageImages ( ).size ( ) );
        }

        // - garageAddress
        if ( request.getGarageAddress ( ) != null ) {
            GarageAddress garageAddress = garageAddressService.createGarageAddress ( request.getGarageAddress ( ) );
            garageV2.setGarageAddress ( garageAddress );
            log.info ( "6   {}", garageV2.getGarageAddress ( ).getAddress ( ) );
        }

        // - owner
        if ( userId != null ) {
            User user = userRepository.findByMobileNumber ( userId ).orElseThrow ( () -> new ResourceNotFoundException ( "user not found" ) );
            Set<Role> roles = user.getRoles ( );
            Role garageOwner = roleRepository.findByName ( ERole.GARAGE_OWNER ).orElseThrow ( () -> new ResourceNotFoundException ( "role not found" ) );
            if ( ! roles.contains ( garageOwner ) ) {
                roles.add ( garageOwner );
            }
            garageV2.setOwner ( userRepository.save ( user ) );
            log.info ( "7   {}", garageV2.getOwner ( ).getId ( ) );
        }
        log.info ( "8   {}", garageV2.getGarageArabicName ( ) );
        GarageV2Response response = garageV2Mapper.toResponse ( garageV2Repository.save ( garageV2 ) );

        return new ResponseEntity<> ( response, HttpStatus.CREATED );
    }

    @Override
    public ResponseEntity<List<GarageV2Response>> getGarage(String userId, String userRole) {
        User user = userRepository.findByMobileNumber ( userId ).orElseThrow ( () -> new ResourceNotFoundException ( "user not found" ) );
        List<GarageV2> garageList = garageV2Repository.findByOwnerId ( user.getId ( ) );
        List<GarageV2Response> response = garageList.stream ( ).filter ( GarageV2::isActive ).map ( garageV2Mapper::toResponse ).toList ( );
        return new ResponseEntity<> ( response, HttpStatus.OK );

    }

    @Override
    public ResponseEntity<String> deleteGarage(String userId, String userRole, Integer garageId) {
        User user = userRepository.findByMobileNumber ( userId ).orElseThrow ( () -> new ResourceNotFoundException ( "user not found" ) );
        List<GarageV2> garageList = garageV2Repository.findByOwnerId ( user.getId ( ) );
        GarageV2 garage = garageList.stream ( ).filter ( g -> g.getId ( ).equals ( garageId ) ).findFirst ( ).orElseThrow ( () -> new ResourceNotFoundException ( "garage not found" ) );
        if ( garageList.contains ( garage ) ) {
            garage.setActive ( false );
        } else {
            throw new ResourceNotFoundException ( "garage not found " );
        }
        garageV2Repository.save ( garage );
        return new ResponseEntity<> ( "Garage deleted successfully", HttpStatus.OK );
    }

    @Override
    @Transactional
    public ResponseEntity<GarageV2Response> updateGarage(String userId, String userRole, Integer garageId, GarageV2Request request) {
        GarageV2 garage = garageV2Repository.findById ( garageId )
                .orElseThrow ( () -> new ResourceNotFoundException ( "garage not found" ) );

        if ( ! userId.equals ( garage.getOwner ( ).getMobileNumber ( ) ) ) {
            throw new ResourceNotFoundException ( "garage not found" );
        }

        garageV2Mapper.updateGarage ( garage, request );

        // Collect old files for cleanup
        List<Long> filesToCleanup = new ArrayList<> ( );

        // Handle garage logo update
        if ( request.getGarageLogo ( ) != null ) {
            Long prevLogoId = garage.getGarageLogo ( ) != null ? garage.getGarageLogo ( ).getId ( ) : null;

            MediaFile garageLogo = mediaFileRepository.findById ( request.getGarageLogo ( ) )
                    .orElseThrow ( () -> new ResourceNotFoundException ( "garage logo not found" ) );
            garage.setGarageLogo ( garageLogo );

            // Schedule old logo for cleanup if changed
            if ( prevLogoId != null && ! prevLogoId.equals ( request.getGarageLogo ( ) ) ) {
                filesToCleanup.add ( prevLogoId );
            }

            log.info ( "Updated garage logo from {} to {}", prevLogoId, garageLogo.getId ( ) );
        }

        // Handle trade license attachments
        if ( request.getTradeLicenseAttachmentIds ( ) != null ) {
            garage.getTradeLicenseAttachment ( ).clear ( );
            request.getTradeLicenseAttachmentIds ( ).forEach ( id -> {
                MediaFile mediaFile = mediaFileRepository.findById ( id )
                        .orElseThrow ( () -> new ResourceNotFoundException ( "media file not found" ) );
                garage.getTradeLicenseAttachment ( ).add ( mediaFile );
            } );
        }

        // Handle garage images
        if ( request.getGarageImageIds ( ) != null ) {
            garage.getGarageImages ( ).clear ( );
            request.getGarageImageIds ( ).forEach ( id -> {
                MediaFile mediaFile = mediaFileRepository.findById ( id )
                        .orElseThrow ( () -> new ResourceNotFoundException ( "media file not found" ) );
                garage.getGarageImages ( ).add ( mediaFile );
            } );
        }

        // Handle VAT certificates
        if ( request.getVatCertificateIds ( ) != null ) {
            garage.getVatCertificate ( ).clear ( );
            request.getVatCertificateIds ( ).forEach ( id -> {
                MediaFile mediaFile = mediaFileRepository.findById ( id )
                        .orElseThrow ( () -> new ResourceNotFoundException ( "media file not found" ) );
                garage.getVatCertificate ( ).add ( mediaFile );
            } );
        }

        // Handle service types
        if ( request.getServiceTypeIds ( ) != null ) {
            garage.getServiceTypes ( ).clear ( );
            request.getServiceTypeIds ( ).forEach ( id -> {
                ServiceType serviceType = serviceTypeRepository.findById ( id )
                        .orElseThrow ( () -> new ResourceNotFoundException ( "service type not found" ) );
                garage.getServiceTypes ( ).add ( serviceType );
            } );
        }

        // Save garage first
        GarageV2 savedGarage = garageV2Repository.save ( garage );

        // Schedule async cleanup of old files
        if ( ! filesToCleanup.isEmpty ( ) ) {
            scheduleMediaFileCleanup ( filesToCleanup );
        }

        GarageV2Response response = garageV2Mapper.toResponse ( savedGarage );
        return new ResponseEntity<> ( response, HttpStatus.OK );
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void scheduleMediaFileCleanup(List<Long> mediaFileIds) {
        // Small delay to ensure transaction is committed
        try {
            Thread.sleep ( 3000 );

            for (Long fileId : mediaFileIds) {
                try {
                    // Check if file is still referenced elsewhere before deleting
                    if ( ! isMediaFileStillReferenced ( fileId ) ) {
                        mediaFileRepository.deleteById ( fileId );
                        log.info ( "Successfully cleaned up media file: {}", fileId );
                    } else {
                        log.info ( "Media file {} still referenced, skipping cleanup", fileId );
                    }
                }
                catch ( Exception e ) {
                    log.warn ( "Failed to cleanup media file {}: {}", fileId, e.getMessage ( ) );
                }
            }
        }
        catch ( InterruptedException e ) {
            Thread.currentThread ( ).interrupt ( );
            log.warn ( "Media file cleanup interrupted" );
        }
    }

    private boolean isMediaFileStillReferenced(Long mediaFileId) {
        // Check if this media file is still referenced by any garage
        // This is a safety check to prevent deleting files that might still be in use
        long count = garageV2Repository.countByGarageLogoId ( mediaFileId );
        return count > 0;
    }


    @Override
    public ResponseEntity<GarageV2Response> getGarage(Integer garageId) {
        GarageV2 garage = garageV2Repository.findById ( garageId ).orElseThrow ( () -> new ResourceNotFoundException ( "garage not found" ) );
        return new ResponseEntity<> ( garageV2Mapper.toResponse ( garage ), HttpStatus.OK );
    }

    @Override
    public ResponseEntity<List<GarageV2Response>> getAllGarage(String userId, String userRole) {

        List<GarageV2Response> response = garageV2Repository.findAll ( ).stream ( ).filter ( GarageV2::isActive ).map ( garageV2Mapper::toResponse ).toList ( );
        return new ResponseEntity<> ( response, HttpStatus.OK );
    }
}
