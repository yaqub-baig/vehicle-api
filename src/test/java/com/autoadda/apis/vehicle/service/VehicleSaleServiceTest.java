package com.autoadda.apis.vehicle.service;

import com.autoadda.apis.vehicle.clients.AuthClient;
import com.autoadda.apis.vehicle.dao.VehicleDetailsRepository;
import com.autoadda.apis.vehicle.dao.VehicleListingRepository;
import com.autoadda.apis.vehicle.dao.VehicleSaleInvoiceRepository;
import com.autoadda.apis.vehicle.dao.VehicleSaleRepository;
import com.autoadda.apis.vehicle.entity.VehicleDetails;
import com.autoadda.apis.vehicle.entity.VehicleListing;
import com.autoadda.apis.vehicle.entity.VehicleSale;
import com.autoadda.apis.vehicle.enums.CompanyType;
import com.autoadda.apis.vehicle.mapper.VehicleSaleMapper;
import com.autoadda.apis.vehicle.request.CompleteSaleRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.response.UserResponse;
import com.autoadda.apis.vehicle.response.VehicleSaleResponse;
import com.autoadda.apis.vehicle.service.impl.VehicleSaleServiceImpl;
import com.autoadda.apis.vehicle.util.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleSaleServiceTest {

    private static final Integer LISTING_ID = 1;
    private static final Integer VEHICLE_ID = 10;
    private static final Integer SELLER_ID = 100;
    private static final Integer BUYER_ID = 200;

    @Mock
    private VehicleListingRepository vehicleListingRepository;
    @Mock
    private VehicleDetailsRepository vehicleDetailsRepository;
    @Mock
    private VehicleSaleRepository vehicleSaleRepository;
    @Mock
    private VehicleSaleInvoiceRepository vehicleSaleInvoiceRepository;
    @Mock
    private VehicleSaleMapper vehicleSaleMapper;
    @Mock
    private AuthClient authClient;
    @Mock
    private CompanyAuthorizationService companyAuth;
    @InjectMocks
    private VehicleSaleServiceImpl service;

    private CompleteSaleRequest request;
    private VehicleDetails vehicle;
    private VehicleListing listing;

    @BeforeEach
    void setUp() {
        request = new CompleteSaleRequest();
        request.setListingId(LISTING_ID);
        request.setBuyerUserId(BUYER_ID);
        request.setSalePrice(85000.0);
        request.setPaymentMode("CASH");

        vehicle = new VehicleDetails();
        vehicle.setId(VEHICLE_ID);
        vehicle.setCustomerId(SELLER_ID);
        vehicle.setIsForSale(Boolean.TRUE);

        listing = VehicleListing.builder()
                .id(LISTING_ID)
                .vehicle(vehicle)
                .status(Status.LISTED)
                .build();

        lenient().when(vehicleSaleMapper.toResponse(any())).thenReturn(VehicleSaleResponse.builder().build());
    }

    @Test
    void completeSale_happyPath_recordsSaleAndTransfersOwnership() {
        when(vehicleListingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing));
        when(vehicleSaleRepository.findByVehicleListing_Id(LISTING_ID)).thenReturn(Optional.empty());
        when(authClient.getUser(BUYER_ID)).thenReturn(UserResponse.builder().id(BUYER_ID).name("Buyer B").build());
        when(vehicleSaleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        GenericResponse response = service.completeSale(request, SELLER_ID);

        assertEquals("200", response.getCode());

        ArgumentCaptor<VehicleSale> saleCaptor = ArgumentCaptor.forClass(VehicleSale.class);
        verify(vehicleSaleRepository).save(saleCaptor.capture());
        VehicleSale sale = saleCaptor.getValue();
        assertEquals(SELLER_ID, sale.getPreviousOwnerId());
        assertEquals(BUYER_ID, sale.getNewOwnerId());
        assertEquals(Status.ACTIVE, sale.getStatus());
        assertEquals("CASH", sale.getPaymentMode());
        assertEquals(85000.0, sale.getInvoice().getTotalAmount());

        assertEquals(Status.SOLD, listing.getStatus());
        assertFalse(vehicle.getIsForSale());
        assertEquals(BUYER_ID, vehicle.getCustomerId());
        assertEquals(SELLER_ID, vehicle.getPreviousOwnerId());
        verify(vehicleDetailsRepository).save(vehicle);
    }

    @Test
    void completeSale_dealerCompanyOwner_allowed() {
        vehicle.setCompanyId(9L);
        when(vehicleListingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing));
        when(companyAuth.isOwnerWithType(9L, 500, CompanyType.USED_CAR_DEALER)).thenReturn(true);
        when(vehicleSaleRepository.findByVehicleListing_Id(LISTING_ID)).thenReturn(Optional.empty());
        when(authClient.getUser(BUYER_ID)).thenReturn(UserResponse.builder().id(BUYER_ID).name("Buyer B").build());
        when(vehicleSaleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        GenericResponse response = service.completeSale(request, 500);

        assertEquals("200", response.getCode());
    }

    @Test
    void completeSale_listingNotFound_returns404() {
        when(vehicleListingRepository.findById(LISTING_ID)).thenReturn(Optional.empty());

        GenericResponse response = service.completeSale(request, SELLER_ID);

        assertEquals("404", response.getCode());
        verify(vehicleSaleRepository, never()).save(any());
    }

    @Test
    void completeSale_listingNotOpen_returns400() {
        listing.setStatus(Status.SOLD);
        when(vehicleListingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing));

        GenericResponse response = service.completeSale(request, SELLER_ID);

        assertEquals("400", response.getCode());
    }

    @Test
    void completeSale_saleAlreadyExists_returns409() {
        when(vehicleListingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing));
        when(vehicleSaleRepository.findByVehicleListing_Id(LISTING_ID)).thenReturn(Optional.of(new VehicleSale()));

        GenericResponse response = service.completeSale(request, SELLER_ID);

        assertEquals("409", response.getCode());
        verify(vehicleSaleRepository, never()).save(any());
    }

    @Test
    void completeSale_callerNotOwnerOrDealer_returns403() {
        when(vehicleListingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing));

        GenericResponse response = service.completeSale(request, 999);

        assertEquals("403", response.getCode());
        verify(vehicleSaleRepository, never()).save(any());
    }

    @Test
    void completeSale_buyerNotFound_returns404() {
        when(vehicleListingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing));
        when(vehicleSaleRepository.findByVehicleListing_Id(LISTING_ID)).thenReturn(Optional.empty());
        when(authClient.getUser(BUYER_ID)).thenReturn(null);

        GenericResponse response = service.completeSale(request, SELLER_ID);

        assertEquals("404", response.getCode());
        verify(vehicleSaleRepository, never()).save(any());
    }

    @Test
    void completeSale_invalidPaymentMode_returns400() {
        request.setPaymentMode("BITCOIN");

        GenericResponse response = service.completeSale(request, SELLER_ID);

        assertEquals("400", response.getCode());
        verify(vehicleListingRepository, never()).findById(any());
    }
}
