package com.autoadda.apis.vehicle.exception;

/**
 * throw exception when username not exist
 */
public class CustomerNotExistException extends Exception {


    public CustomerNotExistException() {
        super();
    }

    public CustomerNotExistException(String message) {
        super(message);
    }

    public CustomerNotExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
