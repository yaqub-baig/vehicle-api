package com.service.vehicle.service.impl;

import com.service.vehicle.dao.GarageAddressRepository;
import com.service.vehicle.entity.GarageAddress;
import com.service.vehicle.request.GarageAddressRequest;
import com.service.vehicle.service.GarageAddressService;
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
