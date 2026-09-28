package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {

    List<Country> findByIsActiveTrue();

    Optional<Country> findByCodeIgnoreCase(String code);
}
