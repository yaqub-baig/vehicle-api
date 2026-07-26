package com.autoadda.apis.vehicle.entity;

import com.autoadda.apis.vehicle.enums.CarpoolConnectionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "carpool_connection")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarpoolConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requester_listing_id", nullable = false)
    private Long requesterListingId;

    @Column(name = "requester_user_id", nullable = false)
    private Integer requesterUserId;

    @Column(name = "target_listing_id", nullable = false)
    private Long targetListingId;

    @Column(name = "target_user_id", nullable = false)
    private Integer targetUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CarpoolConnectionStatus status;

    @Column(length = 500)
    private String message;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

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
        if (status == null) {
            status = CarpoolConnectionStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
