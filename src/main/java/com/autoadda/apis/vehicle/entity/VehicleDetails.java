package com.autoadda.apis.vehicle.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.autoadda.apis.vehicle.util.PolicyTypeEnum;
import com.autoadda.apis.vehicle.util.VehicleTypeCategoryEnum;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "vehicle_details")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;
    @Column(name = "brand")
    private String brand;

    @Column(name = "model")
    private String model;

    @Column(name = "year")
    private String year;

    @Column(name = "engine_type")
    private String engineType;

    @Column(name = "engine_no")
    private String engineNumber;

    @Column(name = "chassis_no",unique = true)
    private String chassisNo;

    @Column(name = "origin")
    private String origin;

    @Column(name = "no_of_passenger")
    private String numOfPassenger;

    @Column(name = "vehicle_type")
    private String vehicleType;

    @Column(name = "tc_number")
    private String tcNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "policy_type")
    private PolicyTypeEnum policyType;

    @Column(name = "insu_company")
    private String insuCompany;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type_category")
    private VehicleTypeCategoryEnum vehicleTypeCategory;

    @Column(name = "code")
    private String code;
    @Column(name = "brand_request_reference_id")
    private String brandRequestReferenceId;
    @Column(name = "state")
    private String state;
    @Column(name = "number")
    private String number;
    @Column(name = "vehicle_color")
    private String vehicleColor;
    @Column(name = "nick_name")
    private String nickName;
    @Column(name = "reg_expiry")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate regExpiry;
    @Column(name = "insu_expiry")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate insuExpiry;
    @Column(name = "insu_policy")
    private String insuPolicy;
    @Column(name= "odometer")
    private String odometer;

    @ManyToOne(optional = true, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "mulkia_image_id_front")
    private MediaFile mulkiaImageFront;


    @ManyToOne(optional = true,cascade = CascadeType.REMOVE)
    @JoinColumn(name = "mulkia_image_id_back")
    private MediaFile mulkiaImageBack;

    @OneToMany(orphanRemoval = true,cascade = CascadeType.ALL)
    @JoinColumn(name = "vehicle_images_id")
    private List<MediaFile> vehicleImages;
    @Column(name = "main_image_id")
    private Long mainImageId;
    private Long companyId;
    private Integer customerId;
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "insurance_id", referencedColumnName = "id")
    private InsuranceDetails insurance;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "registration_id", referencedColumnName = "id")
    private VehicleRegistration registration;

    private boolean isDeleted ;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now ( );
        updatedAt = createdAt;
        isDeleted = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now ( );
    }

}
