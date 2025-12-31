package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.AccidentReport;
import com.autoadda.apis.vehicle.util.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccidentReportRepository extends JpaRepository<AccidentReport, Integer> {
    List<AccidentReport> findByVehicleId(Integer vehicleId);
    List<AccidentReport> findByReportedBy(Integer reportedBy);
    List<AccidentReport> findByStatus(Status status);
    List<AccidentReport> findAllByOrderByCreatedAtDesc();
}
