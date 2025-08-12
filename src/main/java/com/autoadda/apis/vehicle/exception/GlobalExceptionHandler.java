package com.autoadda.apis.vehicle.exception;

import com.autoadda.apis.vehicle.response.GenericResponse;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.persistence.EntityNotFoundException;
import java.nio.file.AccessDeniedException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponse> handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult ( ).getFieldErrors ( ).stream ( ).map ( err -> err.getField ( ) + ": " + err.getDefaultMessage ( ) ).collect ( Collectors.joining ( "; " ) );
        log.warn ( "Validation failed: {}", errors );
        return buildResponse ( ex, HttpStatus.BAD_REQUEST, ErrorMessage.ERROR_VALIDATION_FAILED + ": " + errors );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<GenericResponse> handleConstraintViolation(ConstraintViolationException ex) {
        log.warn ( "Constraint violation: {}", ex.getMessage ( ) );
        Throwable cause = ex.getCause ( );
        String userFriendlyMessage = "constraint violation";
        if ( cause instanceof ConstraintViolationException hibernateEx ) {
            String constraintName = hibernateEx.getConstraintName ( );
            if ( constraintName != null ) {
                if ( constraintName.contains ( "idx_email" ) ) {
                    userFriendlyMessage = "Email already exists.";
                } else if ( constraintName.contains ( "idx_phone" ) ) {
                    userFriendlyMessage = "Phone number already exists.";
                }
            }
        }
        return buildResponse ( ex, HttpStatus.BAD_REQUEST, ErrorMessage.ERROR_CONSTRAINT_VIOLATION + ": " + userFriendlyMessage );
    }


    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<GenericResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn ( "Data integrity violation", ex );

        String errorMessage = "A database error occurred";
        Throwable rootCause = ex.getRootCause ( );

        if ( rootCause != null && rootCause.getMessage ( ) != null ) {
            String errorMsg = rootCause.getMessage ( );

            if ( errorMsg.contains ( "Duplicate entry" ) && errorMsg.contains ( "for key 'users.idx_phone'" ) ) {
                errorMessage = "Phone number is already registered";
            } else if ( errorMsg.contains ( "Duplicate entry" ) && errorMsg.contains ( "for key 'users.idx_email'" ) ) {
                errorMessage = "Email address is already registered";
            }
        }

        return buildResponse ( ex, HttpStatus.CONFLICT, errorMessage );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<GenericResponse> handleUnreadableJson(HttpMessageNotReadableException ex) {
        log.warn ( "Malformed request payload", ex );
        return buildResponse ( ex, HttpStatus.BAD_REQUEST, ErrorMessage.ERROR_UNREADABLE_JSON );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<GenericResponse> handleMissingParams(MissingServletRequestParameterException ex) {
        String msg = ErrorMessage.ERROR_MISSING_PARAMETER + ": " + ex.getParameterName ( );
        log.warn ( msg, ex );
        return buildResponse ( ex, HttpStatus.BAD_REQUEST, msg );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<GenericResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        log.warn ( "Unsupported HTTP method", ex );
        return buildResponse ( ex, HttpStatus.METHOD_NOT_ALLOWED, ErrorMessage.ERROR_METHOD_NOT_ALLOWED );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<GenericResponse> handleEntityNotFound(EntityNotFoundException ex) {
        log.warn ( "Entity not found", ex );
        return buildResponse ( ex, HttpStatus.NOT_FOUND, ErrorMessage.ERROR_NOT_FOUND );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<GenericResponse> handleAccessDenied(AccessDeniedException ex) {
        log.warn ( "Access denied", ex );
        return buildResponse ( ex, HttpStatus.FORBIDDEN, ErrorMessage.ERROR_FORBIDDEN );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GenericResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        return buildResponse ( ex, HttpStatus.BAD_REQUEST, ex.getMessage ( ) );
    }



    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<GenericResponse> handleUnauthorized(UnauthorizedAccessException ex) {
        log.warn ( "Unauthorized access", ex );
        return buildResponse ( ex, HttpStatus.UNAUTHORIZED, ErrorMessage.ERROR_UNAUTHORIZED );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<GenericResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn ( "Resource not found", ex );
        return buildResponse ( ex, HttpStatus.NOT_FOUND, ex.getMessage ( ) );
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<GenericResponse> handleConflict(ConflictException ex) {
        log.warn ( "Conflict error", ex );
        return buildResponse ( ex, HttpStatus.CONFLICT, ErrorMessage.ERROR_DATA_INTEGRITY );
    }

    @ExceptionHandler(OtpExpiredException.class)
    public ResponseEntity<GenericResponse> handleOtpExpiredException(OtpExpiredException ex) {
        log.warn ( "OTP has expired", ex );
        return buildResponse ( ex, HttpStatus.BAD_REQUEST, ex.getMessage ( ) );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericResponse> handleGenericException(Exception ex) {
        log.error ( "Unhandled exception", ex );
        return buildResponse ( ex, HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessage.ERROR_INTERNAL_SERVER );
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<GenericResponse> handleInvalidInputException(InvalidInputException ex) {
        log.warn ( "Invalid input", ex );
        return buildResponse ( ex, HttpStatus.BAD_REQUEST, ex.getMessage ( ) );
    }

//    @ExceptionHandler(UsernameNotFoundException.class)
//    public ResponseEntity<GenericResponse> handleUsernameNotFoundException(UsernameNotFoundException ex) {
//        log.warn ( "Invalid input", ex );
//        return buildResponse ( ex, HttpStatus.BAD_REQUEST, ex.getMessage ( ) );
//    }
//
//    @ExceptionHandler(BadCredentialsException.class)
//    public ResponseEntity<GenericResponse> handleBadCredentialsException(BadCredentialsException ex) {
//        log.warn ( "Invalid input", ex );
//        return buildResponse ( ex, HttpStatus.BAD_REQUEST, ex.getMessage ( ) );
//    }

    @ExceptionHandler(MemberAlreadyPresentException.class)
    public ResponseEntity<GenericResponse> handleMemberAlreadyPresentException(MemberAlreadyPresentException ex) {
        log.warn ( "Invalid input", ex );
        return buildResponse ( ex, HttpStatus.BAD_REQUEST, ex.getMessage ( ) );
    }

    @ExceptionHandler(OtpNotSentException.class)
    public ResponseEntity<GenericResponse> handleOtpNotSentException(OtpNotSentException ex) {
        log.warn ( "Failed to send OTP", ex );
        return buildResponse ( ex, HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessage.ERROR_OTP_SENDING_FAILED );
    }

    private ResponseEntity<GenericResponse> buildResponse(Exception ex, HttpStatus status, String message) {
        return ResponseEntity.status ( HttpStatus.OK ).body ( GenericResponse.builder ( ).status ( "failed" )
                .code ( String.valueOf ( status.value ( ) ) ).message ( message ).build ( ) );
    }
}
