package com.autoadda.apis.vehicle.adapter.vision;

import com.autoadda.apis.vehicle.exception.DocumentExtractionException;
import com.autoadda.apis.vehicle.exception.ProviderUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.SocketTimeoutException;

/** Maps HTTP client failures from any provider to temporary vs permanent extraction errors. */
@Slf4j
final class ProviderErrors {

    private ProviderErrors() {
    }

    static DocumentExtractionException map(String provider, RestClientException ex, long elapsedMs) {
        if ( ex instanceof RestClientResponseException response ) {
            int status = response.getStatusCode ( ).value ( );
            log.warn ( "{} request failed after {} ms: HTTP {} {}", provider, elapsedMs, status, response.getResponseBodyAsString ( ) );
            if ( status == 429 || status == 408 || status >= 500 ) {
                return new ProviderUnavailableException ( provider + " unavailable (HTTP " + status + ")", ex );
            }
            return new DocumentExtractionException ( provider + " rejected the request (HTTP " + status + ")", ex );
        }
        // Timeouts surface as ResourceAccessException or, while reading the body, as a plain RestClientException
        if ( isTimeout ( ex ) ) {
            log.warn ( "{} request timed out after {} ms", provider, elapsedMs );
            return new ProviderUnavailableException ( provider + " timed out", ex );
        }
        log.warn ( "{} request failed after {} ms: {}", provider, elapsedMs, ex.getMessage ( ) );
        return new ProviderUnavailableException ( provider + " could not be reached", ex );
    }

    private static boolean isTimeout(Throwable ex) {
        for ( Throwable t = ex; t != null; t = t.getCause ( ) ) {
            if ( t instanceof SocketTimeoutException ) {
                return true;
            }
        }
        return false;
    }
}
