package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.VehicleSaleInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleSaleInvoiceRepository extends JpaRepository<VehicleSaleInvoice,Integer> {

    Optional<VehicleSaleInvoice> findByVehicleSale_Id(Integer vehicleSaleId);
}
