package com.autoadda.apis.vehicle.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuickReturnRequest {

    private List<Long> returnImageIds;

    private String returnNotes;

    private Boolean keyReturned;
}
