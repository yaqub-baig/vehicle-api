package com.platform.profileservice.exception;

/**
 * throw exception when username already exist
 */
public class MemberAlreadyExistException extends Exception {

	private String message;

	public MemberAlreadyExistException() {
		super();
	}

	public MemberAlreadyExistException(String message) {
		super(message);
		this.message = message;
	}

	public MemberAlreadyExistException(String message, Throwable cause) {
		super(message, cause);
	}
}
