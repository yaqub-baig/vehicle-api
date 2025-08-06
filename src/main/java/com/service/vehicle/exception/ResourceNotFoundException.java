package com.service.vehicle.exception;

public class ResourceNotFoundException extends RuntimeException {
    private String message;

    public ResourceNotFoundException() {
        super ( );
    }

    public ResourceNotFoundException(String message) {
        super ( message );
        this.message = message;
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super ( message, cause );
    }
}

