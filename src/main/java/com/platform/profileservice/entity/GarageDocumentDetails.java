package com.platform.profileservice.entity;

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
@Table(name = "garage_document_details")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class GarageDocumentDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "garage_id")
	private Integer garageId;
	
	@Column(name = "file_nme")
	private String fileName;
	
	@Column(name = "extension")
	private String extension;
	
	@Column(name = "byte_str")
	private String byteStrng;
	
	@Column(name = "status")
	private String status;

	@Column(name = "insert_tms")
	private Date insertTms;
}
