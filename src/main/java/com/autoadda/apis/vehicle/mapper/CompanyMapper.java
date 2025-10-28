package com.autoadda.apis.vehicle.mapper;

import com.autoadda.apis.vehicle.clients.IdentityClient;
import com.autoadda.apis.vehicle.dao.MediaFileRepository;
import com.autoadda.apis.vehicle.entity.Company;
import com.autoadda.apis.vehicle.entity.MediaFile;
import com.autoadda.apis.vehicle.exception.ResourceNotFoundException;
import com.autoadda.apis.vehicle.request.CompanyRequest;
import com.autoadda.apis.vehicle.response.CompanyResponse;
import com.autoadda.apis.vehicle.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CompanyMapper {
    private final IdentityClient identityClient;
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
        return  companyFound ;
    }
}
