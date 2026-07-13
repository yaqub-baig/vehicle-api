package com.autoadda.apis.vehicle.entity;

import com.autoadda.apis.vehicle.enums.BorrowingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

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

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "borrowing_images_id")
    @BatchSize(size = 50)
    private List<MediaFile> borrowingImages;

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "return_images_id")
    @BatchSize(size = 50)
    private List<MediaFile> returnImages;

    @Column(name = "borrowing_purpose", length = 500)
    private String borrowingPurpose;

    @Column(name = "return_notes", length = 500)
    private String returnNotes;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "key_returned")
    private Boolean keyReturned;

    @Column(name = "key_returned_confirmed_by_owner")
    private Boolean keyReturnedConfirmedByOwner;

    @Column(name = "return_rejection_reason", length = 500)
    private String returnRejectionReason;

    @Column(name = "return_initiated_at")
    private LocalDateTime returnInitiatedAt;

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
