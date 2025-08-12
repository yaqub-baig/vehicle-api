package com.autoadda.apis.vehicle.validator;

import com.autoadda.apis.vehicle.dao.AuthenticationDetailsRepository;
import com.autoadda.apis.vehicle.dao.CustomerRepository;
import com.autoadda.apis.vehicle.dao.GarageRepository;
import com.autoadda.apis.vehicle.entity.AuthenticationDetails;
import com.autoadda.apis.vehicle.entity.Customer;
import com.autoadda.apis.vehicle.entity.Garage;
import com.autoadda.apis.vehicle.exception.CustomerNotExistException;
import com.autoadda.apis.vehicle.exception.GarageNotExistException;
import com.autoadda.apis.vehicle.exception.MemberAlreadyExistException;
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
