package com.service.vehicle.service;

import com.service.vehicle.entity.GarageAddress;
import com.service.vehicle.request.GarageAddressRequest;

public interface GarageAddressService {
    GarageAddress createGarageAddress(GarageAddressRequest garageAddress);
}
