package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.CarRentalListing;
import com.autoadda.apis.vehicle.request.RentalSearchCriteria;
import org.springframework.data.domain.Page;

public interface CarRentalListingCustomRepository {

    Page<CarRentalListing> findByCriteriaWithPagination(RentalSearchCriteria criteria);
}
