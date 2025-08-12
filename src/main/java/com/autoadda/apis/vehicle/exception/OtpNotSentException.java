package com.autoadda.apis.vehicle.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class OtpNotSentException extends RuntimeException{
    public OtpNotSentException(String message) {
        super(message);
    }
}
