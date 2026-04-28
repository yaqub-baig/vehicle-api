package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.VehicleListing;
import com.autoadda.apis.vehicle.request.VehicleSearchCriteria;
import org.springframework.data.domain.Page;

import java.util.List;

public interface VehicleListingCustomRepository {

    List<VehicleListing> findByCriteria(VehicleSearchCriteria criteria);

    List<VehicleListing> findByAdvancedSearch(VehicleSearchCriteria criteria);

    // Paginated methods
    Page<VehicleListing> findByCriteriaWithPagination(VehicleSearchCriteria criteria);

    Page<VehicleListing> findByAdvancedSearchWithPagination(VehicleSearchCriteria criteria);

}
