package com.autoadda.apis.vehicle.entity;

import java.util.Date;

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
@Table(name = "garage_services")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class GarageServices {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
    private Integer id;
	
	@Column(name = "garage_id")
    private Integer garageId;
	
	@Column(name = "service_id")
    private Integer serviceId;
	
	@Column(name = "active_ind")
    private String activeInd;
	
	@Column(name = "insert_tms")
	private Date insertTms;
}
