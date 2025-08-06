package com.service.vehicle.validator;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.service.vehicle.dao.AuthenticationDetailsRepository;
import com.service.vehicle.dao.CustomerRepository;
import com.service.vehicle.dao.GarageRepository;
import com.service.vehicle.entity.AuthenticationDetails;
import com.service.vehicle.entity.Customer;
import com.service.vehicle.entity.Garage;
import com.service.vehicle.exception.CustomerNotExistException;
import com.service.vehicle.exception.GarageNotExistException;
import com.service.vehicle.exception.MemberAlreadyExistException;

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
