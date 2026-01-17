package com.autoadda.apis.vehicle.entity;

import com.autoadda.apis.vehicle.util.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "accident_report")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccidentReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private VehicleDetails vehicle;

    @Column(nullable = false )
    private Integer reportedBy;

    @Column(nullable = false)
    private LocalDate accidentDate;

    @Column
    private String location;

    @Column(length = 2000)
    private String description;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "police_report_id")
    private MediaFile policeReport;

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "accident_report_id")
    private List<MediaFile> accidentImages;

    @Enumerated(EnumType.STRING)
    @Column
    private Status status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (status == null) {
            status = Status.SUBMITTED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
