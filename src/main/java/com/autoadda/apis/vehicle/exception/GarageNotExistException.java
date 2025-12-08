package com.autoadda.apis.vehicle.exception;

/**
 * throw exception when username not exist
 */
public class GarageNotExistException extends Exception {


    public GarageNotExistException() {
        super();
    }

    public GarageNotExistException(String message) {
        super(message);
    }

    public GarageNotExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
