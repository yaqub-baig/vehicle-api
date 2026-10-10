package com.autoadda.apis.vehicle.exception;

/**
 * A document-reading provider failed for a temporary reason (overloaded, rate limited, timed out),
 * so it is worth trying another provider and skipping this one for a while.
 */
public class ProviderUnavailableException extends DocumentExtractionException {

    public ProviderUnavailableException(String message, Throwable cause) {
        super ( message, cause );
    }
}
