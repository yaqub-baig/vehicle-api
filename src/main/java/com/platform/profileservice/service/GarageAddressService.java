package com.platform.profileservice.service;

import com.platform.profileservice.entity.GarageAddress;
import com.platform.profileservice.request.GarageAddressRequest;

public interface GarageAddressService {
    GarageAddress createGarageAddress(GarageAddressRequest garageAddress);
}
