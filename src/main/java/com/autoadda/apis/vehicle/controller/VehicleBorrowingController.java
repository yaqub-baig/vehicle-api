package com.autoadda.apis.vehicle.controller;

import com.autoadda.apis.vehicle.request.BorrowingRejectionRequest;
import com.autoadda.apis.vehicle.request.BorrowingReturnRequest;
import com.autoadda.apis.vehicle.request.BorrowingTransferRequest;
import com.autoadda.apis.vehicle.request.OwnerInitiatedBorrowingRequest;
import com.autoadda.apis.vehicle.request.TransferRejectionRequest;
import com.autoadda.apis.vehicle.request.VehicleBorrowingRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.VehicleBorrowingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("/vehicle-borrowings")
@RequiredArgsConstructor
public class VehicleBorrowingController {

    private final VehicleBorrowingService borrowingService;

    /**
     * Create a new borrowing request
     * POST /vehicle-borrowings
     */
    @PostMapping
    public GenericResponse createBorrowingRequest(
            @RequestBody VehicleBorrowingRequest request,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        Integer borrowerId = Integer.parseInt(userId);
        return borrowingService.createBorrowingRequest(request, borrowerId);
    }

    /**
     * Owner directly assigns vehicle to a user (skips approval workflow)
     * POST /vehicle-borrowings/assign
     */
    @PostMapping("/assign")
    public GenericResponse assignVehicleToBorrower(
            @RequestBody OwnerInitiatedBorrowingRequest request,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        Integer ownerId = Integer.parseInt(userId);
        return borrowingService.createOwnerInitiatedBorrowing(request, ownerId);
    }

    /**
     * Search for users by AutoAdda ID (for owner to find borrower)
     * GET /vehicle-borrowings/search-user?userId={userId}
     */
    @GetMapping("/search-user")
    public GenericResponse searchUserById (@RequestParam(required = true) Integer userId ) {
        return borrowingService.searchUserById(userId);
    }

    /**
     * Owner approves a borrowing request
     * PUT /vehicle-borrowings/{id}/approve
     */
    @PutMapping("/{id}/approve")
    public GenericResponse approveBorrowingRequest(
            @PathVariable Long id,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        Integer ownerId = Integer.parseInt(userId);
        return borrowingService.approveBorrowingRequest(id, ownerId);
    }

    /**
     * Owner rejects a borrowing request
     * PUT /vehicle-borrowings/{id}/reject
     */
    @PutMapping("/{id}/reject")
    public GenericResponse rejectBorrowingRequest(
            @PathVariable Long id,
            @RequestHeader("X-Auto-Adda-Id") String userId,
            @RequestBody(required = false) BorrowingRejectionRequest request) {
        Integer ownerId = Integer.parseInt(userId);
        return borrowingService.rejectBorrowingRequest(id, ownerId, request);
    }

    /**
     * Borrower starts the borrowing (picks up vehicle)
     * PUT /vehicle-borrowings/{id}/start
     */
    @PutMapping("/{id}/start")
    public GenericResponse startBorrowing(
            @PathVariable Long id,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        Integer borrowerId = Integer.parseInt(userId);
        return borrowingService.startBorrowing(id, borrowerId);
    }

    /**
     * Borrower initiates return
     * PUT /vehicle-borrowings/{id}/return
     */
    @PutMapping("/{id}/return")
    public GenericResponse initiateReturn(
            @PathVariable Long id,
            @RequestHeader("X-Auto-Adda-Id") String userId,
            @RequestBody(required = false) BorrowingReturnRequest request) {
        Integer borrowerId = Integer.parseInt(userId);
        return borrowingService.initiateReturn(id, borrowerId, request);
    }

    /**
     * Either party confirms return
     * PUT /vehicle-borrowings/{id}/confirm-return
     */
    @PutMapping("/{id}/confirm-return")
    public GenericResponse confirmReturn(
            @PathVariable Long id,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        Integer confirmerId = Integer.parseInt(userId);
        return borrowingService.confirmReturn(id, confirmerId);
    }

    /**
     * Borrower cancels request
     * PUT /vehicle-borrowings/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public GenericResponse cancelBorrowingRequest(
            @PathVariable Long id,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        Integer borrowerId = Integer.parseInt(userId);
        return borrowingService.cancelBorrowingRequest(id, borrowerId);
    }

    /**
     * Get borrowing by ID
     * GET /vehicle-borrowings/{id}
     */
    @GetMapping("/{id}")
    public GenericResponse getBorrowingById(@PathVariable Long id) {
        return borrowingService.getBorrowingById(id);
    }

    /**
     * Get borrowings by vehicle
     * GET /vehicle-borrowings/vehicle/{vehicleId}
     */
    @GetMapping("/vehicle/{vehicleId}")
    public GenericResponse getBorrowingsByVehicle(@PathVariable Integer vehicleId) {
        return borrowingService.getBorrowingsByVehicle(vehicleId);
    }

