package com.autoadda.apis.vehicle.exception;

/**
 * throw exception when username not exist
 */
public class InvalidLoginException extends Exception {



	public InvalidLoginException() {
		super();
	}

	public InvalidLoginException(String message) {
		super(message);

	}

	public InvalidLoginException(String message, Throwable cause) {
		super(message, cause);
	}
}
