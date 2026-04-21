package com.autoadda.apis.vehicle.mapper;

import com.autoadda.apis.vehicle.clients.AuthClient;
import com.autoadda.apis.vehicle.dao.BorrowingTransferRepository;
import com.autoadda.apis.vehicle.dao.VehicleDetailsRepository;
import com.autoadda.apis.vehicle.entity.BorrowingTransfer;
import com.autoadda.apis.vehicle.entity.MediaFile;
import com.autoadda.apis.vehicle.entity.VehicleBorrowing;
import com.autoadda.apis.vehicle.entity.VehicleDetails;
import com.autoadda.apis.vehicle.enums.BorrowingStatus;
import com.autoadda.apis.vehicle.enums.TransferStatus;
import com.autoadda.apis.vehicle.request.VehicleBorrowingRequest;
import com.autoadda.apis.vehicle.response.BorrowingTransferResponse;
import com.autoadda.apis.vehicle.response.UserResponse;
import com.autoadda.apis.vehicle.response.VehicleBorrowingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class VehicleBorrowingMapper {

    private final AuthClient authClient;
    private final VehicleDetailsRepository vehicleRepository;
    private final MediaFileMapper mediaFileMapper;
    private final BorrowingTransferRepository transferRepository;

    public VehicleBorrowingResponse toResponse(VehicleBorrowing entity) {
        if (entity == null) {
            return null;
        }

        VehicleBorrowingResponse.VehicleBorrowingResponseBuilder builder = VehicleBorrowingResponse.builder()
                .id(entity.getId())
                .borrowerId(entity.getBorrowerId())
                .originalBorrowerId(entity.getOriginalBorrowerId())
                .vehicleId(entity.getVehicleId())
                .ownerId(entity.getOwnerId())
                .borrowingTime(entity.getBorrowingTime())
                .expectedReturnTime(entity.getExpectedReturnTime())
                .actualReturnTime(entity.getActualReturnTime())
                .status(entity.getStatus())
                .returnConfirmedByBorrower(entity.getReturnConfirmedByBorrower())
                .returnConfirmedByOwner(entity.getReturnConfirmedByOwner())
                .borrowingPurpose(entity.getBorrowingPurpose())
                .returnNotes(entity.getReturnNotes())
                .rejectionReason(entity.getRejectionReason())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        try {
            UserResponse borrower = authClient.getUser(entity.getBorrowerId());
            if (borrower != null) {
                builder.borrowerName(borrower.getName());
            }
        } catch (Exception e) {
            log.warn("Failed to fetch borrower details for ID {}: {}", entity.getBorrowerId(), e.getMessage());
        }

        if (entity.getOriginalBorrowerId() != null
                && !entity.getOriginalBorrowerId().equals(entity.getBorrowerId())) {
            try {
                UserResponse originalBorrower = authClient.getUser(entity.getOriginalBorrowerId());
                if (originalBorrower != null) {
                    builder.originalBorrowerName(originalBorrower.getName());
                }
            } catch (Exception e) {
                log.warn("Failed to fetch original borrower details for ID {}: {}",
                        entity.getOriginalBorrowerId(), e.getMessage());
            }
        }

        if (entity.getId() != null) {
            try {
                List<BorrowingTransfer> transfers = transferRepository
                        .findByBorrowingIdAndIsDeletedFalseOrderByCreatedAtAsc(entity.getId());
                if (!transfers.isEmpty()) {
                    builder.transferHistory(transfers.stream().map(this::toTransferResponse).toList());
                    transfers.stream()
                            .filter(t -> t.getStatus() == TransferStatus.PENDING)
                            .findFirst()
                            .ifPresent(t -> builder.activeTransfer(toTransferResponse(t)));
                }
            } catch (Exception e) {
                log.warn("Failed to load transfer history for borrowing {}: {}", entity.getId(), e.getMessage());
            }
        }

        try {
            UserResponse owner = authClient.getUser(entity.getOwnerId());
            if (owner != null) {
                builder.ownerName(owner.getName());
            }
        } catch (Exception e) {
            log.warn("Failed to fetch owner details for ID {}: {}", entity.getOwnerId(), e.getMessage());
        }

        try {
            Optional<VehicleDetails> vehicleOpt = vehicleRepository.findById(entity.getVehicleId());
            if (vehicleOpt.isPresent()) {
                VehicleDetails vehicle = vehicleOpt.get();
                String vehicleInfo = String.format("%s %s - %s",
                        vehicle.getBrand() != null ? vehicle.getBrand() : "",
                        vehicle.getModel() != null ? vehicle.getModel() : "",
                        vehicle.getNumber() != null ? vehicle.getNumber() : "");
                builder.vehicleInfo(vehicleInfo.trim());
            }
        } catch (Exception e) {
            log.warn("Failed to fetch vehicle details for ID {}: {}", entity.getVehicleId(), e.getMessage());
        }

        if (entity.getBorrowingImage() != null) {
            builder.borrowingImage(mediaFileMapper.toResponse(entity.getBorrowingImage()));
        }

        if (entity.getReturnImage() != null) {
            builder.returnImage(mediaFileMapper.toResponse(entity.getReturnImage()));
        }

        return builder.build();
    }

    public List<VehicleBorrowingResponse> toResponseList(List<VehicleBorrowing> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }

    public VehicleBorrowing toEntity(VehicleBorrowingRequest request, Integer borrowerId,
                                      Integer ownerId, MediaFile borrowingImage) {
        return VehicleBorrowing.builder()
                .borrowerId(borrowerId)
                .originalBorrowerId(borrowerId)
                .vehicleId(request.getVehicleId())
                .ownerId(ownerId)
                .expectedReturnTime(request.getExpectedReturnTime())
                .borrowingImage(borrowingImage)
                .borrowingPurpose(request.getBorrowingPurpose())
                .status(BorrowingStatus.PENDING)
                .returnConfirmedByBorrower(false)
                .returnConfirmedByOwner(false)
                .isDeleted(false)
                .build();
    }

    public BorrowingTransferResponse toTransferResponse(BorrowingTransfer entity) {
        if (entity == null) {
            return null;
        }

        BorrowingTransferResponse.BorrowingTransferResponseBuilder builder = BorrowingTransferResponse.builder()
                .id(entity.getId())
                .borrowingId(entity.getBorrowingId())
                .fromBorrowerId(entity.getFromBorrowerId())
                .toBorrowerId(entity.getToBorrowerId())
                .status(entity.getStatus())
                .reason(entity.getReason())
                .notes(entity.getNotes())
                .requestedAt(entity.getRequestedAt())
                .resolvedAt(entity.getResolvedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        try {
            UserResponse from = authClient.getUser(entity.getFromBorrowerId());
            if (from != null) {
                builder.fromBorrowerName(from.getName());
            }
        } catch (Exception e) {
            log.warn("Failed to fetch from-borrower {} for transfer {}: {}",
                    entity.getFromBorrowerId(), entity.getId(), e.getMessage());
        }

        try {
            UserResponse to = authClient.getUser(entity.getToBorrowerId());
            if (to != null) {
                builder.toBorrowerName(to.getName());
            }
        } catch (Exception e) {
            log.warn("Failed to fetch to-borrower {} for transfer {}: {}",
                    entity.getToBorrowerId(), entity.getId(), e.getMessage());
        }

        if (entity.getTransferImage() != null) {
            builder.transferImage(mediaFileMapper.toResponse(entity.getTransferImage()));
        }

        return builder.build();
    }
}
