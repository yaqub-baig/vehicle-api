package com.platform.profileservice.service.impl;

import com.platform.profileservice.dao.*;
import com.platform.profileservice.entity.*;
import com.platform.profileservice.exception.ResourceNotFoundException;

import com.platform.profileservice.mapper.GarageV2Mapper;
import com.platform.profileservice.request.GarageAddressRequest;
import com.platform.profileservice.request.GarageV2Request;
import com.platform.profileservice.response.GarageV2Response;
import com.platform.profileservice.service.GarageAddressService;
import com.platform.profileservice.util.ERole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GarageServiceImplTest {

    @Mock
    private GarageV2Repository garageV2Repository;
    @Mock
    private MediaFileRepository mediaFileRepository;
    @Mock
    private ServiceTypeRepository serviceTypeRepository;
    @Mock
    private GarageAddressService garageAddressService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private GarageV2Mapper garageV2Mapper;

    @InjectMocks
    private GarageServiceImpl garageService;

    private final String TEST_USER_ID = "+971501234567";
    private final String TEST_USER_ROLE = "GARAGE_OWNER";
    private final Integer TEST_GARAGE_ID = 1;

    // ================ CREATE GARAGE TEST CASES ================

    @Test
    void createGarage_withValidRequestAndAttachments_shouldReturnCreatedResponse() {
        // Arrange
        GarageV2Request request = createValidGarageRequest();
        GarageV2 garageEntity = createGarageV2Entity();
        User owner = createTestUser();
        Role garageOwnerRole = new Role(3L, ERole.GARAGE_OWNER);

        when(mediaFileRepository.findById(anyLong()))
                .thenReturn( Optional.of(new MediaFile ()));
        when(serviceTypeRepository.findById(anyInt()))
                .thenReturn(Optional.of(new ServiceType ()));
        when(garageAddressService.createGarageAddress(any()))
                .thenReturn(new GarageAddress());
        when(userRepository.findByMobileNumber(TEST_USER_ID))
                .thenReturn(Optional.of(owner));
        when(roleRepository.findByName(ERole.GARAGE_OWNER))
                .thenReturn(Optional.of(garageOwnerRole));
        when(garageV2Repository.save(any()))
                .thenReturn(garageEntity);
        when ( garageV2Mapper.toResponse (any()))
                .thenReturn(new GarageV2Response());

        // Act
        ResponseEntity<GarageV2Response> response =
                garageService.createGarage(request, TEST_USER_ID, TEST_USER_ROLE);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo( HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(TEST_GARAGE_ID);

        verify(mediaFileRepository, times(5)).findById(anyLong());
        verify(garageV2Repository).save(any());
    }

    @Test
    void createGarage_withMissingMediaFile_shouldThrowResourceNotFoundException() {
        // Arrange
        GarageV2Request request = createValidGarageRequest();
        when(mediaFileRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() ->
                garageService.createGarage(request, TEST_USER_ID, TEST_USER_ROLE))
                .isInstanceOf( ResourceNotFoundException.class)
                .hasMessageContaining("garage logo not found");
    }

    // ================ GET GARAGE TEST CASES ================

    @Test
    void getGarage_withValidOwner_shouldReturnActiveGarages() {
        // Arrange
        User owner = createTestUser();
        List<GarageV2> garages = List.of(
                createActiveGarage(1),
                createInactiveGarage(2)
        );

        when(userRepository.findByMobileNumber(TEST_USER_ID))
                .thenReturn(Optional.of(owner));
        when(garageV2Repository.findByOwnerId(owner.getId()))
                .thenReturn(garages);

        // Act
        ResponseEntity<List<GarageV2Response>> response =
                garageService.getGarage(TEST_USER_ID, TEST_USER_ROLE);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(userRepository).findByMobileNumber(TEST_USER_ID);
    }

    @Test
    void getGarage_withInvalidUser_shouldThrowResourceNotFoundException() {
        // Arrange
        when(userRepository.findByMobileNumber(TEST_USER_ID))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() ->
                garageService.getGarage(TEST_USER_ID, TEST_USER_ROLE))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("user not found");
    }

    // ================ DELETE GARAGE TEST CASES ================

    @Test
    @Transactional
    void deleteGarage_withValidOwner_shouldSoftDelete() {
        // Arrange
        User owner = createTestUser();
        GarageV2 garage = createActiveGarage(TEST_GARAGE_ID);
        garage.setOwner(owner);

        when(userRepository.findByMobileNumber(TEST_USER_ID))
                .thenReturn(Optional.of(owner));
        when(garageV2Repository.findByOwnerId(owner.getId()))
                .thenReturn(List.of(garage));
        when(garageV2Repository.save(any()))
                .thenReturn(garage);

        // Act
        ResponseEntity<String> response =
                garageService.deleteGarage(TEST_USER_ID, TEST_USER_ROLE, TEST_GARAGE_ID);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(garage.isActive()).isFalse();
        verify(garageV2Repository).save(garage);
    }

    @Test
    void deleteGarage_withNonOwner_shouldThrowException() {
        // Arrange
        User owner = createTestUser();
        GarageV2 garage = createActiveGarage(TEST_GARAGE_ID);
        garage.setOwner(new User()); // Different owner

        when(userRepository.findByMobileNumber(TEST_USER_ID))
                .thenReturn(Optional.of(owner));
        when(garageV2Repository.findByOwnerId(owner.getId()))
                .thenReturn(List.of(createActiveGarage(999))); // Different garage

        // Act & Assert
        assertThatThrownBy(() ->
                garageService.deleteGarage(TEST_USER_ID, TEST_USER_ROLE, TEST_GARAGE_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("garage not found");
    }

    // ================ UPDATE GARAGE TEST CASES ================

    @Test
    @Transactional
    void updateGarage_withValidChanges_shouldUpdateAndCleanupOldFiles() {
        // Arrange
        GarageV2 existingGarage = createActiveGarage(TEST_GARAGE_ID);
        existingGarage.setOwner(createTestUser());
        existingGarage.setGarageLogo(new MediaFile()); // Old logo

        GarageV2Request updateRequest = createValidGarageRequest();
        updateRequest.setGarageLogo(2L); // New logo

        when(garageV2Repository.findById(TEST_GARAGE_ID))
                .thenReturn(Optional.of(existingGarage));
        when(mediaFileRepository.findById(2L))
                .thenReturn(Optional.of(new MediaFile()));
        when(garageV2Repository.save(any()))
                .thenReturn(existingGarage);

        // Act
        ResponseEntity<GarageV2Response> response =
                garageService.updateGarage(TEST_USER_ID, TEST_USER_ROLE, TEST_GARAGE_ID, updateRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(existingGarage.getGarageLogo().getId()).isEqualTo(2L);
        verify(garageV2Repository).save(existingGarage);
    }

    @Test
    void updateGarage_withInvalidGarageId_shouldThrowException() {
        // Arrange
        when(garageV2Repository.findById(TEST_GARAGE_ID))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() ->
                garageService.updateGarage(TEST_USER_ID, TEST_USER_ROLE, TEST_GARAGE_ID, new GarageV2Request()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("garage not found");
    }

    // ================ HELPER METHODS ================

    private GarageV2Request createValidGarageRequest() {
        return GarageV2Request.builder()
                .garageLogo(1L)
                .tradeLicenseAttachmentIds(List.of(2L, 3L))
                .vatCertificateIds(List.of(4L))
                .garageImageIds(List.of(5L, 6L))
                .serviceTypeIds(List.of(1, 2))
                .garageAddress(new GarageAddressRequest ())
                .build();
    }

    private GarageV2 createGarageV2Entity() {
        GarageV2 garage = new GarageV2();
        garage.setId(TEST_GARAGE_ID);
        garage.setGarageLogo ( new MediaFile () );

        return garage;
    }

    private User createTestUser() {
        User user = new User();
        user.setId(1);
        user.setMobileNumber(TEST_USER_ID);
        return user;
    }

    private GarageV2 createActiveGarage(Integer id) {
        GarageV2 garage = new GarageV2();
        garage.setId(id);
        garage.setActive(true);
        return garage;
    }

    private GarageV2 createInactiveGarage(Integer id) {
        GarageV2 garage = new GarageV2();
        garage.setId(id);
        garage.setActive(false);
        return garage;
    }
}

