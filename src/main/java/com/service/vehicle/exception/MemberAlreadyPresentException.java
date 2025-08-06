package com.service.vehicle.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class MemberAlreadyPresentException extends RuntimeException {
    public MemberAlreadyPresentException(String message) {
        super ( message );
    }
    public MemberAlreadyPresentException(String message, Throwable cause) {
        super ( message ,cause);
    }
}
