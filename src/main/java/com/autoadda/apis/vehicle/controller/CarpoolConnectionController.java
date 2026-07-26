package com.autoadda.apis.vehicle.controller;

import com.autoadda.apis.vehicle.enums.CarpoolConnectionStatus;
import com.autoadda.apis.vehicle.request.CarpoolConnectionRejectionRequest;
import com.autoadda.apis.vehicle.request.CarpoolConnectionRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.CarpoolConnectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/carpool-connections")
@RequiredArgsConstructor
public class CarpoolConnectionController {

    private final CarpoolConnectionService connectionService;

    /**
     * Send a connect request from one of my listings to a compatible listing
     * POST /carpool-connections
     */
    @PostMapping
    public GenericResponse connect(
            @Valid @RequestBody CarpoolConnectionRequest request,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return connectionService.createConnectionRequest(request, Integer.parseInt(userId));
    }

    /**
     * Recipient accepts a pending connect request; reveals contact info to both parties
     * PUT /carpool-connections/{id}/accept
     */
    @PutMapping("/{id}/accept")
    public GenericResponse accept(
            @PathVariable Long id,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return connectionService.acceptConnection(id, Integer.parseInt(userId));
    }

    /**
     * Recipient rejects a pending connect request
     * PUT /carpool-connections/{id}/reject
     */
    @PutMapping("/{id}/reject")
    public GenericResponse reject(
            @PathVariable Long id,
            @RequestHeader("X-Auto-Adda-Id") String userId,
            @RequestBody(required = false) CarpoolConnectionRejectionRequest request) {
        return connectionService.rejectConnection(id, Integer.parseInt(userId), request);
    }

    /**
     * Requester cancels a pending connect request
     * PUT /carpool-connections/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public GenericResponse cancel(
            @PathVariable Long id,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return connectionService.cancelConnection(id, Integer.parseInt(userId));
    }

    /**
     * Get a connection by ID (participants only)
     * GET /carpool-connections/{id}
     */
    @GetMapping("/{id}")
    public GenericResponse getById(
            @PathVariable Long id,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        return connectionService.getConnectionById(id, Integer.parseInt(userId));
    }

    /**
     * Get my connections (as requester or target), optionally filtered by status
     * GET /carpool-connections/my-connections?status=PENDING
     */
    @GetMapping("/my-connections")
    public GenericResponse myConnections(
            @RequestHeader("X-Auto-Adda-Id") String userId,
            @RequestParam(required = false) CarpoolConnectionStatus status) {
        return connectionService.getMyConnections(Integer.parseInt(userId), status);
    }
}
