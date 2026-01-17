package com.autoadda.apis.vehicle.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class VehicleOwnerDocumentation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    private MediaFile tradeLicence;
    @ManyToOne
    private MediaFile emiratesId;
    @ManyToOne
    private MediaFile drivingLicence;
    @Column(unique = true,nullable = false)
    private Integer vehicleOwnerId;
}
