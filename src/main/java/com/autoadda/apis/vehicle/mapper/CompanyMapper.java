package com.autoadda.apis.vehicle.mapper;

import com.autoadda.apis.vehicle.clients.AuthClient;
import com.autoadda.apis.vehicle.dao.MediaFileRepository;
import com.autoadda.apis.vehicle.entity.Company;
import com.autoadda.apis.vehicle.entity.MediaFile;
import com.autoadda.apis.vehicle.enums.CompanyType;
import com.autoadda.apis.vehicle.exception.InvalidInputException;
import com.autoadda.apis.vehicle.exception.ResourceNotFoundException;
import com.autoadda.apis.vehicle.request.CompanyRequest;
import com.autoadda.apis.vehicle.response.CompanyResponse;
import com.autoadda.apis.vehicle.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class CompanyMapper {
    private final AuthClient identityClient;
    private final MediaFileRepository mediaFileRepository;

    public Company toEntity(CompanyRequest companyRequest) {
        Company company = new Company ( );
        UserResponse user = identityClient.getUser ( companyRequest.getCompanyOwnerId ( ) );
        if(user!=null){
            company.setCompanyOwnerId ( user.getId ( ) );
        }
        else{
            throw new ResourceNotFoundException ( "User not found" );
        }
        company.setName ( companyRequest.getName ( ) );
        company.setTradeLicenseExpiry ( companyRequest.getTradeLicenseExpiry ( ) );
        company.setTradeLicenseNumber ( companyRequest.getTradeLicenseNumber ( ) );

        if ( companyRequest.getProfileImageId ( ) != null ) {
            MediaFile profileImage = mediaFileRepository.findById(companyRequest.getProfileImageId()).orElseThrow(() -> new ResourceNotFoundException("Profile image not found"));
            company.setProfileImage(profileImage);
        }
        company.setIsActive ( true );

        company.setTypes ( parseTypes ( companyRequest.getTypes ( ) ) );
        company.setShowroomAddress ( companyRequest.getShowroomAddress ( ) );
        company.setRtaApprovalNumber ( companyRequest.getRtaApprovalNumber ( ) );
        company.setAuthorizedBrands ( cleanBrands ( companyRequest.getAuthorizedBrands ( ) ) );

        return company;
    }

    public CompanyResponse toResponse(Company company) {
        CompanyResponse companyResponse = new CompanyResponse ( );
        companyResponse.setId ( company.getId ( ) );
        companyResponse.setName ( company.getName ( ) );
        UserResponse user = identityClient.getUser ( company.getCompanyOwnerId ( ) );
        companyResponse.setCompanyOwner ( user );
        companyResponse.setTradeLicenseExpiry ( company.getTradeLicenseExpiry ()!=null? company.getTradeLicenseExpiry ():null );
        companyResponse.setTradeLicenseNumber ( company.getTradeLicenseNumber ()!=null?company.getTradeLicenseNumber ():null );
        companyResponse.setProfileImage(company.getProfileImage() != null? company.getProfileImage():null);
        companyResponse.setTypes ( company.getTypes ( ) != null ? company.getTypes ( ) : new HashSet<> ( ) );
        companyResponse.setShowroomAddress ( company.getShowroomAddress ( ) );
        companyResponse.setRtaApprovalNumber ( company.getRtaApprovalNumber ( ) );
        companyResponse.setAuthorizedBrands ( company.getAuthorizedBrands ( ) != null ? company.getAuthorizedBrands ( ) : new HashSet<> ( ) );
        return companyResponse;
    }

    public Company updateEntity(Company companyFound, CompanyRequest companyRequest) {
        companyFound.setName (companyRequest.getName ()!=null? companyRequest.getName ( ):companyFound.getName () );

        if(companyRequest.getTradeLicenseExpiry ()!=null){
            companyFound.setTradeLicenseExpiry ( companyRequest.getTradeLicenseExpiry ( ) );
        }
        if ( companyRequest.getProfileImageId ( ) != null ) {
            MediaFile profileImage = mediaFileRepository.findById(companyRequest.getProfileImageId()).orElseThrow(() -> new ResourceNotFoundException("Profile image not found"));
            companyFound.setProfileImage(profileImage);
        }
        if ( companyRequest.getTypes ( ) != null ) {
            Set<CompanyType> parsed = parseTypes ( companyRequest.getTypes ( ) );
            if ( companyRequest.getTypes ( ).isEmpty ( ) ) {
                throw new InvalidInputException ( "A company must have at least one type" );
            }
            companyFound.getTypes ( ).clear ( );
            companyFound.getTypes ( ).addAll ( parsed );
        }
        if ( companyRequest.getAuthorizedBrands ( ) != null ) {
            companyFound.getAuthorizedBrands ( ).clear ( );
            companyFound.getAuthorizedBrands ( ).addAll ( cleanBrands ( companyRequest.getAuthorizedBrands ( ) ) );
        }
        if ( companyRequest.getShowroomAddress ( ) != null ) {
            companyFound.setShowroomAddress ( companyRequest.getShowroomAddress ( ) );
        }
        if ( companyRequest.getRtaApprovalNumber ( ) != null ) {
            companyFound.setRtaApprovalNumber ( companyRequest.getRtaApprovalNumber ( ) );
        }
        return  companyFound ;
    }

    /**
     * Null/empty -> the default single type FLEET_OWNER. Otherwise every entry is parsed to a
     * {@link CompanyType}, rejecting unknown values.
     */
    private Set<CompanyType> parseTypes(Set<String> raw) {
        if ( raw == null || raw.isEmpty ( ) ) {
            Set<CompanyType> defaults = new HashSet<> ( );
            defaults.add ( CompanyType.FLEET_OWNER );
            return defaults;
        }
        Set<CompanyType> parsed = new HashSet<> ( );
        for ( String value : raw ) {
            if ( value == null || value.isBlank ( ) ) {
                continue;
            }
            try {
                parsed.add ( CompanyType.valueOf ( value.trim ( ).toUpperCase ( ) ) );
            } catch ( IllegalArgumentException e ) {
                throw new InvalidInputException ( "Invalid company type: " + value );
            }
        }
        if ( parsed.isEmpty ( ) ) {
            throw new InvalidInputException ( "A company must have at least one type" );
        }
        return parsed;
    }

    private Set<String> cleanBrands(Set<String> raw) {
        if ( raw == null ) {
            return new HashSet<> ( );
        }
        return raw.stream ( )
                .filter ( b -> b != null && !b.isBlank ( ) )
                .map ( String::trim )
                .collect ( Collectors.toCollection ( HashSet::new ) );
    }
}
