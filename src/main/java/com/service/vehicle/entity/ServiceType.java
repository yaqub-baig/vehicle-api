package com.service.vehicle.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "service_type")
public class ServiceType {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "service")
    private String service;

    @Column(name = "dsc")
    private String dsc;

}

