package com.autoadda.apis.vehicle.service.impl;

import com.autoadda.apis.vehicle.clients.AuthClient;
import com.autoadda.apis.vehicle.dao.CompanyRepository;
import com.autoadda.apis.vehicle.entity.Company;
import com.autoadda.apis.vehicle.exception.ResourceNotFoundException;
import com.autoadda.apis.vehicle.mapper.CompanyMapper;
import com.autoadda.apis.vehicle.request.CompanyRequest;
import com.autoadda.apis.vehicle.response.CompanyResponse;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.util.ERole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompanyService {
    private static final String SUCCESS = "success";
    private static final String COMPANY_NOT_FOUND = "Company not found";
    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final AuthClient identityClient;
    @Transactional
    public GenericResponse createCompany(CompanyRequest companyRequest) {
        validateTradeLicense ( companyRequest );
        Company company = companyMapper.toEntity ( companyRequest );
        Company company1 = companyRepository.save ( company );
        identityClient.updateRole ( company1.getCompanyOwnerId ( ), ERole.COMPANY_OWNER );
        CompanyResponse companyResponse = companyMapper.toResponse ( company1 );
        return GenericResponse.builder ( )
                .body ( companyResponse )
                .code ( "200" )
                .message ( SUCCESS )
                .build ( );
    }

    private void validateTradeLicense(CompanyRequest companyRequest) {
        companyRepository.findByTradeLicenseNumber ( companyRequest.getTradeLicenseNumber ( ) ).ifPresent ( company -> {
            throw new ResourceNotFoundException ( "Trade License Number already exists" );
        } );
    }


    public GenericResponse getAllCompanies() {
        List<Company> all = companyRepository.findAll ( );
        log.info ( " is active status from db {}", all.stream ( ).map ( Company::getIsActive ).toList ( ) );
        all = all.stream ( ).filter ( Company::getIsActive ).toList ( );
        List<CompanyResponse> list = all.stream ( )
                .map ( companyMapper::toResponse )
                .toList ( );

        return GenericResponse.builder ( )
                .body ( list )
                .code ( "200" )
                .message ( SUCCESS )
                .build ( );
    }

    public GenericResponse getCompanyById(Long id) {
        Company byId = companyRepository.findById ( id ).orElseThrow ( () -> new ResourceNotFoundException ( COMPANY_NOT_FOUND ) );
        if (Boolean.FALSE.equals( byId.getIsActive ( )) ) {
            return GenericResponse.builder ( ).code ( "200" ).status ( SUCCESS ).message ( "Company is not Active" ).build ( );
        }
        return GenericResponse.builder ( )
                .body ( companyMapper.toResponse ( byId ) )
                .code ( "200" )
                .message ( SUCCESS )
                .build ( );
    }

    public CompanyResponse getCompanyByIdV2(Long id) {
        Company byId = companyRepository.findById ( id ).orElseThrow ( () -> new ResourceNotFoundException ( COMPANY_NOT_FOUND ) );

        return  companyMapper.toResponse ( byId );
    }


    public GenericResponse updateCompany(Long id, CompanyRequest companyRequest) {
        Company companyFound = companyRepository.findById ( id ).orElseThrow ( () -> new ResourceNotFoundException ( COMPANY_NOT_FOUND ) );
        Company company = companyMapper.updateEntity ( companyFound, companyRequest );
        if ( companyRequest.getTradeLicenseNumber ( ) != null ) {
            validateTradeLicense ( companyRequest );
            company.setTradeLicenseNumber ( companyRequest.getTradeLicenseNumber ( ) );
        }
        CompanyResponse companyResponse = companyMapper.toResponse ( companyRepository.save ( company ) );
        return GenericResponse.builder ( )
                .body ( companyResponse )
                .code ( "200" )
                .message ( SUCCESS )
                .build ( );
    }

    public GenericResponse deleteCompany(Long id) {
        Company company = companyRepository.findById ( id ).orElseThrow ( () -> new ResourceNotFoundException ( COMPANY_NOT_FOUND ) );
        company.setIsActive ( false );
        companyRepository.save ( company );
        return GenericResponse.builder ( ).code ( "200" ).status ( SUCCESS ).message ( "Company deleted successfully" ).build ( );
    }

    public GenericResponse getCompanyByOwnerId(Integer id) {
        Company byId = companyRepository.findByCompanyOwnerId ( id ).orElseThrow ( () -> new ResourceNotFoundException ( COMPANY_NOT_FOUND ) );
        if (Boolean.FALSE.equals( byId.getIsActive ( )) ) {
            return GenericResponse.builder ( ).code ( "200" ).status ( SUCCESS ).message ( "Company is not Active" ).build ( );
        }
        return GenericResponse.builder ( )
                .body ( companyMapper.toResponse ( byId ) )
                .code ( "200" )
                .message ( SUCCESS )
                .build ( );


    }
}
