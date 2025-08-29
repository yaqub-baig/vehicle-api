package com.autoadda.apis.vehicle.service;

import com.autoadda.apis.vehicle.dao.VehicleDetailsRepository;
import com.autoadda.apis.vehicle.dao.VehicleListingRepository;
import com.autoadda.apis.vehicle.entity.VehicleDetails;
import com.autoadda.apis.vehicle.entity.VehicleListing;
import com.autoadda.apis.vehicle.exception.ResourceNotFoundException;
import com.autoadda.apis.vehicle.mapper.VehicleListingMapper;
import com.autoadda.apis.vehicle.request.VehicleListingRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.response.VehicleListingResponse;
import com.autoadda.apis.vehicle.util.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleListingServiceTest {

    private final Integer vehicleId = 1;
    private final Integer listingId = 1;
    private final Integer userId = 1;
    private final String city = "Bhopal";

    @Mock
    private VehicleListingRepository vehicleListingRepository;
    @Mock
    private VehicleDetailsRepository vehicleDetailsRepository;
    @Mock
    private VehicleListingMapper vehicleListingMapper;
    @InjectMocks
    private VehicleListingService vehicleListingService;

    private VehicleListingRequest vehicleListingRequest;
    private VehicleListing vehicleListing;
    private VehicleDetails vehicleDetails;
    private VehicleListingResponse vehicleListingResponse;
    private VehicleListingRequest updateRequest;

    @BeforeEach
    void setUp() {
        vehicleListingRequest = new VehicleListingRequest();
        vehicleListingRequest.setVehicleId(vehicleId);
        vehicleListingRequest.setPrice(10000.0);
        vehicleListingRequest.setDescription("Test description");
        vehicleListingRequest.setCity("Bhopal");

        updateRequest = new VehicleListingRequest();
        updateRequest.setPrice(12000.0);
        updateRequest.setDescription("Updated description");
        updateRequest.setCity("Indore");

        vehicleDetails = new VehicleDetails();
        vehicleDetails.setId(vehicleId);
        vehicleDetails.setCustomerId(userId);

        vehicleListing = new VehicleListing();
        vehicleListing.setId(listingId);
        vehicleListing.setVehicle(vehicleDetails);
        vehicleListing.setPrice(10000.0);
        vehicleListing.setDescription("Test description");
        vehicleListing.setCity("Bhopal");
        vehicleListing.setStatus(Status.LISTED);
        vehicleListing.setCreatedAt(LocalDateTime.now());

        vehicleListingResponse = new VehicleListingResponse();
        vehicleListingResponse.setId(listingId);
        vehicleListingResponse.setPrice(10000.0);
        vehicleListingResponse.setDescription("Test description");
        vehicleListingResponse.setCity("Bhopal");
        vehicleListingResponse.setStatus(Status.LISTED);
    }

    @Test
    void createVehicleListing_WithValidRequest_ShouldCreateListingSuccessfully() {
        // Arrange
        when(vehicleDetailsRepository.findById(vehicleId)).thenReturn(Optional.of(vehicleDetails));
        when(vehicleListingMapper.requestToEntity(vehicleListingRequest)).thenReturn(vehicleListing);
        when(vehicleListingRepository.save(any(VehicleListing.class))).thenReturn(vehicleListing);
        when(vehicleListingMapper.entityToResponse(vehicleListing)).thenReturn(vehicleListingResponse);

        // Act
        GenericResponse response = vehicleListingService.createVehicleListing(vehicleListingRequest);

        // Assert
        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertEquals("200", response.getCode());
        assertEquals("Vehicle listed successfully", response.getMessage());
        assertNotNull(response.getBody());
        assertEquals(vehicleListingResponse, response.getBody());

        verify(vehicleDetailsRepository, times(1)).findById(vehicleId);
        verify(vehicleListingMapper, times(1)).requestToEntity(vehicleListingRequest);
        verify(vehicleListingRepository, times(1)).save(vehicleListing);
        verify(vehicleListingMapper, times(1)).entityToResponse(vehicleListing);
    }

    @Test
    void createVehicleListing_WithInvalidVehicleId_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(vehicleDetailsRepository.findById(vehicleId)).thenReturn(Optional.empty());
        when(vehicleListingMapper.requestToEntity(vehicleListingRequest)).thenReturn(vehicleListing);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> vehicleListingService.createVehicleListing(vehicleListingRequest));

        assertEquals("invalid vehicle id", exception.getMessage());

        verify(vehicleDetailsRepository, times(1)).findById(vehicleId);
        verify(vehicleListingMapper, times(1)).requestToEntity(vehicleListingRequest);
        verify(vehicleListingRepository, never()).save(any());
        verify(vehicleListingMapper, never()).entityToResponse(any());
    }

    @Test
    void getVehicleListings_WithLocation_ShouldReturnFilteredListings() {
        // Arrange
        List<VehicleListing> vehicleListings = Arrays.asList(vehicleListing);
        when(vehicleListingRepository.findByCity(city)).thenReturn(vehicleListings);
        when(vehicleListingMapper.entityToResponse(any(VehicleListing.class))).thenReturn(vehicleListingResponse);

        // Act
        GenericResponse response = vehicleListingService.getVehicleListings(city);

        // Assert
        assertNotNull(response);
        assertEquals("200", response.getCode());
        assertEquals("success", response.getMessage());
        assertNotNull(response.getBody());

        List<?> bodyList = (List<?>) response.getBody();
        assertEquals(1, bodyList.size());

        verify(vehicleListingRepository, times(1)).findByCity(city);
        verify(vehicleListingRepository, never()).findAll();
        verify(vehicleListingMapper, times(1)).entityToResponse(any(VehicleListing.class));
    }

    @Test
    void getVehicleListings_WithoutLocation_ShouldReturnAllListingsSorted() {
        // Arrange
        VehicleListing olderListing = new VehicleListing();
        olderListing.setId(2);
        olderListing.setCreatedAt(LocalDateTime.now().minusDays(1));

        List<VehicleListing> vehicleListings = Arrays.asList(olderListing, vehicleListing);
        when(vehicleListingRepository.findAll()).thenReturn(vehicleListings);
        when(vehicleListingMapper.entityToResponse(any(VehicleListing.class))).thenReturn(vehicleListingResponse);

        // Act
        GenericResponse response = vehicleListingService.getVehicleListings(null);

        // Assert
        assertNotNull(response);
        assertEquals("200", response.getCode());
        assertEquals("success", response.getMessage());
        assertNotNull(response.getBody());

        List<?> bodyList = (List<?>) response.getBody();
        assertEquals(2, bodyList.size());

        verify(vehicleListingRepository, times(1)).findAll();
        verify(vehicleListingRepository, never()).findByCity(any());
        verify(vehicleListingMapper, times(2)).entityToResponse(any(VehicleListing.class));
    }

    @Test
    void getVehicleListings_WhenNoListingsExist_ShouldReturnEmptyList() {
        // Arrange
        when(vehicleListingRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        GenericResponse response = vehicleListingService.getVehicleListings(null);

        // Assert
        assertNotNull(response);
        assertEquals("200", response.getCode());
        assertEquals("success", response.getMessage());
        assertNotNull(response.getBody());

        List<?> bodyList = (List<?>) response.getBody();
        assertTrue(bodyList.isEmpty());

        verify(vehicleListingRepository, times(1)).findAll();
        verify(vehicleListingMapper, never()).entityToResponse(any(VehicleListing.class));
    }

    @Test
    void getVehicleListingById_WithValidId_ShouldReturnListing() {
        // Arrange
        when(vehicleListingRepository.findById(listingId)).thenReturn(Optional.of(vehicleListing));
        when(vehicleListingMapper.entityToResponse(vehicleListing)).thenReturn(vehicleListingResponse);

        // Act
        GenericResponse response = vehicleListingService.getVehicleListingById(listingId);

        // Assert
        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertEquals("200", response.getCode());
        assertEquals("vehicle retrieved successfully", response.getMessage());
        assertNotNull(response.getBody());
        assertEquals(vehicleListingResponse, response.getBody());

        verify(vehicleListingRepository, times(1)).findById(listingId);
        verify(vehicleListingMapper, times(1)).entityToResponse(vehicleListing);
    }

    @Test
    void getVehicleListingById_WithInvalidId_ShouldThrowResourceNotFoundException() {
        // Arrange
        Integer invalidId = 999;
        when(vehicleListingRepository.findById(invalidId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> vehicleListingService.getVehicleListingById(invalidId));

        assertEquals("invalid vehicle listing id", exception.getMessage());

        verify(vehicleListingRepository, times(1)).findById(invalidId);
        verify(vehicleListingMapper, never()).entityToResponse(any());
    }

    @Test
    void getVehicleListingsByUserId_WithValidUserId_ShouldReturnUserListings() {
        // Arrange
        List<VehicleListing> userListings = Arrays.asList(vehicleListing);
        when(vehicleListingRepository.findByVehicleCustomerId(userId)).thenReturn(userListings);
        when(vehicleListingMapper.entityToResponse(any(VehicleListing.class))).thenReturn(vehicleListingResponse);

        // Act
        GenericResponse response = vehicleListingService.getVehicleListingsByUserId(userId);

        // Assert
        assertNotNull(response);
        assertEquals("200", response.getCode());
        assertEquals("success", response.getMessage());
        assertNotNull(response.getBody());

        List<?> bodyList = (List<?>) response.getBody();
        assertEquals(1, bodyList.size());

        verify(vehicleListingRepository, times(1)).findByVehicleCustomerId(userId);
        verify(vehicleListingMapper, times(1)).entityToResponse(any(VehicleListing.class));
    }

    @Test
    void getVehicleListingsByUserId_WithNoListings_ShouldReturnEmptyList() {
        // Arrange
        when(vehicleListingRepository.findByVehicleCustomerId(userId)).thenReturn(Collections.emptyList());

        // Act
        GenericResponse response = vehicleListingService.getVehicleListingsByUserId(userId);

        // Assert
        assertNotNull(response);
        assertEquals("200", response.getCode());
        assertEquals("success", response.getMessage());
        assertNotNull(response.getBody());

        List<?> bodyList = (List<?>) response.getBody();
        assertTrue(bodyList.isEmpty());

        verify(vehicleListingRepository, times(1)).findByVehicleCustomerId(userId);
        verify(vehicleListingMapper, never()).entityToResponse(any(VehicleListing.class));
    }

    @Test
    void updateVehicleListing_WithValidId_ShouldUpdateListing() {
        // Arrange
        when(vehicleListingRepository.findById(listingId)).thenReturn(Optional.of(vehicleListing));
        when(vehicleListingRepository.save(any(VehicleListing.class))).thenReturn(vehicleListing);
        when(vehicleListingMapper.entityToResponse(vehicleListing)).thenReturn(vehicleListingResponse);

        // Act
        GenericResponse response = vehicleListingService.updateVehicleListing(listingId, updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals("200", response.getCode());
        assertEquals("success", response.getMessage());
        assertNotNull(response.getBody());

        verify(vehicleListingRepository, times(1)).findById(listingId);
        verify(vehicleListingRepository, times(1)).save(vehicleListing);
        verify(vehicleListingMapper, times(1)).entityToResponse(vehicleListing);

        // Verify that fields were updated
        assertEquals(updateRequest.getPrice(), vehicleListing.getPrice());
        assertEquals(updateRequest.getDescription(), vehicleListing.getDescription());
        assertEquals(updateRequest.getCity(), vehicleListing.getCity());
    }

    @Test
    void updateVehicleListing_WithPartialUpdate_ShouldUpdateOnlyProvidedFields() {
        // Arrange
        VehicleListingRequest partialUpdate = new VehicleListingRequest();
        partialUpdate.setPrice(15000.0); // Only update price

        VehicleListing originalListing = new VehicleListing();
        originalListing.setId(listingId);
        originalListing.setPrice(10000.0);
        originalListing.setDescription("Original description");
        originalListing.setCity("Bhopal");

        when(vehicleListingRepository.findById(listingId)).thenReturn(Optional.of(originalListing));
        when(vehicleListingRepository.save(any(VehicleListing.class))).thenReturn(originalListing);
        when(vehicleListingMapper.entityToResponse(originalListing)).thenReturn(vehicleListingResponse);

        // Act
        GenericResponse response = vehicleListingService.updateVehicleListing(listingId, partialUpdate);

        // Assert
        assertNotNull(response);
        assertEquals("200", response.getCode());
        assertEquals("success", response.getMessage());

        // Verify only price was updated, other fields remain unchanged
        assertEquals(15000.0, originalListing.getPrice());
        assertEquals("Original description", originalListing.getDescription());
        assertEquals("Bhopal", originalListing.getCity());
    }

    @Test
    void updateVehicleListing_WithInvalidId_ShouldThrowResourceNotFoundException() {
        // Arrange
        Integer invalidId = 999;
        when(vehicleListingRepository.findById(invalidId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> vehicleListingService.updateVehicleListing(invalidId, updateRequest));

        assertEquals("invalid vehicle listing id", exception.getMessage());

        verify(vehicleListingRepository, times(1)).findById(invalidId);
        verify(vehicleListingRepository, never()).save(any());
        verify(vehicleListingMapper, never()).entityToResponse(any());
    }

    @Test
    void createVehicleListing_ShouldSetVehicleCorrectly() {
        // Arrange
        // Create a fresh vehicle listing without vehicle set
        VehicleListing newVehicleListing = new VehicleListing();
        newVehicleListing.setPrice(10000.0);
        newVehicleListing.setDescription("Test description");
        newVehicleListing.setCity("Bhopal");

        when(vehicleDetailsRepository.findById(vehicleId)).thenReturn(Optional.of(vehicleDetails));
        when(vehicleListingMapper.requestToEntity(vehicleListingRequest)).thenReturn(newVehicleListing);
        when(vehicleListingRepository.save(any(VehicleListing.class))).thenReturn(newVehicleListing);
        when(vehicleListingMapper.entityToResponse(newVehicleListing)).thenReturn(vehicleListingResponse);

        // Act
        GenericResponse response = vehicleListingService.createVehicleListing(vehicleListingRequest);

        // Assert
        assertNotNull(response);
        // Verify that the vehicle was set correctly in the entity
        assertEquals(vehicleDetails, newVehicleListing.getVehicle());
        verify(vehicleDetailsRepository, times(1)).findById(vehicleId);
    }
}