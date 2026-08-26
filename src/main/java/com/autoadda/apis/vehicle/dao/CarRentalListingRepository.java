package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.CarRentalListing;
import com.autoadda.apis.vehicle.enums.RentalListingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarRentalListingRepository extends JpaRepository<CarRentalListing, Long>, CarRentalListingCustomRepository {

    Optional<CarRentalListing> findByIdAndIsDeletedFalse(Long id);

    List<CarRentalListing> findByOwnerIdAndIsDeletedFalseOrderByCreatedAtDesc(Integer ownerId);

    Optional<CarRentalListing> findByVehicleIdAndIsDeletedFalse(Integer vehicleId);

    boolean existsByVehicleIdAndStatusAndIsDeletedFalse(Integer vehicleId, RentalListingStatus status);
}
