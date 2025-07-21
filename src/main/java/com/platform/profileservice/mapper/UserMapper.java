package com.platform.profileservice.mapper;

import com.platform.profileservice.entity.User;
import com.platform.profileservice.response.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User customer) {
        UserResponse response = new UserResponse();
        response.setId(customer.getId() != null ? customer.getId() : 0);
        response.setFirstName(customer.getFirstName() != null ? customer.getFirstName() : "");
        response.setLastName(customer.getLastName() != null ? customer.getLastName() : "");
        response.setEmail(customer.getEmail() != null ? customer.getEmail() : "");
        response.setMobileNumber(customer.getMobileNumber() != null ? customer.getMobileNumber() : "");
        return response;
    }
}
