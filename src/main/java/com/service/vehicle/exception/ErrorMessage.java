package com.service.vehicle.exception;

public class ErrorMessage {

    // 2xx Success
    public static final String SUCCESS_OK = "Request processed successfully.";
    public static final String SUCCESS_CREATED = "Resource created successfully.";
    public static final String SUCCESS_NO_CONTENT = "Request processed successfully, no content to return.";
   // 4xx Client Errors
    public static final String ERROR_BAD_REQUEST = "The request could not be understood or was missing required parameters.";
    public static final String ERROR_VALIDATION_FAILED = "Validation failed for one or more fields.";
    public static final String ERROR_CONSTRAINT_VIOLATION = "Constraint violation occurred.";
    public static final String ERROR_UNREADABLE_JSON = "Malformed or unreadable request payload.";
    public static final String ERROR_MISSING_PARAMETER = "Missing required request parameter.";
    public static final String ERROR_DATA_INTEGRITY = "Duplicate entry or data constraint violation";
    public static final String ERROR_UNAUTHORIZED = "Authentication is required and has failed or has not yet been provided.";
    public static final String ERROR_FORBIDDEN = "You do not have permission to access the requested resource.";
    public static final String ERROR_NOT_FOUND = "The requested resource was not found.";
    public static final String ERROR_METHOD_NOT_ALLOWED = "The HTTP method is not supported for this resource.";
    public static final String ERROR_CONFLICT = "A conflict occurred with the current state of the resource.";
    public static final String ERROR_UNSUPPORTED_MEDIA_TYPE = "The request entity has a media type which the server or resource does not support.";
    public static final String ERROR_UNPROCESSABLE_ENTITY = "The request was well-formed but was unable to be followed due to semantic errors.";
    // 5xx Server Errors
    public static final String ERROR_INTERNAL_SERVER = "An unexpected internal server error occurred.";
    public static final String ERROR_NOT_IMPLEMENTED = "The server does not support the functionality required to fulfill the request.";
    public static final String ERROR_BAD_GATEWAY = "The server received an invalid response from the upstream server.";
    public static final String ERROR_SERVICE_UNAVAILABLE = "The service is currently unavailable. Please try again later.";
    public static final String ERROR_GATEWAY_TIMEOUT = "The gateway did not receive a timely response from the upstream server.";
 public static final String ERROR_OTP_SENDING_FAILED = "Otp sending failed.";

 private ErrorMessage() {
        // Prevent instantiation
    }

}




