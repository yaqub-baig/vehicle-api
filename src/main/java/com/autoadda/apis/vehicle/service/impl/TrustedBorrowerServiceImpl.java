package com.autoadda.apis.vehicle.service.impl;

import com.autoadda.apis.vehicle.clients.AuthClient;
import com.autoadda.apis.vehicle.dao.TrustedBorrowerRepository;
import com.autoadda.apis.vehicle.entity.TrustedBorrower;
import com.autoadda.apis.vehicle.request.AddTrustedBorrowerRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.response.TrustedBorrowerResponse;
import com.autoadda.apis.vehicle.response.UserResponse;
import com.autoadda.apis.vehicle.service.TrustedBorrowerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrustedBorrowerServiceImpl implements TrustedBorrowerService {

    private static final String ERROR = "error";
    private static final String SUCCESS = "success";

    private final TrustedBorrowerRepository trustedBorrowerRepository;
    private final AuthClient authClient;

    @Override
    @Transactional
    public GenericResponse addTrustedBorrower(Integer ownerId, AddTrustedBorrowerRequest request) {
        log.info("Owner {} adding trusted borrower {}", ownerId, request.getBorrowerId());

        Integer borrowerId = request.getBorrowerId();

        if (borrowerId.equals(ownerId)) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("400")
                    .message("Cannot add yourself as a trusted borrower")
                    .build();
        }

        try {
            UserResponse borrower = authClient.getUser(borrowerId);
            if (borrower == null) {
                return GenericResponse.builder()
                        .status(ERROR)
                        .code("404")
                        .message("Borrower not found")
                        .build();
            }
        } catch (Exception e) {
            log.error("Error fetching borrower {}: {}", borrowerId, e.getMessage());
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("404")
                    .message("Borrower not found")
                    .build();
        }

        Optional<TrustedBorrower> existingOpt = trustedBorrowerRepository.findByOwnerIdAndBorrowerId(ownerId, borrowerId);

        if (existingOpt.isPresent()) {
            TrustedBorrower existing = existingOpt.get();
            if (Boolean.FALSE.equals(existing.getIsDeleted())) {
                return GenericResponse.builder()
                        .status(ERROR)
                        .code("400")
                        .message("This user is already a trusted borrower")
                        .build();
            }
            existing.setIsDeleted(false);
            TrustedBorrower saved = trustedBorrowerRepository.save(existing);
            return GenericResponse.builder()
                    .status(SUCCESS)
                    .code("200")
                    .message("Trusted borrower added")
                    .body(toResponse(saved))
                    .build();
        }

        TrustedBorrower trustedBorrower = TrustedBorrower.builder()
                .ownerId(ownerId)
                .borrowerId(borrowerId)
                .isDeleted(false)
                .build();
        TrustedBorrower saved = trustedBorrowerRepository.save(trustedBorrower);

        log.info("Trusted borrower {} added for owner {}", borrowerId, ownerId);

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("201")
                .message("Trusted borrower added")
                .body(toResponse(saved))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse removeTrustedBorrower(Integer ownerId, Integer borrowerId) {
        log.info("Owner {} removing trusted borrower {}", ownerId, borrowerId);

        Optional<TrustedBorrower> existingOpt = trustedBorrowerRepository.findByOwnerIdAndBorrowerId(ownerId, borrowerId);
        if (existingOpt.isEmpty() || Boolean.TRUE.equals(existingOpt.get().getIsDeleted())) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("404")
                    .message("Trusted borrower not found")
                    .build();
        }

        TrustedBorrower existing = existingOpt.get();
        existing.setIsDeleted(true);
        trustedBorrowerRepository.save(existing);

        log.info("Trusted borrower {} removed for owner {}", borrowerId, ownerId);

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Trusted borrower removed")
                .build();
    }

    @Override
    public GenericResponse getTrustedBorrowers(Integer ownerId) {
        log.info("Fetching trusted borrowers for owner {}", ownerId);

        List<TrustedBorrower> trustedBorrowers =
                trustedBorrowerRepository.findByOwnerIdAndIsDeletedFalseOrderByCreatedAtDesc(ownerId);

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Trusted borrowers retrieved successfully")
                .body(trustedBorrowers.stream().map(this::toResponse).toList())
                .build();
    }

    private TrustedBorrowerResponse toResponse(TrustedBorrower entity) {
        TrustedBorrowerResponse.TrustedBorrowerResponseBuilder builder = TrustedBorrowerResponse.builder()
                .id(entity.getId())
                .ownerId(entity.getOwnerId())
                .borrowerId(entity.getBorrowerId())
                .createdAt(entity.getCreatedAt());

        try {
            UserResponse borrower = authClient.getUser(entity.getBorrowerId());
            if (borrower != null) {
                builder.borrowerName(borrower.getName());
                builder.borrowerMobileNumber(borrower.getMobileNumber());
            }
        } catch (Exception e) {
            log.warn("Failed to fetch borrower details for ID {}: {}", entity.getBorrowerId(), e.getMessage());
        }

        return builder.build();
    }
}
