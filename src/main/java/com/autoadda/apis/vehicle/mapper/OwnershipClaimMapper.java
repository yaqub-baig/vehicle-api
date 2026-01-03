package com.autoadda.apis.vehicle.mapper;

import com.autoadda.apis.vehicle.clients.AuthClient;
import com.autoadda.apis.vehicle.entity.VehicleOwnershipClaim;
import com.autoadda.apis.vehicle.response.OwnershipClaimResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OwnershipClaimMapper {
    private final AuthClient authClient;
    public OwnershipClaimResponse toResponse(VehicleOwnershipClaim claim) {
        if (claim == null) {
            return null;
        }

        return OwnershipClaimResponse.builder()
                .id(claim.getId())
                .chassisNo(claim.getChassisNo())
                .vehicleId(claim.getVehicleId())
                .requesterId(claim.getRequesterId())
                .currentOwnerId(claim.getCurrentOwnerId())
                .reason(claim.getReason())
                .status(claim.getStatus())
                .requestDate(claim.getRequestDate())
                .responseDate(claim.getResponseDate())
                .responseNotes(claim.getResponseNotes())
                .createdAt(claim.getCreatedAt())
                .updatedAt(claim.getUpdatedAt())
                .currentOwnerName(authClient.getUser(claim.getCurrentOwnerId()).getName())
                .requesterName(authClient.getUser(claim.getRequesterId()).getName())
                .build();

        // Note: requesterName, currentOwnerName, and vehicle details
        // can be populated by calling Identity Service if needed
    }

    public List<OwnershipClaimResponse> toResponseList(List<VehicleOwnershipClaim> claims) {
        if (claims == null) {
            return List.of();
        }

        return claims.stream()
                .map(this::toResponse)
                .toList();
    }
}
