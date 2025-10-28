package com.autoadda.apis.vehicle.response;

import com.autoadda.apis.vehicle.util.Status;
import lombok.Data;

import java.time.LocalDate;

@Data
public class VehicleListingResponse {
    public Integer id;
    private double price;
    private Status status;
    private LocalDate listedDate;
    private String description;
    private VehicleDetailResponse vehicle;
    private String city;
}
