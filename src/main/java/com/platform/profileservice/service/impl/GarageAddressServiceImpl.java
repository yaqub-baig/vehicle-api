package com.platform.profileservice.service.impl;

import com.platform.profileservice.dao.GarageAddressRepository;
import com.platform.profileservice.entity.GarageAddress;
import com.platform.profileservice.request.GarageAddressRequest;
import com.platform.profileservice.service.GarageAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service@RequiredArgsConstructor
public class GarageAddressServiceImpl implements GarageAddressService {
    private final GarageAddressRepository garageAddressRepository;
    @Override
    public GarageAddress createGarageAddress(GarageAddressRequest request) {
        GarageAddress garageAddress = new GarageAddress ( );
        garageAddress.setCountry ( request.getCountry ( ) );
        garageAddress.setState ( request.getState ( ) );
        garageAddress.setArea ( request.getArea ( ) );
        garageAddress.setAddress ( request.getAddress ( ) );
        garageAddress.setGmap ( request.getGmap ( ) );
        return garageAddressRepository.save ( garageAddress );
    }
}
