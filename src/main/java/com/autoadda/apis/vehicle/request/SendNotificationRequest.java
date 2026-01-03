package com.autoadda.apis.vehicle.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationRequest {
    private String userId;
    private String role;  // Optional - if null/empty, send to all roles for the user
    private String title;
    private String body;
    private String url;
}
