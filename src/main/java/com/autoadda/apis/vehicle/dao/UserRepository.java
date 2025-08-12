package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByMobileNumber(String mobileNumber);

}
