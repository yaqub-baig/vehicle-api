package com.autoadda.apis.vehicle.mapper;

import com.autoadda.apis.vehicle.clients.AuthClient;
import com.autoadda.apis.vehicle.dao.MediaFileRepository;
import com.autoadda.apis.vehicle.entity.Company;
import com.autoadda.apis.vehicle.enums.CompanyType;
import com.autoadda.apis.vehicle.exception.InvalidInputException;
import com.autoadda.apis.vehicle.request.CompanyRequest;
import com.autoadda.apis.vehicle.response.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class CompanyMapperTest {

    @Mock
    private AuthClient identityClient;
    @Mock
    private MediaFileRepository mediaFileRepository;
    @InjectMocks
    private CompanyMapper companyMapper;

    private CompanyRequest request;

    @BeforeEach
    void setUp() {
        request = new CompanyRequest();
        request.setCompanyOwnerId(1);
        request.setName("Test Co");
        lenient().when(identityClient.getUser(1)).thenReturn(UserResponse.builder().id(1).name("Owner").build());
    }

    @Test
    void toEntity_noTypes_defaultsToFleetOwner() {
        Company company = companyMapper.toEntity(request);
        assertEquals(Set.of(CompanyType.FLEET_OWNER), company.getTypes());
    }

    @Test
    void toEntity_parsesMultipleTypesCaseInsensitively() {
        request.setTypes(new HashSet<>(Set.of("used_car_dealer", "FLEET_OWNER")));
        Company company = companyMapper.toEntity(request);
        assertEquals(Set.of(CompanyType.USED_CAR_DEALER, CompanyType.FLEET_OWNER), company.getTypes());
    }

    @Test
    void toEntity_unknownType_rejected() {
        request.setTypes(new HashSet<>(Set.of("SCRAP_YARD")));
        assertThrows(InvalidInputException.class, () -> companyMapper.toEntity(request));
    }

    @Test
    void toEntity_blankBrandsFilteredOut() {
        request.setAuthorizedBrands(new HashSet<>(Set.of(" BMW ", "  ")));
        Company company = companyMapper.toEntity(request);
        assertEquals(Set.of("BMW"), company.getAuthorizedBrands());
    }

    @Test
    void updateEntity_typesAbsent_leavesExistingUntouched() {
        Company existing = new Company();
        existing.setTypes(new HashSet<>(Set.of(CompanyType.NEW_CAR_SHOWROOM)));
        existing.setAuthorizedBrands(new HashSet<>());

        companyMapper.updateEntity(existing, request);

        assertEquals(Set.of(CompanyType.NEW_CAR_SHOWROOM), existing.getTypes());
    }

    @Test
    void updateEntity_typesPresent_replacesSet() {
        Company existing = new Company();
        existing.setTypes(new HashSet<>(Set.of(CompanyType.NEW_CAR_SHOWROOM)));
        existing.setAuthorizedBrands(new HashSet<>());
        request.setTypes(new HashSet<>(Set.of("FLEET_OWNER")));

        companyMapper.updateEntity(existing, request);

        assertEquals(Set.of(CompanyType.FLEET_OWNER), existing.getTypes());
    }

    @Test
    void updateEntity_emptyTypeSet_rejected() {
        Company existing = new Company();
        existing.setTypes(new HashSet<>(Set.of(CompanyType.FLEET_OWNER)));
        existing.setAuthorizedBrands(new HashSet<>());
        request.setTypes(new HashSet<>());

        assertThrows(InvalidInputException.class, () -> companyMapper.updateEntity(existing, request));
    }

    @Test
    void toResponse_nullCollections_becomeEmptySets() {
        Company company = new Company();
        company.setCompanyOwnerId(1);
        company.setTypes(null);
        company.setAuthorizedBrands(null);

        var response = companyMapper.toResponse(company);

        assertTrue(response.getTypes().isEmpty());
        assertTrue(response.getAuthorizedBrands().isEmpty());
    }
}
