package com.service.vehicle.validator;

import com.service.vehicle.dao.AuthenticationDetailsRepository;
import com.service.vehicle.dao.CustomerRepository;
import com.service.vehicle.dao.GarageRepository;
import com.service.vehicle.entity.AuthenticationDetails;
import com.service.vehicle.entity.Customer;
import com.service.vehicle.entity.Garage;
import com.service.vehicle.exception.CustomerNotExistException;
import com.service.vehicle.exception.GarageNotExistException;
import com.service.vehicle.exception.MemberAlreadyExistException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ServiceValidator {

    private final GarageRepository garageRepository;
    private final CustomerRepository customerRepository;
    private final AuthenticationDetailsRepository authenticationDetailsRepository;

    public void validateGarage(Integer garageId) throws GarageNotExistException {
        Optional<Garage> optGarage = garageRepository.findById ( garageId );
        if ( ! optGarage.isPresent ( ) ) {
            throw new GarageNotExistException ( "Garage not found" );
        }
    }

    public void validateCustomer(Integer customerId) throws CustomerNotExistException {
        Optional<Customer> optGarage = customerRepository.findById ( customerId );
        if ( ! optGarage.isPresent ( ) ) {
            throw new CustomerNotExistException ( "Customer not found" );
        }
    }

    public void validateUsername(String username) throws MemberAlreadyExistException {
        AuthenticationDetails authData = authenticationDetailsRepository.findByUserName ( username );
        if ( null != authData ) {
            throw new MemberAlreadyExistException ( "Username already exist" );
        }
    }


}
