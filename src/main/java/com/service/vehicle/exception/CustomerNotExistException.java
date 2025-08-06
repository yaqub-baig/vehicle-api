package com.service.vehicle.exception;

/**
 * throw exception when username not exist
 */
public class CustomerNotExistException extends Exception {

	private String message;

	public CustomerNotExistException() {
		super();
	}

	public CustomerNotExistException(String message) {
		super(message);
		this.message = message;
	}

	public CustomerNotExistException(String message, Throwable cause) {
		super(message, cause);
	}
}
