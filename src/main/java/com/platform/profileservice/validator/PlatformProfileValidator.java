package com.platform.profileservice.validator;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.platform.profileservice.dao.AuthenticationDetailsRepository;
import com.platform.profileservice.dao.CustomerRepository;
import com.platform.profileservice.dao.GarageRepository;
import com.platform.profileservice.entity.AuthenticationDetails;
import com.platform.profileservice.entity.Customer;
import com.platform.profileservice.entity.Garage;
import com.platform.profileservice.exception.CustomerNotExistException;
import com.platform.profileservice.exception.GarageNotExistException;
import com.platform.profileservice.exception.MemberAlreadyExistException;

@Component
public class PlatformProfileValidator {

	
	@Autowired
	private GarageRepository garageRepository;
	
	@Autowired
	private CustomerRepository customerRepository;
	
	@Autowired
	private AuthenticationDetailsRepository authenticationDetailsRepository;
	
	public void validateGarage(Integer garageId) throws GarageNotExistException {
		Optional<Garage> optGarage = garageRepository.findById(garageId);
		if(!optGarage.isPresent()) {
			throw new GarageNotExistException("Garage not found");
		}
	}
	
	public void validateCustomer(Integer customerId) throws CustomerNotExistException {
		Optional<Customer> optGarage = customerRepository.findById(customerId);
		if(!optGarage.isPresent()) {
			throw new CustomerNotExistException("Customer not found");
		}
	}
	
	public void validateUsername(String username) throws MemberAlreadyExistException {
		AuthenticationDetails authData = authenticationDetailsRepository.findByUserName(username);
		if(null != authData) {
			throw new MemberAlreadyExistException("Username already exist");
		}
	}
	
	
}
