package com.autoadda.apis.vehicle.entity;

import com.autoadda.apis.vehicle.enums.CompanyType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

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

    // A company can be more than one kind of business at once (e.g. a used-car dealer
    // that also runs a rental fleet). Every company has at least one type; existing rows
    // are backfilled to FLEET_OWNER.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "company_type", joinColumns = @JoinColumn(name = "company_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 40)
    private Set<CompanyType> types = new HashSet<>();

    // Brands a NEW_CAR_SHOWROOM is authorized to sell (e.g. "Mercedes-Benz", "BMW").
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "company_authorized_brand", joinColumns = @JoinColumn(name = "company_id"))
    @Column(name = "brand", nullable = false, length = 100)
    private Set<String> authorizedBrands = new HashSet<>();

    @Column(name = "showroom_address", length = 500)
    private String showroomAddress;

    @Column(name = "rta_approval_number", length = 100)
    private String rtaApprovalNumber;
}
