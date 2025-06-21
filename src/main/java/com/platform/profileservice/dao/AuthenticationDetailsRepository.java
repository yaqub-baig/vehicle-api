package com.platform.profileservice.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.platform.profileservice.entity.AuthenticationDetails;

@Repository
public interface AuthenticationDetailsRepository extends JpaRepository<AuthenticationDetails, Integer>{

	AuthenticationDetails findByUserName(String username);

}
