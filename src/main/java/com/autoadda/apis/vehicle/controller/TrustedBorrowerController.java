package com.autoadda.apis.vehicle.controller;

import com.autoadda.apis.vehicle.request.AddTrustedBorrowerRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.TrustedBorrowerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/trusted-borrowers")
@RequiredArgsConstructor
public class TrustedBorrowerController {

    private final TrustedBorrowerService trustedBorrowerService;

    /**
     * Owner adds a borrower to their trusted list, enabling that borrower
     * to use quick-borrow (self-request) on any of the owner's vehicles.
     * POST /trusted-borrowers
     */
    @PostMapping
    public GenericResponse addTrustedBorrower(
            @RequestBody AddTrustedBorrowerRequest request,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        Integer ownerId = Integer.parseInt(userId);
        return trustedBorrowerService.addTrustedBorrower(ownerId, request);
    }

    /**
     * Owner removes a borrower from their trusted list.
     * DELETE /trusted-borrowers/{borrowerId}
     */
    @DeleteMapping("/{borrowerId}")
    public GenericResponse removeTrustedBorrower(
            @PathVariable Integer borrowerId,
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        Integer ownerId = Integer.parseInt(userId);
        return trustedBorrowerService.removeTrustedBorrower(ownerId, borrowerId);
    }

    /**
     * List the caller's trusted borrowers.
     * GET /trusted-borrowers
     */
    @GetMapping
    public GenericResponse getTrustedBorrowers(
            @RequestHeader("X-Auto-Adda-Id") String userId) {
        Integer ownerId = Integer.parseInt(userId);
        return trustedBorrowerService.getTrustedBorrowers(ownerId);
    }
}
