package com.autoadda.apis.vehicle.service;



import com.autoadda.apis.vehicle.dao.MediaFileRepository;
import com.autoadda.apis.vehicle.dao.VehicleOwnerDocumentRepository;
import com.autoadda.apis.vehicle.entity.MediaFile;
import com.autoadda.apis.vehicle.entity.VehicleOwnerDocumentation;
import com.autoadda.apis.vehicle.exception.ResourceNotFoundException;
import com.autoadda.apis.vehicle.request.VehicleOwnerDocumentRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.impl.VehicleOwnerDocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VehicleOwnerDocumentServiceTest {

    @Mock
    private VehicleOwnerDocumentRepository vehicleDocumentRepository;

    @Mock
    private MediaFileRepository mediaFileRepository;

    @InjectMocks
    private VehicleOwnerDocumentService vehicleOwnerDocumentService;

    private VehicleOwnerDocumentRequest request;
    private VehicleOwnerDocumentation documentation;
    private MediaFile emirateIdFile;
    private MediaFile tradeLicenceFile;
    private MediaFile drivingLicenceFile;

    @BeforeEach
    void setUp() {
        // Setup test data
        request = new VehicleOwnerDocumentRequest();
        request.setVehicleOwnerId(1);
        request.setEmirateId(100L);
        request.setTradeLicenceId(200L);
        request.setDrivingLicenceId(300L);

        documentation = new VehicleOwnerDocumentation();
        documentation.setId(1);
        documentation.setVehicleOwnerId(1);

        emirateIdFile = new MediaFile();
        emirateIdFile.setId(100L);
        emirateIdFile.setFileName("emirate_id.jpg");

        tradeLicenceFile = new MediaFile();
        tradeLicenceFile.setId(200L);
        tradeLicenceFile.setFileName("trade_licence.jpg");

        drivingLicenceFile = new MediaFile();
        drivingLicenceFile.setId(300L);
        drivingLicenceFile.setFileName("driving_licence.jpg");
    }

    @Test
    void uploadVehicleOwnerDocument_withAllFiles_shouldSuccess() {
        // Arrange
        when(mediaFileRepository.findById(100L)).thenReturn(Optional.of(emirateIdFile));
        when(mediaFileRepository.findById(200L)).thenReturn(Optional.of(tradeLicenceFile));
        when(mediaFileRepository.findById(300L)).thenReturn(Optional.of(drivingLicenceFile));
        when(vehicleDocumentRepository.save(any(VehicleOwnerDocumentation.class))).thenReturn(documentation);

        // Act
        GenericResponse response = vehicleOwnerDocumentService.uploadVehicleOwnerDocument(request);

        // Assert
        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertEquals("200", response.getCode());
        assertEquals("Vehicle owner document uploaded successfully", response.getMessage());
        assertNotNull(response.getBody());

        verify(mediaFileRepository, times(1)).findById(100L);
        verify(mediaFileRepository, times(1)).findById(200L);
        verify(mediaFileRepository, times(1)).findById(300L);
        verify(vehicleDocumentRepository, times(1)).save(any(VehicleOwnerDocumentation.class));
    }

    @Test
    void uploadVehicleOwnerDocument_withOnlyEmirateId_shouldSuccess() {
        // Arrange
        request.setTradeLicenceId(null);
        request.setDrivingLicenceId(null);

        when(mediaFileRepository.findById(100L)).thenReturn(Optional.of(emirateIdFile));
        when(vehicleDocumentRepository.save(any(VehicleOwnerDocumentation.class))).thenReturn(documentation);

        // Act
        GenericResponse response = vehicleOwnerDocumentService.uploadVehicleOwnerDocument(request);

        // Assert
        assertNotNull(response);
        assertEquals("success", response.getStatus());
        verify(mediaFileRepository, times(1)).findById(100L);
        verify(mediaFileRepository, never()).findById(200L);
        verify(mediaFileRepository, never()).findById(300L);
        verify(vehicleDocumentRepository, times(1)).save(any(VehicleOwnerDocumentation.class));
    }

    @Test
    void uploadVehicleOwnerDocument_emirateIdNotFound_shouldThrowException() {
        // Arrange
        when(mediaFileRepository.findById(100L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            vehicleOwnerDocumentService.uploadVehicleOwnerDocument(request);
        });

        assertEquals("emirate image not found", exception.getMessage());
        verify(mediaFileRepository, times(1)).findById(100L);
        verify(vehicleDocumentRepository, never()).save(any());
    }

    @Test
    void uploadVehicleOwnerDocument_tradeLicenceNotFound_shouldThrowException() {
        // Arrange
        when(mediaFileRepository.findById(100L)).thenReturn(Optional.of(emirateIdFile));
        when(mediaFileRepository.findById(200L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            vehicleOwnerDocumentService.uploadVehicleOwnerDocument(request);
        });

        assertEquals("trade licence image not found", exception.getMessage());
        verify(mediaFileRepository, times(1)).findById(100L);
        verify(mediaFileRepository, times(1)).findById(200L);
        verify(vehicleDocumentRepository, never()).save(any());
    }

    @Test
    void getVehicleOwnerDocument_exists_shouldReturnSuccess() {
        // Arrange
        when(vehicleDocumentRepository.findByVehicleOwnerId(1)).thenReturn(Optional.of(documentation));

        // Act
        GenericResponse response = vehicleOwnerDocumentService.getVehicleOwnerDocument(1);

        // Assert
        assertNotNull(response);
        assertEquals("success", response.getStatus());
//        assertEquals("200L", response.getCode());
        assertEquals("Vehicle owner document fetched successfully", response.getMessage());
        assertNotNull(response.getBody());

        verify(vehicleDocumentRepository, times(1)).findByVehicleOwnerId(1);
    }

    @Test
    void getVehicleOwnerDocument_notExists_shouldThrowException() {
        // Arrange
        when(vehicleDocumentRepository.findByVehicleOwnerId(1)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            vehicleOwnerDocumentService.getVehicleOwnerDocument(1);
        });

        assertEquals("Vehicle owner document not found", exception.getMessage());
        verify(vehicleDocumentRepository, times(1)).findByVehicleOwnerId(1);
    }

    @Test
    void updateVehicleOwnerDocument_exists_shouldUpdateSuccessfully() {
        // Arrange
        when(vehicleDocumentRepository.findByVehicleOwnerId(1)).thenReturn(Optional.of(documentation));
        when(mediaFileRepository.findById(100L)).thenReturn(Optional.of(emirateIdFile));
        when(mediaFileRepository.findById(200L)).thenReturn(Optional.of(tradeLicenceFile));
        when(mediaFileRepository.findById(300L)).thenReturn(Optional.of(drivingLicenceFile));
        when(vehicleDocumentRepository.save(any(VehicleOwnerDocumentation.class))).thenReturn(documentation);

        // Act
        GenericResponse response = vehicleOwnerDocumentService.updateVehicleOwnerDocument(1, request);

        // Assert
        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertEquals("200", response.getCode());
        assertEquals("Vehicle owner document updated successfully", response.getMessage());
        assertNotNull(response.getBody());

        verify(vehicleDocumentRepository, times(1)).findByVehicleOwnerId(1);
        verify(mediaFileRepository, times(1)).findById(100L);
        verify(mediaFileRepository, times(1)).findById(200L);
        verify(mediaFileRepository, times(1)).findById(300L);
        verify(vehicleDocumentRepository, times(1)).save(documentation);
    }

    @Test
    void updateVehicleOwnerDocument_notExists_shouldCreateNew() {
        // Arrange
        when(vehicleDocumentRepository.findByVehicleOwnerId(1)).thenReturn(Optional.empty());
        when(mediaFileRepository.findById(100L)).thenReturn(Optional.of(emirateIdFile));
        when(mediaFileRepository.findById(200L)).thenReturn(Optional.of(tradeLicenceFile));
        when(mediaFileRepository.findById(300L)).thenReturn(Optional.of(drivingLicenceFile));
        when(vehicleDocumentRepository.save(any(VehicleOwnerDocumentation.class))).thenReturn(documentation);

        // Act
        GenericResponse response = vehicleOwnerDocumentService.updateVehicleOwnerDocument(1, request);

        // Assert
        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertEquals("Vehicle owner document updated successfully", response.getMessage());

        verify(vehicleDocumentRepository, times(1)).findByVehicleOwnerId(1);
        verify(vehicleDocumentRepository, times(1)).save(any(VehicleOwnerDocumentation.class));
    }

    @Test
    void updateVehicleOwnerDocument_partialUpdate_shouldUpdateOnlyProvidedFields() {
        // Arrange
        request.setEmirateId(null); // Only update trade licence and driving licence
        request.setDrivingLicenceId(null); // Only update trade licence

        when(vehicleDocumentRepository.findByVehicleOwnerId(1)).thenReturn(Optional.of(documentation));
        when(mediaFileRepository.findById(200L)).thenReturn(Optional.of(tradeLicenceFile));
        when(vehicleDocumentRepository.save(any(VehicleOwnerDocumentation.class))).thenReturn(documentation);

        // Act
        GenericResponse response = vehicleOwnerDocumentService.updateVehicleOwnerDocument(1, request);

        // Assert
        assertNotNull(response);
        assertEquals("success", response.getStatus());

        verify(mediaFileRepository, never()).findById(100L); // emirateId not updated
        verify(mediaFileRepository, times(1)).findById(200L); // tradeLicence updated
        verify(mediaFileRepository, never()).findById(300L); // drivingLicence not updated
        verify(vehicleDocumentRepository, times(1)).save(documentation);
    }

    @Test
    void updateVehicleOwnerDocument_fileNotFound_shouldThrowException() {
        // Arrange
        when(vehicleDocumentRepository.findByVehicleOwnerId(1)).thenReturn(Optional.of(documentation));
        when(mediaFileRepository.findById(100L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            vehicleOwnerDocumentService.updateVehicleOwnerDocument(1, request);
        });

        assertEquals("emirate image not found", exception.getMessage());
        verify(mediaFileRepository, times(1)).findById(100L);
        verify(vehicleDocumentRepository, never()).save(any());
    }

    @Test
    void updateVehicleOwnerDocument_nullRequestFields_shouldNotThrowException() {
        // Arrange
        request.setEmirateId(null);
        request.setTradeLicenceId(null);
        request.setDrivingLicenceId(null);

        when(vehicleDocumentRepository.findByVehicleOwnerId(1)).thenReturn(Optional.of(documentation));
        when(vehicleDocumentRepository.save(any(VehicleOwnerDocumentation.class))).thenReturn(documentation);

        // Act
        GenericResponse response = vehicleOwnerDocumentService.updateVehicleOwnerDocument(1, request);

        // Assert
        assertNotNull(response);
        assertEquals("success", response.getStatus());

        verify(mediaFileRepository, never()).findById(any());
        verify(vehicleDocumentRepository, times(1)).save(documentation);
    }

    @Test
    void uploadVehicleOwnerDocument_nullFiles_shouldSuccess() {
        // Arrange
        request.setEmirateId(null);
        request.setTradeLicenceId(null);
        request.setDrivingLicenceId(null);

        when(vehicleDocumentRepository.save(any(VehicleOwnerDocumentation.class))).thenReturn(documentation);

        // Act
        GenericResponse response = vehicleOwnerDocumentService.uploadVehicleOwnerDocument(request);

        // Assert
        assertNotNull(response);
        assertEquals("success", response.getStatus());

        verify(mediaFileRepository, never()).findById(any());
        verify(vehicleDocumentRepository, times(1)).save(any(VehicleOwnerDocumentation.class));
    }
}