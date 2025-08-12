package com.autoadda.apis.vehicle.exception;

/**
 * throw exception when username not exist
 */
public class MemberNotExistException extends Exception {

	private String message;

	public MemberNotExistException() {
		super();
	}

	public MemberNotExistException(String message) {
		super(message);
		this.message = message;
	}

	public MemberNotExistException(String message, Throwable cause) {
		super(message, cause);
	}
}
