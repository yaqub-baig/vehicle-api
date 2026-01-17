package com.autoadda.apis.vehicle.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private Integer companyOwnerId;
    private String tradeLicenseNumber;
    private String tradeLicenseExpiry;
    private Boolean isActive;

    @ManyToOne
    private MediaFile profileImage;
}
