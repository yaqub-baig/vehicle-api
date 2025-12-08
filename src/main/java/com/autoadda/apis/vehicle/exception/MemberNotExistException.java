package com.autoadda.apis.vehicle.exception;

/**
 * throw exception when username not exist
 */
public class MemberNotExistException extends Exception {



	public MemberNotExistException() {
		super();
	}

	public MemberNotExistException(String message) {
		super(message);
	}

	public MemberNotExistException(String message, Throwable cause) {
		super(message, cause);
	}
}
