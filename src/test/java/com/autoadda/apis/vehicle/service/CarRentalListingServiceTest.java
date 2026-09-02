package com.autoadda.apis.vehicle.service;

import com.autoadda.apis.vehicle.dao.CarRentalListingRepository;
import com.autoadda.apis.vehicle.dao.VehicleDetailsRepository;
import com.autoadda.apis.vehicle.entity.CarRentalListing;
import com.autoadda.apis.vehicle.entity.VehicleDetails;
import com.autoadda.apis.vehicle.enums.RentalListingStatus;
import com.autoadda.apis.vehicle.mapper.CarRentalListingMapper;
import com.autoadda.apis.vehicle.request.CarRentalListingRequest;
import com.autoadda.apis.vehicle.request.RentalSearchCriteria;
import com.autoadda.apis.vehicle.response.CarRentalListingResponse;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.response.PagedResponse;
import com.autoadda.apis.vehicle.service.impl.CarRentalListingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarRentalListingServiceTest {

    @Mock
    private CarRentalListingRepository listingRepository;

    @Mock
    private VehicleDetailsRepository vehicleDetailsRepository;

    @Mock
    private CarRentalListingMapper mapper;

    private CarRentalListingServiceImpl service;

    private static final Integer OWNER_ID = 1;
    private static final Integer OTHER_USER_ID = 2;
    private static final Integer VEHICLE_ID = 10;

    private VehicleDetails vehicle;

    @BeforeEach
    void setUp() {
        service = new CarRentalListingServiceImpl(listingRepository, vehicleDetailsRepository, mapper);

        vehicle = VehicleDetails.builder()
                .id(VEHICLE_ID)
                .customerId(OWNER_ID)
                .brand("Toyota")
                .model("Corolla")
                .build();

        lenient().when(vehicleDetailsRepository.findById(VEHICLE_ID)).thenReturn(Optional.of(vehicle));
        lenient().when(mapper.toResponse(any(CarRentalListing.class), any()))
                .thenReturn(CarRentalListingResponse.builder().build());
        lenient().when(listingRepository.save(any(CarRentalListing.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private CarRentalListingRequest buildRequest() {
        CarRentalListingRequest request = new CarRentalListingRequest();
        request.setVehicleId(VEHICLE_ID);
        request.setPricePerHour(BigDecimal.valueOf(25));
        request.setPricePerDay(BigDecimal.valueOf(150));
        request.setKmLimitPerDay(200);
        request.setExcessKmPenaltyRate(BigDecimal.valueOf(1.5));
        request.setLateReturnPenaltyRate(BigDecimal.valueOf(20));
        request.setLocation("Dubai");
        return request;
    }

    @Test
    void createListing_ownerOwnsVehicle_shouldSucceed() {
        when(listingRepository.existsByVehicleIdAndStatusAndIsDeletedFalse(VEHICLE_ID, RentalListingStatus.ACTIVE))
                .thenReturn(false);
        when(mapper.toEntity(any(CarRentalListingRequest.class), any()))
                .thenReturn(CarRentalListing.builder().vehicleId(VEHICLE_ID).ownerId(OWNER_ID)
                        .status(RentalListingStatus.ACTIVE).isDeleted(false).build());

        GenericResponse response = service.createListing(buildRequest(), OWNER_ID);

        assertEquals("success", response.getStatus());
        assertEquals("201", response.getCode());

        ArgumentCaptor<CarRentalListing> captor = ArgumentCaptor.forClass(CarRentalListing.class);
        org.mockito.Mockito.verify(listingRepository).save(captor.capture());
        assertEquals(OWNER_ID, captor.getValue().getOwnerId());
    }

    @Test
    void createListing_callerDoesNotOwnVehicle_shouldReturn403() {
        GenericResponse response = service.createListing(buildRequest(), OTHER_USER_ID);

        assertEquals("error", response.getStatus());
        assertEquals("403", response.getCode());
    }

    @Test
    void createListing_vehicleAlreadyHasActiveListing_shouldReturn400() {
        when(listingRepository.existsByVehicleIdAndStatusAndIsDeletedFalse(VEHICLE_ID, RentalListingStatus.ACTIVE))
                .thenReturn(true);

        GenericResponse response = service.createListing(buildRequest(), OWNER_ID);

        assertEquals("error", response.getStatus());
        assertEquals("400", response.getCode());
    }

    @Test
    void browseListings_invalidSortField_shouldReturn400() {
        RentalSearchCriteria criteria = RentalSearchCriteria.builder().sortBy("notAField").build();

        GenericResponse response = service.browseListings(criteria);

        assertEquals("error", response.getStatus());
        assertEquals("400", response.getCode());
    }

    @Test
    void browseListings_default_shouldReturnPagedResponse() {
        CarRentalListing listing = CarRentalListing.builder()
                .id(1L).vehicleId(VEHICLE_ID).ownerId(OWNER_ID)
                .pricePerDay(BigDecimal.valueOf(150)).location("Dubai")
                .status(RentalListingStatus.ACTIVE).build();
        Page<CarRentalListing> page = new PageImpl<>(List.of(listing), PageRequest.of(0, 10), 1);
        when(listingRepository.findByCriteriaWithPagination(any(RentalSearchCriteria.class))).thenReturn(page);

        GenericResponse response = service.browseListings(RentalSearchCriteria.builder().build());

        assertEquals("success", response.getStatus());
        assertEquals("200", response.getCode());
        PagedResponse<?> body = (PagedResponse<?>) response.getBody();
        assertEquals(1, body.getTotalElements());
    }
}
