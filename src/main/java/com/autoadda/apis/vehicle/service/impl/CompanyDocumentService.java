package com.autoadda.apis.vehicle.service.impl;

import com.autoadda.apis.vehicle.dao.CompanyDocumentRepository;
import com.autoadda.apis.vehicle.dao.CompanyRepository;
import com.autoadda.apis.vehicle.dao.MediaFileRepository;
import com.autoadda.apis.vehicle.entity.CompanyDocument;
import com.autoadda.apis.vehicle.entity.MediaFile;
import com.autoadda.apis.vehicle.exception.ResourceNotFoundException;
import com.autoadda.apis.vehicle.request.CompanyDocumentRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
public class CompanyDocumentService {
    private final CompanyDocumentRepository companyDocumentRepository;
    private final CompanyRepository companyRepository;
    private final MediaFileRepository mediaFileRepository;

    public GenericResponse uploadCompanyDocument(CompanyDocumentRequest companyDocumentRequest) {
        CompanyDocument companyDocument = new CompanyDocument ( );
        companyDocument.setCompany ( companyRepository.findById ( companyDocumentRequest.getCompanyId ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( format ( "company not found with this id  {}", companyDocumentRequest.getCompanyId ( ) ) ) ) );
        if ( companyDocumentRequest.getTradeLicenseImageId ( ) != null ) {
            MediaFile tradeLicenceId = mediaFileRepository.findById ( companyDocumentRequest.getTradeLicenseImageId ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "trade licence image not found" ) );
            companyDocument.setTradeLicence ( tradeLicenceId );

        }
        if ( companyDocumentRequest.getEmiratesIdImageId ( ) != null ) {
            MediaFile emirateId = mediaFileRepository.findById ( companyDocumentRequest.getEmiratesIdImageId ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "driving licence image not found" ) );
            companyDocument.setEmiratesId ( emirateId );
        }
        CompanyDocument save = companyDocumentRepository.save ( companyDocument );
        return GenericResponse.builder ( )
                .status ( "success" )
                .code ( "200" )
                .message ( "Vehicle owner document uploaded successfully" )
                .body ( save )
                .build ( );
    }

    public GenericResponse getCompanyDocument(Long companyId) {
        CompanyDocument companyDocument = companyDocumentRepository.findByCompanyId ( companyId ).orElseThrow ( () -> new ResourceNotFoundException ( "Company document not found" ) );
        return GenericResponse.builder ( )
                .status ( "success" )
                .code ( "200" )
                .message ( "Company document fetched successfully" )
                .body ( companyDocument )
                .build ( );
    }

    public GenericResponse updateCompanyDocument(Long companyId, CompanyDocumentRequest companyDocumentRequest) {
        CompanyDocument companyDocument = companyDocumentRepository.findByCompanyId ( companyId ).orElseThrow ( () -> new ResourceNotFoundException ( "Company document not found" ) );
        companyDocument.setCompany ( companyRepository.findById ( companyDocumentRequest.getCompanyId ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( format ( "company not found with this id  {}", companyDocumentRequest.getCompanyId ( ) ) ) ) );
        if ( companyDocumentRequest.getTradeLicenseImageId ( ) != null ) {
            MediaFile tradeLicenceId = mediaFileRepository.findById ( companyDocumentRequest.getTradeLicenseImageId ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "trade licence image not found" ) );
            companyDocument.setTradeLicence ( tradeLicenceId );
        }
        if ( companyDocumentRequest.getEmiratesIdImageId ( ) != null ) {
            MediaFile emirateId = mediaFileRepository.findById ( companyDocumentRequest.getEmiratesIdImageId ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "driving licence image not found" ) );
            companyDocument.setEmiratesId ( emirateId );
        }
        CompanyDocument save = companyDocumentRepository.save ( companyDocument );
        return GenericResponse.builder ( )
                .status ( "success" )
                .code ( "200" )
                .message ( "Vehicle owner document updated successfully" )
                .body ( save )
                .build ( );
    }
}
