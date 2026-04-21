package com.autoadda.apis.vehicle.response;

import com.autoadda.apis.vehicle.enums.TransferStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowingTransferResponse {

    private Long id;
    private Long borrowingId;
    private Integer fromBorrowerId;
    private String fromBorrowerName;
    private Integer toBorrowerId;
    private String toBorrowerName;
    private TransferStatus status;
    private String reason;
    private String notes;
    private MediaFileResponse transferImage;
    private LocalDateTime requestedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
