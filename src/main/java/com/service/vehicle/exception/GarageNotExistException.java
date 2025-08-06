package com.service.vehicle.exception;

/**
 * throw exception when username not exist
 */
public class GarageNotExistException extends Exception {

	private String message;

	public GarageNotExistException() {
		super();
	}

	public GarageNotExistException(String message) {
		super(message);
		this.message = message;
	}

	public GarageNotExistException(String message, Throwable cause) {
		super(message, cause);
	}
}
