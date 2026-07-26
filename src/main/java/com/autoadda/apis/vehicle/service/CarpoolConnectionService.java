package com.autoadda.apis.vehicle.service;

import com.autoadda.apis.vehicle.enums.CarpoolConnectionStatus;
import com.autoadda.apis.vehicle.request.CarpoolConnectionRejectionRequest;
import com.autoadda.apis.vehicle.request.CarpoolConnectionRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;

public interface CarpoolConnectionService {

    GenericResponse createConnectionRequest(CarpoolConnectionRequest request, Integer requesterUserId);

    GenericResponse acceptConnection(Long connectionId, Integer userId);

    GenericResponse rejectConnection(Long connectionId, Integer userId, CarpoolConnectionRejectionRequest request);

    GenericResponse cancelConnection(Long connectionId, Integer userId);

    GenericResponse getConnectionById(Long connectionId, Integer userId);

    GenericResponse getMyConnections(Integer userId, CarpoolConnectionStatus statusFilter);
}
