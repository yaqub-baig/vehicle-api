package com.autoadda.apis.vehicle.service;

import com.autoadda.apis.vehicle.entity.GarageAddress;
import com.autoadda.apis.vehicle.request.GarageAddressRequest;

public interface GarageAddressService {
    GarageAddress createGarageAddress(GarageAddressRequest garageAddress);
}
