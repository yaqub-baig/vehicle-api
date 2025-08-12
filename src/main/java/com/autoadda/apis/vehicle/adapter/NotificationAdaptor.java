package com.autoadda.apis.vehicle.adapter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.autoadda.apis.vehicle.request.NotificationRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;

@Component
public class NotificationAdaptor {
	
	
	@Value("${notification.service.send.email.url}")
	private String notificationServiceUrl;
	
	public GenericResponse callNotificationService(NotificationRequest request) {
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<NotificationRequest> httpRequest = new HttpEntity<>(request);
		GenericResponse response = restTemplate.postForObject(notificationServiceUrl, httpRequest, GenericResponse.class);
		return response;
	}

}