    /**
     * Get borrowings as borrower
     * GET /vehicle-borrowings/borrower/{borrowerId}
     */
    @GetMapping("/borrower/{borrowerId}")
    public GenericResponse getBorrowingsByBorrower(@PathVariable Integer borrowerId) {
        return borrowingService.getBorrowingsByBorrower(borrowerId);
    }

    /**
     * Get my borrowings (as borrower)
     * GET /vehicle-borrowings/my-borrowings
     */
    @GetMapping("/my-borrowings")
    public GenericResponse getMyBorrowings(
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        Integer borrowerId = Integer.parseInt(userId);
        return borrowingService.getBorrowingsByBorrower(borrowerId);
    }

    /**
     * Get borrowings as owner
     * GET /vehicle-borrowings/owner/{ownerId}
     */
    @GetMapping("/owner/{ownerId}")
    public GenericResponse getBorrowingsByOwner(@PathVariable Integer ownerId) {
        return borrowingService.getBorrowingsByOwner(ownerId);
    }

    /**
     * Get borrowings of my vehicles (as owner)
     * GET /vehicle-borrowings/my-vehicles
     */
    @GetMapping("/my-vehicles")
    public GenericResponse getMyVehicleBorrowings(
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        Integer ownerId = Integer.parseInt(userId);
        return borrowingService.getBorrowingsByOwner(ownerId);
    }

    /**
     * Get pending borrowing requests for owner
     * GET /vehicle-borrowings/pending
     */
    @GetMapping("/pending")
    public GenericResponse getPendingBorrowings(
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        Integer ownerId = Integer.parseInt(userId);
        return borrowingService.getPendingBorrowingsForOwner(ownerId);
    }

    /**
     * Get borrowings for all vehicles of a company
     * GET /vehicle-borrowings/company/{companyId}
     */
    @GetMapping("/company/{companyId}")
    public GenericResponse getBorrowingsByCompany(@PathVariable Long companyId) {
        return borrowingService.getBorrowingsByCompany(companyId);
    }

    /**
     * Get active borrowing for a vehicle
     * GET /vehicle-borrowings/vehicle/{vehicleId}/active
     */
    @GetMapping("/vehicle/{vehicleId}/active")
    public GenericResponse getActiveBorrowingForVehicle(@PathVariable Integer vehicleId) {
        return borrowingService.getActiveBorrowingForVehicle(vehicleId);
    }

    /**
     * Check vehicle availability for borrowing
     * GET /vehicle-borrowings/vehicle/{vehicleId}/available
     */
    @GetMapping("/vehicle/{vehicleId}/available")
    public GenericResponse checkVehicleAvailability(@PathVariable Integer vehicleId) {
        boolean available = borrowingService.isVehicleAvailableForBorrowing(vehicleId);
        return GenericResponse.builder()
                .status("success")
                .code("200")
                .message(available ? "Vehicle is available for borrowing" : "Vehicle is not available for borrowing")
                .body(Map.of("available", available))
                .build();
    }

    /**
     * Current borrower proposes handover to another user
     * POST /vehicle-borrowings/{id}/transfer
     */
    @PostMapping("/{id}/transfer")
    public GenericResponse initiateTransfer(
            @PathVariable Long id,
            @RequestBody BorrowingTransferRequest request,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        Integer fromBorrowerId = Integer.parseInt(userId);
        return borrowingService.initiateTransfer(id, fromBorrowerId, request);
    }

    /**
     * Recipient accepts a pending transfer
     * PUT /vehicle-borrowings/{id}/transfers/{tid}/accept
     */
    @PutMapping("/{id}/transfers/{tid}/accept")
    public GenericResponse acceptTransfer(
            @PathVariable Long id,
            @PathVariable Long tid,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        Integer toBorrowerId = Integer.parseInt(userId);
        return borrowingService.acceptTransfer(id, tid, toBorrowerId);
    }

    /**
     * Recipient rejects a pending transfer
     * PUT /vehicle-borrowings/{id}/transfers/{tid}/reject
     */
    @PutMapping("/{id}/transfers/{tid}/reject")
    public GenericResponse rejectTransfer(
            @PathVariable Long id,
            @PathVariable Long tid,
            @RequestHeader("X-Auto-Adda-Id") String userId,
            @RequestBody(required = false) TransferRejectionRequest request) {
        Integer toBorrowerId = Integer.parseInt(userId);
        return borrowingService.rejectTransfer(id, tid, toBorrowerId, request);
    }

    /**
     * Requesting borrower cancels a pending transfer
     * PUT /vehicle-borrowings/{id}/transfers/{tid}/cancel
     */
    @PutMapping("/{id}/transfers/{tid}/cancel")
    public GenericResponse cancelTransfer(
            @PathVariable Long id,
            @PathVariable Long tid,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        Integer fromBorrowerId = Integer.parseInt(userId);
        return borrowingService.cancelTransfer(id, tid, fromBorrowerId);
    }

    /**
     * Full transfer chain for a borrowing
     * GET /vehicle-borrowings/{id}/transfers
     */
    @GetMapping("/{id}/transfers")
    public GenericResponse getTransfers(@PathVariable Long id) {
        return borrowingService.getTransfersForBorrowing(id);
    }
}
