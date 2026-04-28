package com.autoadda.apis.vehicle.entity;


import com.autoadda.apis.vehicle.util.CarCondition;
import com.autoadda.apis.vehicle.util.Status;
import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleListing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    private VehicleDetails vehicle;
    private double price;
    @Column(length = 2000)
    private String description;
    private String city;
    private LocalDate listedDate;

    @OneToOne(mappedBy = "vehicleListing", cascade = CascadeType.ALL)
    private VehicleSale sale;
    @Enumerated(EnumType.STRING)
    private Status status; // LISTED, SOLD, REMOVED

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type")
    private CarCondition condition;
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now ( );
        updatedAt = createdAt;
        if ( listedDate == null ) {
            listedDate = LocalDate.now ( );
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now ( );
    }


}
