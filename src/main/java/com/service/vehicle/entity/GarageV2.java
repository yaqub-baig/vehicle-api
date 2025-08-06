package com.service.vehicle.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GarageV2 {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    //m
    @Column(name = "garage_name")
    private String name;
    @Column(name = "garage_brand_name")
    private String garageBrandName;
    @Column(name = "garage_arabic_name")
    private String garageArabicName;
    @Column(name = "trn_number")
    private String trnNumber;
    @Column(name = "website_url")
    private String website;
    //m
    @ManyToOne
    @JoinColumn(name = "garage_logo")
    private MediaFile garageLogo;
    //m
    @OneToMany
    @JoinColumn(name = "garage_trade_license")
    private List<MediaFile> tradeLicenseAttachment = new ArrayList<> ( );
    @OneToMany
    @JoinColumn(name = "garage_vat_certificate")
    private List<MediaFile> vatCertificate = new ArrayList<> ( );

    @Column(name = "phone_number")
    private String phoneNumber;
    //m
    @Column(name = "mobile_number")
    private String mobileNumber;
    @Column(name = "email")
    private String email;
    @ManyToOne
    @JoinColumn(name = "garage_address")
    private GarageAddress garageAddress;
    @Column(name = "brands")
    private String brands;
    @Column(name = "models")
    private String models;
    @OneToMany
    @JoinColumn(name = "garage_service_types")
    private List<ServiceType> serviceTypes = new ArrayList<> ( );
    @OneToMany
    @JoinColumn(name = "garage_images")
    private List<MediaFile> garageImages = new ArrayList<> ( );
    @ManyToOne
    @JoinColumn(name = "garage_owner")
    private User owner;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    private boolean isActive = true;
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now ( );
    }
}
