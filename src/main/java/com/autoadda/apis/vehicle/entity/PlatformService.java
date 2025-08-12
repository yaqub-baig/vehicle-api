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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "platform_service")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlatformService {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "service")
	private String service;
	
	@Column(name = "dsc")
	private String dsc;
	
	@Column(name = "category_id")
	private Integer categoryId;
	
	@Column(name = "insert_tms")
	private Date insertTms;

}
