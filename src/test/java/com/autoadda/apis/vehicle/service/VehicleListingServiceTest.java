package com.autoadda.apis.vehicle.service;

import com.autoadda.apis.vehicle.dao.VehicleDetailsRepository;
import com.autoadda.apis.vehicle.dao.VehicleListingRepository;
import com.autoadda.apis.vehicle.entity.VehicleDetails;
import com.autoadda.apis.vehicle.entity.VehicleListing;
import com.autoadda.apis.vehicle.exception.ResourceNotFoundException;
import com.autoadda.apis.vehicle.mapper.VehicleListingMapper;
import com.autoadda.apis.vehicle.request.VehicleListingRequest;
import com.autoadda.apis.vehicle.request.VehicleSearchCriteria;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.response.VehicleListingResponse;
import com.autoadda.apis.vehicle.service.impl.VehicleListingService;
import com.autoadda.apis.vehicle.util.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

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
    private VehicleSearchCriteria searchCriteria;

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

        searchCriteria = new VehicleSearchCriteria();
        searchCriteria.setLocation ("Bhopal");
        searchCriteria.setFromPrice(5000.0);
        searchCriteria.setToPrice(15000.0);

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
    void getVehicleListings_WithCriteria_ShouldReturnFilteredListings() {
        // Arrange
        List<VehicleListing> vehicleListings = Arrays.asList(vehicleListing);
        when(vehicleListingRepository.findByCriteria(searchCriteria)).thenReturn(vehicleListings);
        when(vehicleListingMapper.entityToResponse(any(VehicleListing.class))).thenReturn(vehicleListingResponse);

        // Act
        GenericResponse response = vehicleListingService.getVehicleListings(searchCriteria);

        // Assert
        assertNotNull(response);
        assertEquals("200", response.getCode());
        assertEquals("success", response.getMessage());
        assertNotNull(response.getBody());

        List<?> bodyList = (List<?>) response.getBody();
        assertEquals(1, bodyList.size());

        verify(vehicleListingRepository, times(1)).findByCriteria(searchCriteria);
        verify(vehicleListingMapper, times(1)).entityToResponse(any(VehicleListing.class));
    }

    @Test
    void getVehicleListings_WithoutCriteria_ShouldReturnAllListingsSorted() {
        // Arrange
        VehicleSearchCriteria emptyCriteria = new VehicleSearchCriteria();

        VehicleListing olderListing = new VehicleListing();
        olderListing.setId(2);
        olderListing.setCreatedAt(LocalDateTime.now().minusDays(1));

        List<VehicleListing> vehicleListings = Arrays.asList(olderListing, vehicleListing);
        when(vehicleListingRepository.findAllByOrderByCreatedAtDesc()).thenReturn(vehicleListings);
        when(vehicleListingMapper.entityToResponse(any(VehicleListing.class))).thenReturn(vehicleListingResponse);

        // Act
        GenericResponse response = vehicleListingService.getVehicleListings(emptyCriteria);

        // Assert
        assertNotNull(response);
        assertEquals("200", response.getCode());
        assertEquals("success", response.getMessage());
        assertNotNull(response.getBody());

        List<?> bodyList = (List<?>) response.getBody();
        assertEquals(2, bodyList.size());

        verify(vehicleListingRepository, times(1)).findAllByOrderByCreatedAtDesc();
        verify(vehicleListingMapper, times(2)).entityToResponse(any(VehicleListing.class));
    }

    @Test
    void getVehicleListings_WhenNoListingsExist_ShouldReturnEmptyList() {
        // Arrange
        VehicleSearchCriteria emptyCriteria = new VehicleSearchCriteria();
        when(vehicleListingRepository.findAllByOrderByCreatedAtDesc()).thenReturn(Collections.emptyList());

        // Act
        GenericResponse response = vehicleListingService.getVehicleListings(emptyCriteria);

        // Assert
        assertNotNull(response);
        assertEquals("200", response.getCode());
        assertEquals("No vehicles found matching the criteria", response.getMessage());
        assertNotNull(response.getBody());

        List<?> bodyList = (List<?>) response.getBody();
        assertTrue(bodyList.isEmpty());

        verify(vehicleListingRepository, times(1)).findAllByOrderByCreatedAtDesc();
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

    // Range filter validation tests

    @Test
    void getVehicleListings_WithValidPriceRange_ShouldReturnFilteredListings() {
        // Arrange
        VehicleSearchCriteria criteria = new VehicleSearchCriteria();
        criteria.setFromPrice(5000.0);
        criteria.setToPrice(15000.0);

        List<VehicleListing> vehicleListings = Arrays.asList(vehicleListing);
        when(vehicleListingRepository.findByCriteria(criteria)).thenReturn(vehicleListings);
        when(vehicleListingMapper.entityToResponse(any(VehicleListing.class))).thenReturn(vehicleListingResponse);

        // Act
        GenericResponse response = vehicleListingService.getVehicleListings(criteria);

        // Assert
        assertNotNull(response);
        assertEquals("200", response.getCode());
        assertEquals("success", response.getMessage());

        verify(vehicleListingRepository, times(1)).findByCriteria(criteria);
    }

    @Test
    void getVehicleListings_WithValidYearRange_ShouldReturnFilteredListings() {
        // Arrange
        VehicleSearchCriteria criteria = new VehicleSearchCriteria();
        criteria.setFromYear(2015);
        criteria.setToYear(2020);

        List<VehicleListing> vehicleListings = Arrays.asList(vehicleListing);
        when(vehicleListingRepository.findByCriteria(criteria)).thenReturn(vehicleListings);
        when(vehicleListingMapper.entityToResponse(any(VehicleListing.class))).thenReturn(vehicleListingResponse);

        // Act
        GenericResponse response = vehicleListingService.getVehicleListings(criteria);

        // Assert
        assertNotNull(response);
        assertEquals("200", response.getCode());
        assertEquals("success", response.getMessage());

        verify(vehicleListingRepository, times(1)).findByCriteria(criteria);
    }

    @Test
    void getVehicleListings_WithOnlyFromPrice_ShouldReturnValidationError() {
        // Arrange
        VehicleSearchCriteria criteria = new VehicleSearchCriteria();
        criteria.setFromPrice(5000.0);
        // toPrice is not set

        // Act
        GenericResponse response = vehicleListingService.getVehicleListings(criteria);

        // Assert
        assertNotNull(response);
        assertEquals("400", response.getCode());
        assertEquals("failed", response.getStatus());
        assertTrue(response.getMessage().contains("Both fromPrice and toPrice must be provided together"));

        verify(vehicleListingRepository, never()).findByCriteria(any());
    }

    @Test
    void getVehicleListings_WithOnlyToPrice_ShouldReturnValidationError() {
        // Arrange
        VehicleSearchCriteria criteria = new VehicleSearchCriteria();
        criteria.setToPrice(15000.0);
        // fromPrice is not set

        // Act
        GenericResponse response = vehicleListingService.getVehicleListings(criteria);

        // Assert
        assertNotNull(response);
        assertEquals("400", response.getCode());
        assertEquals("failed", response.getStatus());
        assertTrue(response.getMessage().contains("Both fromPrice and toPrice must be provided together"));

        verify(vehicleListingRepository, never()).findByCriteria(any());
    }

    @Test
    void getVehicleListings_WithInvalidPriceRange_ShouldReturnValidationError() {
        // Arrange
        VehicleSearchCriteria criteria = new VehicleSearchCriteria();
        criteria.setFromPrice(15000.0);
        criteria.setToPrice(5000.0); // fromPrice > toPrice

        // Act
        GenericResponse response = vehicleListingService.getVehicleListings(criteria);

        // Assert
        assertNotNull(response);
        assertEquals("400", response.getCode());
        assertEquals("failed", response.getStatus());
        assertTrue(response.getMessage().contains("fromPrice") &&
                   response.getMessage().contains("cannot be greater than toPrice"));

        verify(vehicleListingRepository, never()).findByCriteria(any());
    }

    @Test
    void getVehicleListings_WithNegativePriceValues_ShouldReturnValidationError() {
        // Arrange
        VehicleSearchCriteria criteria = new VehicleSearchCriteria();
        criteria.setFromPrice(-5000.0);
        criteria.setToPrice(15000.0);

        // Act
        GenericResponse response = vehicleListingService.getVehicleListings(criteria);

        // Assert
        assertNotNull(response);
        assertEquals("400", response.getCode());
        assertEquals("failed", response.getStatus());
        assertTrue(response.getMessage().contains("Price values must be positive"));

        verify(vehicleListingRepository, never()).findByCriteria(any());
    }

    @Test
    void getVehicleListings_WithOnlyFromYear_ShouldReturnValidationError() {
        // Arrange
        VehicleSearchCriteria criteria = new VehicleSearchCriteria();
        criteria.setFromYear(2015);
        // toYear is not set

        // Act
        GenericResponse response = vehicleListingService.getVehicleListings(criteria);

        // Assert
        assertNotNull(response);
        assertEquals("400", response.getCode());
        assertEquals("failed", response.getStatus());
        assertTrue(response.getMessage().contains("Both fromYear and toYear must be provided together"));

        verify(vehicleListingRepository, never()).findByCriteria(any());
    }

    @Test
    void getVehicleListings_WithInvalidYearRange_ShouldReturnValidationError() {
        // Arrange
        VehicleSearchCriteria criteria = new VehicleSearchCriteria();
        criteria.setFromYear(2020);
        criteria.setToYear(2015); // fromYear > toYear

        // Act
        GenericResponse response = vehicleListingService.getVehicleListings(criteria);

        // Assert
        assertNotNull(response);
        assertEquals("400", response.getCode());
        assertEquals("failed", response.getStatus());
        assertTrue(response.getMessage().contains("fromYear") &&
                   response.getMessage().contains("cannot be greater than toYear"));

        verify(vehicleListingRepository, never()).findByCriteria(any());
    }

    @Test
    void getVehicleListings_WithBothPriceAndYearRanges_ShouldReturnFilteredListings() {
        // Arrange
        VehicleSearchCriteria criteria = new VehicleSearchCriteria();
        criteria.setFromPrice(5000.0);
        criteria.setToPrice(15000.0);
        criteria.setFromYear(2015);
        criteria.setToYear(2020);

        List<VehicleListing> vehicleListings = Arrays.asList(vehicleListing);
        when(vehicleListingRepository.findByCriteria(criteria)).thenReturn(vehicleListings);
        when(vehicleListingMapper.entityToResponse(any(VehicleListing.class))).thenReturn(vehicleListingResponse);

        // Act
        GenericResponse response = vehicleListingService.getVehicleListings(criteria);

        // Assert
        assertNotNull(response);
        assertEquals("200", response.getCode());
        assertEquals("success", response.getMessage());

        verify(vehicleListingRepository, times(1)).findByCriteria(criteria);
    }

    @Test
    void getVehicleListings_WithBoundaryPriceValues_ShouldReturnFilteredListings() {
        // Arrange - Test equal from and to values (valid edge case)
        VehicleSearchCriteria criteria = new VehicleSearchCriteria();
        criteria.setFromPrice(10000.0);
        criteria.setToPrice(10000.0); // Same value, should be valid

        List<VehicleListing> vehicleListings = Arrays.asList(vehicleListing);
        when(vehicleListingRepository.findByCriteria(criteria)).thenReturn(vehicleListings);
        when(vehicleListingMapper.entityToResponse(any(VehicleListing.class))).thenReturn(vehicleListingResponse);

        // Act
        GenericResponse response = vehicleListingService.getVehicleListings(criteria);

        // Assert
        assertNotNull(response);
        assertEquals("200", response.getCode());
        assertEquals("success", response.getMessage());

        verify(vehicleListingRepository, times(1)).findByCriteria(criteria);
    }

    // Multi-city location search tests

    @Test
    void getVehicleListings_WithSingleCity_ShouldReturnFilteredListings() {
        // Arrange - Test backward compatibility
        VehicleSearchCriteria criteria = new VehicleSearchCriteria();
        criteria.setLocation("Bhopal");

        List<VehicleListing> vehicleListings = Arrays.asList(vehicleListing);
        when(vehicleListingRepository.findByCriteria(criteria)).thenReturn(vehicleListings);
        when(vehicleListingMapper.entityToResponse(any(VehicleListing.class))).thenReturn(vehicleListingResponse);

        // Act
        GenericResponse response = vehicleListingService.getVehicleListings(criteria);

        // Assert
        assertNotNull(response);
        assertEquals("200", response.getCode());
        assertEquals("success", response.getMessage());
        assertEquals(1, criteria.getCitiesAsList().size());
        assertEquals("bhopal", criteria.getCitiesAsList().get(0));

        verify(vehicleListingRepository, times(1)).findByCriteria(criteria);
    }

    @Test
    void getVehicleListings_WithMultipleCities_ShouldReturnFilteredListings() {
        // Arrange
        VehicleSearchCriteria criteria = new VehicleSearchCriteria();
        criteria.setLocation("Bhopal,Indore,Delhi");

        List<VehicleListing> vehicleListings = Arrays.asList(vehicleListing);
        when(vehicleListingRepository.findByCriteria(criteria)).thenReturn(vehicleListings);
        when(vehicleListingMapper.entityToResponse(any(VehicleListing.class))).thenReturn(vehicleListingResponse);

        // Act
        GenericResponse response = vehicleListingService.getVehicleListings(criteria);

        // Assert
        assertNotNull(response);
        assertEquals("200", response.getCode());
        assertEquals("success", response.getMessage());

        List<String> cities = criteria.getCitiesAsList();
        assertEquals(3, cities.size());
        assertTrue(cities.contains("bhopal"));
        assertTrue(cities.contains("indore"));
        assertTrue(cities.contains("delhi"));
        assertTrue(criteria.hasMultipleCities());

        verify(vehicleListingRepository, times(1)).findByCriteria(criteria);
    }

    @Test
    void getVehicleListings_WithMultipleCitiesAndSpaces_ShouldNormalizeCities() {
        // Arrange - Test trimming and normalization
        VehicleSearchCriteria criteria = new VehicleSearchCriteria();
        criteria.setLocation("  Bhopal  , Indore , Delhi  ");

        List<VehicleListing> vehicleListings = Arrays.asList(vehicleListing);
        when(vehicleListingRepository.findByCriteria(criteria)).thenReturn(vehicleListings);
        when(vehicleListingMapper.entityToResponse(any(VehicleListing.class))).thenReturn(vehicleListingResponse);

        // Act
        GenericResponse response = vehicleListingService.getVehicleListings(criteria);

        // Assert
        assertNotNull(response);

        List<String> cities = criteria.getCitiesAsList();
        assertEquals(3, cities.size());
        assertEquals("bhopal", cities.get(0));
        assertEquals("indore", cities.get(1));
        assertEquals("delhi", cities.get(2));
    }

    @Test
    void getVehicleListings_WithDuplicateCities_ShouldRemoveDuplicates() {
        // Arrange
        VehicleSearchCriteria criteria = new VehicleSearchCriteria();
        criteria.setLocation("Bhopal,Indore,bhopal,BHOPAL");

        // Act
        List<String> cities = criteria.getCitiesAsList();

        // Assert
        assertEquals(2, cities.size());
        assertTrue(cities.contains("bhopal"));
        assertTrue(cities.contains("indore"));
    }

    @Test
    void getVehicleListings_WithEmptyCitiesInList_ShouldFilterThem() {
        // Arrange - Test handling of empty values
        VehicleSearchCriteria criteria = new VehicleSearchCriteria();
        criteria.setLocation("Bhopal,,Indore,  ,Delhi");

        // Act
        List<String> cities = criteria.getCitiesAsList();

        // Assert
        assertEquals(3, cities.size());
        assertTrue(cities.contains("bhopal"));
        assertTrue(cities.contains("indore"));
        assertTrue(cities.contains("delhi"));
    }

    @Test
    void getVehicleListings_WithMultipleCitiesPaginated_ShouldReturnPagedResults() {
        // Arrange
        VehicleSearchCriteria criteria = new VehicleSearchCriteria();
        criteria.setLocation("Bhopal,Indore,Delhi");
        criteria.setPage(0);
        criteria.setSize(10);

        Page<VehicleListing> page = new PageImpl<>(Arrays.asList(vehicleListing));
        when(vehicleListingRepository.findByCriteriaWithPagination(criteria)).thenReturn(page);
        when(vehicleListingMapper.entityToResponse(any(VehicleListing.class))).thenReturn(vehicleListingResponse);

        // Act
        GenericResponse response = vehicleListingService.getVehicleListingsWithPagination(criteria);

        // Assert
        assertNotNull(response);
        assertEquals("200", response.getCode());
        assertEquals("success", response.getMessage());

        assertEquals(3, criteria.getCitiesAsList().size());

        verify(vehicleListingRepository, times(1)).findByCriteriaWithPagination(criteria);
    }

    @Test
    void getCitiesAsList_WithNullLocation_ShouldReturnEmptyList() {
        // Arrange
        VehicleSearchCriteria criteria = new VehicleSearchCriteria();
        criteria.setLocation(null);

        // Act
        List<String> cities = criteria.getCitiesAsList();

        // Assert
        assertNotNull(cities);
        assertTrue(cities.isEmpty());
        assertFalse(criteria.hasMultipleCities());
    }

    @Test
    void getCitiesAsList_WithEmptyLocation_ShouldReturnEmptyList() {
        // Arrange
        VehicleSearchCriteria criteria = new VehicleSearchCriteria();
        criteria.setLocation("   ");

        // Act
        List<String> cities = criteria.getCitiesAsList();

        // Assert
        assertNotNull(cities);
        assertTrue(cities.isEmpty());
    }
}