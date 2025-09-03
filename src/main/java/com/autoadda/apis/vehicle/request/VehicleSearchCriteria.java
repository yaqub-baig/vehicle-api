package com.autoadda.apis.vehicle.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleSearchCriteria {
    private String location;
    private Double price;
    private String brand;
    private String model;
    private Integer year;
    private String globalSearch;
    
    public boolean hasAnyCriteria() {
        return hasLocation() || hasPrice() || hasBrand() || hasModel() || hasYear() || hasGlobalSearch();
    }

    public boolean hasSpecificCriteria() {
        return hasLocation() || hasPrice() || hasBrand() || hasModel() || hasYear();
    }
    
    public boolean hasLocation() {
        return location != null && !location.trim().isEmpty();
    }
    
    public boolean hasPrice() {
        return price != null && price > 0;
    }
    
    public boolean hasBrand() {
        return brand != null && !brand.trim().isEmpty();
    }
    
    public boolean hasModel() {
        return model != null && !model.trim().isEmpty();
    }
    
    public boolean hasYear() {
        return year != null && year > 0;
    }
    public boolean hasGlobalSearch() {
        return globalSearch != null && !globalSearch.trim().isEmpty();
    }
}