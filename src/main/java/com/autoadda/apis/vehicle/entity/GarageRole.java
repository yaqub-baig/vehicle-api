package com.autoadda.apis.vehicle.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Data
@Table(name = "garage_role")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class GarageRole {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "role_cde")
    private String roleCde;
	
	@Column(name = "dsc")
    private String dsc;
}
