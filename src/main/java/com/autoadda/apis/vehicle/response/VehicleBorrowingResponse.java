package com.autoadda.apis.vehicle.response;

import com.autoadda.apis.vehicle.enums.BorrowingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleBorrowingResponse {

    private Long id;
    private Integer borrowerId;
    private String borrowerName;
    private Integer originalBorrowerId;
    private String originalBorrowerName;
    private Integer vehicleId;
    private String vehicleInfo;
    private Integer ownerId;
    private String ownerName;
    private LocalDateTime borrowingTime;
    private LocalDateTime expectedReturnTime;
    private LocalDateTime actualReturnTime;
    private BorrowingStatus status;
    private Boolean returnConfirmedByBorrower;
    private Boolean returnConfirmedByOwner;
    private List<MediaFileResponse> borrowingImages;
    private List<MediaFileResponse> returnImages;
    private String borrowingPurpose;
    private String returnNotes;
    private String rejectionReason;
    private Boolean keyReturned;
    private Boolean keyReturnedConfirmedByOwner;
    private String returnRejectionReason;
    private LocalDateTime returnInitiatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BorrowingTransferResponse activeTransfer;
    private List<BorrowingTransferResponse> transferHistory;
}
