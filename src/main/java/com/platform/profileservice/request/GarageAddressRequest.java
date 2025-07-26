package com.platform.profileservice.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GarageAddressRequest {
    private String country;
    private String state;
    private String area;
    private String address;
    private String gmap;
}
