package com.autoadda.apis.vehicle.exception;

/**
 * Thrown when the LLM could not read a document (service down, blocked, or unparseable output).
 */
public class DocumentExtractionException extends RuntimeException {

    public DocumentExtractionException(String message) {
        super ( message );
    }

    public DocumentExtractionException(String message, Throwable cause) {
        super ( message, cause );
    }
}
