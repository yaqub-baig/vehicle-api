package com.platform.profileservice.service.impl;

import com.platform.profileservice.dao.UserRepository;
import com.platform.profileservice.dao.VehicleDetailsRepository;
import com.platform.profileservice.entity.User;
import com.platform.profileservice.entity.VehicleDetails;
import com.platform.profileservice.exception.ResourceNotFoundException;
import com.platform.profileservice.mapper.UserMapper;
import com.platform.profileservice.mapper.VehicleMapper;
import com.platform.profileservice.request.VehicleRegistrationRequest;
import com.platform.profileservice.response.GenericResponse;
import com.platform.profileservice.response.VehicleDetailResponse;
import com.platform.profileservice.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {
    private final VehicleDetailsRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;
    private final UserRepository userRepository;

    @Override
    public GenericResponse addVehicleDetails(VehicleRegistrationRequest request, String mobileNumber) {
        User user = userRepository.findByMobileNumber ( mobileNumber ).orElseThrow ( () -> new ResourceNotFoundException ( "User not found" ) );
        VehicleDetails entity = vehicleMapper.toEntity ( request );
        entity.setCustomer ( user );
        vehicleRepository.save ( entity );
        return GenericResponse.builder ( )
                              .status ( "success" )
                              .code ( "200" )
                              .message ( "Vehicle added successfully" )
                              .build ( );
    }

    @Override
    public GenericResponse getVehicleDetails(String userId) {
        List<VehicleDetails> byCustomerMobileNumber = vehicleRepository.findByCustomerMobileNumber ( userId );
        List<VehicleDetailResponse> response =byCustomerMobileNumber.stream ( )
                                             .map ( vehicleMapper::toResponse )
                                             .toList ( );
        return GenericResponse.builder ( )
                              .body ( response )
                              .code ( "200" )
                              .message ( "success" )
                              .status ( "success" )
                              .build ();
    }
}
