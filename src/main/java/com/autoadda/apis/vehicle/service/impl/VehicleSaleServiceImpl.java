package com.autoadda.apis.vehicle.service.impl;

import com.autoadda.apis.vehicle.clients.AuthClient;
import com.autoadda.apis.vehicle.dao.VehicleDetailsRepository;
import com.autoadda.apis.vehicle.dao.VehicleListingRepository;
import com.autoadda.apis.vehicle.dao.VehicleSaleInvoiceRepository;
import com.autoadda.apis.vehicle.dao.VehicleSaleRepository;
import com.autoadda.apis.vehicle.entity.VehicleDetails;
import com.autoadda.apis.vehicle.entity.VehicleListing;
import com.autoadda.apis.vehicle.entity.VehicleSale;
import com.autoadda.apis.vehicle.entity.VehicleSaleInvoice;
import com.autoadda.apis.vehicle.enums.CompanyType;
import com.autoadda.apis.vehicle.mapper.VehicleSaleMapper;
import com.autoadda.apis.vehicle.request.CompleteSaleRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.response.UserResponse;
import com.autoadda.apis.vehicle.service.CompanyAuthorizationService;
import com.autoadda.apis.vehicle.service.VehicleSaleService;
import com.autoadda.apis.vehicle.util.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleSaleServiceImpl implements VehicleSaleService {

    private static final String ERROR = "error";
    private static final String SUCCESS = "success";
    private static final String SALE_NOT_FOUND = "Vehicle sale not found";
    private static final String NOT_A_PARTICIPANT = "You are not a participant in this sale";
    private static final Set<String> VALID_PAYMENT_MODES =
            Set.of("CASH", "CREDIT", "LOAN", "BANK_TRANSFER", "CHEQUE");

    private final VehicleListingRepository vehicleListingRepository;
    private final VehicleDetailsRepository vehicleDetailsRepository;
    private final VehicleSaleRepository vehicleSaleRepository;
    private final VehicleSaleInvoiceRepository vehicleSaleInvoiceRepository;
    private final VehicleSaleMapper vehicleSaleMapper;
    private final AuthClient authClient;
    private final CompanyAuthorizationService companyAuth;

    @Override
    @Transactional
    public GenericResponse completeSale(CompleteSaleRequest request, Integer callerId) {
        log.info("Completing sale for listing {} by user {}", request.getListingId(), callerId);

        String paymentMode = request.getPaymentMode() == null ? "" : request.getPaymentMode().trim().toUpperCase();
        if (!VALID_PAYMENT_MODES.contains(paymentMode)) {
            return GenericResponse.builder().status(ERROR).code("400")
                    .message("Payment mode must be one of " + VALID_PAYMENT_MODES).build();
        }

        Optional<VehicleListing> listingOpt = vehicleListingRepository.findById(request.getListingId());
        if (listingOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message("Vehicle listing not found").build();
        }

        VehicleListing listing = listingOpt.get();
        VehicleDetails vehicle = listing.getVehicle();
        if (vehicle == null) {
            return GenericResponse.builder().status(ERROR).code("404").message("Listed vehicle not found").build();
        }

        boolean allowed = callerId.equals(vehicle.getCustomerId())
                || (vehicle.getCompanyId() != null
                    && companyAuth.isOwnerWithType(vehicle.getCompanyId(), callerId, CompanyType.USED_CAR_DEALER));
        if (!allowed) {
            return GenericResponse.builder().status(ERROR).code("403")
                    .message("Only the vehicle owner or its used-car-dealer company can complete this sale").build();
        }

        if (listing.getStatus() != Status.LISTED) {
            return GenericResponse.builder().status(ERROR).code("400")
                    .message("This listing is not open for sale").build();
        }

        if (listing.getSale() != null
                || vehicleSaleRepository.findByVehicleListing_Id(listing.getId()).isPresent()) {
            return GenericResponse.builder().status(ERROR).code("409")
                    .message("A sale has already been recorded for this listing").build();
        }

        UserResponse buyer;
        try {
            buyer = authClient.getUser(request.getBuyerUserId());
        } catch (Exception e) {
            log.error("Failed to fetch buyer {}: {}", request.getBuyerUserId(), e.getMessage());
            buyer = null;
        }
        if (buyer == null) {
            return GenericResponse.builder().status(ERROR).code("404").message("Buyer not found").build();
        }

        String buyerName = request.getBuyerName() != null && !request.getBuyerName().isBlank()
                ? request.getBuyerName().trim()
                : buyer.getName();
        Integer previousOwnerId = vehicle.getCustomerId();
        LocalDate saleDate = request.getSaleDate() != null ? request.getSaleDate() : LocalDate.now();

        VehicleSale sale = VehicleSale.builder()
                .vehicle(vehicle)
                .previousOwnerId(previousOwnerId)
                .newOwnerId(request.getBuyerUserId())
                .saleDate(saleDate)
                .salePrice(request.getSalePrice())
                .buyerName(buyerName)
                .paymentMode(paymentMode)
                .status(Status.ACTIVE)
                .vehicleListing(listing)
                .build();

        VehicleSaleInvoice invoice = VehicleSaleInvoice.builder()
                .invoiceDate(LocalDate.now())
                .totalAmount(request.getInvoiceAmount() != null ? request.getInvoiceAmount() : request.getSalePrice())
                .paymentMethod(request.getInvoicePaymentMethod() != null && !request.getInvoicePaymentMethod().isBlank()
                        ? request.getInvoicePaymentMethod().trim()
                        : paymentMode)
                .vehicleSale(sale)
                .build();
        sale.setInvoice(invoice);

        listing.setStatus(Status.SOLD);
        listing.setSale(sale);

        vehicle.setIsForSale(Boolean.FALSE);
        // Ownership transfer - same technique as VehicleOwnershipClaimServiceImpl approval.
        vehicle.setPreviousOwnerId(previousOwnerId);
        vehicle.setCustomerId(request.getBuyerUserId());
        vehicle.setUpdatedAt(LocalDateTime.now());

        vehicleDetailsRepository.save(vehicle);
        VehicleSale savedSale = vehicleSaleRepository.save(sale);
        vehicleListingRepository.save(listing);

        log.info("Sale {} recorded for listing {}; vehicle {} ownership {} -> {}",
                savedSale.getId(), listing.getId(), vehicle.getId(), previousOwnerId, request.getBuyerUserId());

        return GenericResponse.builder()
                .status(SUCCESS).code("200")
                .message("Sale completed; ownership transferred to the buyer")
                .body(vehicleSaleMapper.toResponse(savedSale))
                .build();
    }

    @Override
    public GenericResponse getSaleById(Integer saleId, Integer callerId) {
        Optional<VehicleSale> saleOpt = vehicleSaleRepository.findById(saleId);
        if (saleOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(SALE_NOT_FOUND).build();
        }
        VehicleSale sale = saleOpt.get();
        if (!isParticipant(sale, callerId)) {
            return GenericResponse.builder().status(ERROR).code("403").message(NOT_A_PARTICIPANT).build();
        }
        return ok(sale);
    }

    @Override
    public GenericResponse getSaleByListingId(Integer listingId, Integer callerId) {
        Optional<VehicleSale> saleOpt = vehicleSaleRepository.findByVehicleListing_Id(listingId);
        if (saleOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(SALE_NOT_FOUND).build();
        }
        VehicleSale sale = saleOpt.get();
        if (!isParticipant(sale, callerId)) {
            return GenericResponse.builder().status(ERROR).code("403").message(NOT_A_PARTICIPANT).build();
        }
        return ok(sale);
    }

    @Override
    public GenericResponse getInvoice(Integer saleId, Integer callerId) {
        Optional<VehicleSale> saleOpt = vehicleSaleRepository.findById(saleId);
        if (saleOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(SALE_NOT_FOUND).build();
        }
        VehicleSale sale = saleOpt.get();
        if (!isParticipant(sale, callerId)) {
            return GenericResponse.builder().status(ERROR).code("403").message(NOT_A_PARTICIPANT).build();
        }
        VehicleSaleInvoice invoice = sale.getInvoice() != null
                ? sale.getInvoice()
                : vehicleSaleInvoiceRepository.findByVehicleSale_Id(saleId).orElse(null);
        if (invoice == null) {
            return GenericResponse.builder().status(ERROR).code("404").message("Invoice not found").build();
        }
        return GenericResponse.builder().status(SUCCESS).code("200")
                .message("Invoice retrieved successfully")
                .body(vehicleSaleMapper.toInvoiceResponse(invoice)).build();
    }

    @Override
    public GenericResponse getMySales(Integer callerId) {
        List<VehicleSale> sales = vehicleSaleRepository.findByPreviousOwnerIdOrderBySaleDateDesc(callerId);
        return GenericResponse.builder().status(SUCCESS).code("200")
                .message("Sales retrieved successfully")
                .body(sales.stream().map(vehicleSaleMapper::toResponse).toList()).build();
    }

    @Override
    public GenericResponse getMyPurchases(Integer callerId) {
        List<VehicleSale> purchases = vehicleSaleRepository.findByNewOwnerIdOrderBySaleDateDesc(callerId);
        return GenericResponse.builder().status(SUCCESS).code("200")
                .message("Purchases retrieved successfully")
                .body(purchases.stream().map(vehicleSaleMapper::toResponse).toList()).build();
    }

    private boolean isParticipant(VehicleSale sale, Integer callerId) {
        if (callerId == null) {
            return false;
        }
        if (callerId.equals(sale.getPreviousOwnerId()) || callerId.equals(sale.getNewOwnerId())) {
            return true;
        }
        Long companyId = sale.getVehicle() != null ? sale.getVehicle().getCompanyId() : null;
        return companyId != null && companyAuth.isOwner(companyId, callerId);
    }

    private GenericResponse ok(VehicleSale sale) {
        return GenericResponse.builder().status(SUCCESS).code("200")
                .message("Sale retrieved successfully")
                .body(vehicleSaleMapper.toResponse(sale)).build();
    }
}
