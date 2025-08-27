package com.autoadda.apis.vehicle.entity;

import javax.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleSaleInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDate invoiceDate;

    private double totalAmount;

    private String paymentMethod;

    @OneToOne
    @JoinColumn(name = "vehicle_sale_id")
    private VehicleSale vehicleSale;
}
