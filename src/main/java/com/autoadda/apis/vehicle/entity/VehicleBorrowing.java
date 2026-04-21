package com.autoadda.apis.vehicle.entity;

import com.autoadda.apis.vehicle.enums.BorrowingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_borrowing")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleBorrowing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "borrower_id", nullable = false)
    private Integer borrowerId;

    @Column(name = "original_borrower_id")
    private Integer originalBorrowerId;

    @Column(name = "vehicle_id", nullable = false)
    private Integer vehicleId;

    @Column(name = "owner_id", nullable = false)
    private Integer ownerId;

    @Column(name = "borrowing_time")
    private LocalDateTime borrowingTime;

    @Column(name = "expected_return_time")
    private LocalDateTime expectedReturnTime;

    @Column(name = "actual_return_time")
    private LocalDateTime actualReturnTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BorrowingStatus status;

    @Column(name = "return_confirmed_by_borrower")
    private Boolean returnConfirmedByBorrower;

    @Column(name = "return_confirmed_by_owner")
    private Boolean returnConfirmedByOwner;

    @ManyToOne
    @JoinColumn(name = "borrowing_image_id")
    private MediaFile borrowingImage;

    @ManyToOne
    @JoinColumn(name = "return_image_id")
    private MediaFile returnImage;

    @Column(name = "borrowing_purpose", length = 500)
    private String borrowingPurpose;

    @Column(name = "return_notes", length = 500)
    private String returnNotes;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isDeleted == null) {
            isDeleted = false;
        }
        if (returnConfirmedByBorrower == null) {
            returnConfirmedByBorrower = false;
        }
        if (returnConfirmedByOwner == null) {
            returnConfirmedByOwner = false;
        }
        if (originalBorrowerId == null) {
            originalBorrowerId = borrowerId;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
