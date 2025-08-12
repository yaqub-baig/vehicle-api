package com.autoadda.apis.vehicle.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedAccessException extends RuntimeException {
  private String message;
  public UnauthorizedAccessException() {
    super();
  }
  public UnauthorizedAccessException(String message) {
    super(message);
    this.message = message;
  }
  public UnauthorizedAccessException(String message, Throwable cause) {
    super(message, cause);
  }
}


