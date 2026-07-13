package com.autoadda.apis.vehicle.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmReturnRequest {

    private Boolean keyReturned;
}
