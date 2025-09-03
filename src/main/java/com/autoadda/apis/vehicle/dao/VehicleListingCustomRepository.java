package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.VehicleListing;
import com.autoadda.apis.vehicle.request.VehicleSearchCriteria;

import java.util.List;

public interface VehicleListingCustomRepository {

    List<VehicleListing> findByCriteria(VehicleSearchCriteria criteria);

    List<VehicleListing> findByAdvancedSearch(String searchQuery);

}
