package com.platform.profileservice.entity;

import javax.persistence.*;

import com.platform.profileservice.util.PolicyTypeEnum;
import com.platform.profileservice.util.VehicleTypeCategoryEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

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

	@Column(name = "chassis_no")
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

	@Enumerated(EnumType.STRING)
	@Column(name = "vehicle_type_category")
    private VehicleTypeCategoryEnum vehicleTypeCategory;

	@Column(name = "code")
    private String code;

	@Column(name = "state")
    private String state;
	@Column(name = "number")
    private String number;
	@Column(name = "vehicle_color")
    private String vehicleColor;
	@Column(name = "nick_name")
    private String nickName;
	@Column(name = "reg_expiry")
    private LocalDate regExpiry;
	@Column(name = "insu_expiry")
    private LocalDate insuExpiry;
	@Column(name = "insu_policy")
    private String insuPolicy;

	@ManyToOne
	private User customer;

	@OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "insurance_id", referencedColumnName = "id")
    private InsuranceDetails insurance;


	@OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "registration_id", referencedColumnName = "id")
    private VehicleRegistration registration;

/*
    private Integer mulkiaImage;
    private List<Integer> vehicleImages;
*/

}
