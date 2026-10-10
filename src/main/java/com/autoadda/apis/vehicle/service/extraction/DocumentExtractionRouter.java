package com.autoadda.apis.vehicle.service.extraction;

import com.autoadda.apis.vehicle.adapter.vision.InlineFile;
import com.autoadda.apis.vehicle.adapter.vision.VisionProvider;
import com.autoadda.apis.vehicle.config.ExtractionProperties;
import com.autoadda.apis.vehicle.exception.DocumentExtractionException;
import com.autoadda.apis.vehicle.exception.InvalidInputException;
import com.autoadda.apis.vehicle.exception.ProviderUnavailableException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Tries the configured providers in order (extraction.providers) until one answers.
 * A provider that fails temporarily is moved to the back of the ladder for extraction.cooldown-seconds,
 * so during an outage requests go straight to the next provider instead of waiting on the failing one.
 */
@Slf4j
@Component
public class DocumentExtractionRouter {

    public record Result(String provider, ObjectNode fields) {
    }

    private final ExtractionProperties properties;
    private final Map<String, VisionProvider> providersByName;
    private final Map<String, Instant> coolingDownUntil = new ConcurrentHashMap<> ( );

    public DocumentExtractionRouter(ExtractionProperties properties, List<VisionProvider> providers) {
        this.properties = properties;
        this.providersByName = providers.stream ( )
                .collect ( Collectors.toMap ( VisionProvider::name, Function.identity ( ) ) );
        properties.getProviders ( ).stream ( )
                .filter ( name -> !providersByName.containsKey ( name.trim ( ).toLowerCase ( Locale.ROOT ) ) )
                .forEach ( name -> log.warn ( "Unknown document extraction provider '{}' in extraction.providers", name ) );
    }

    public Result extract(List<InlineFile> files, ExtractionSpec spec) {
        List<VisionProvider> configured = properties.getProviders ( ).stream ( )
                .map ( name -> providersByName.get ( name.trim ( ).toLowerCase ( Locale.ROOT ) ) )
                .filter ( p -> p != null && p.isConfigured ( ) )
                .distinct ( )
                .toList ( );
        if ( configured.isEmpty ( ) ) {
            log.error ( "No document extraction provider has an API key (set GEMINI_API_KEY, GROQ_API_KEY or MISTRAL_API_KEY)" );
            throw new DocumentExtractionException ( "Document reading is not configured on the server" );
        }

        List<VisionProvider> capable = configured.stream ( )
                .filter ( p -> files.stream ( ).allMatch ( f -> p.supports ( f.mimeType ( ) ) ) )
                .toList ( );
        if ( capable.isEmpty ( ) ) {
            throw new InvalidInputException ( "This file type can't be read right now, please upload a JPEG or PNG photo" );
        }

        // Providers still cooling down go last, keeping the configured order otherwise (sort is stable)
        Instant now = Instant.now ( );
        List<VisionProvider> ladder = new ArrayList<> ( capable );
        ladder.sort ( Comparator.comparing ( p -> isCoolingDown ( p, now ) ) );

        boolean allTemporary = true;
        for ( VisionProvider provider : ladder ) {
            try {
                JsonNode result = provider.extract ( files, spec );
                if ( result instanceof ObjectNode fields ) {
                    coolingDownUntil.remove ( provider.name ( ) );
                    log.info ( "Document {} read by {}", spec.documentType ( ), provider.name ( ) );
                    return new Result ( provider.name ( ), fields );
                }
                allTemporary = false;
                log.warn ( "{} returned JSON that is not an object, trying next provider", provider.name ( ) );
            } catch ( ProviderUnavailableException ex ) {
                coolingDownUntil.put ( provider.name ( ), Instant.now ( ).plusSeconds ( properties.getCooldownSeconds ( ) ) );
                log.warn ( "{}, skipping it for {}s and trying next provider", ex.getMessage ( ), properties.getCooldownSeconds ( ) );
            } catch ( DocumentExtractionException ex ) {
                allTemporary = false;
                log.warn ( "{} failed: {}, trying next provider", provider.name ( ), ex.getMessage ( ) );
            }
        }

        throw new DocumentExtractionException ( allTemporary
                ? "Document reading is busy right now, please try again in a minute"
                : "Could not read the document, please fill the form manually" );
    }

    private boolean isCoolingDown(VisionProvider provider, Instant now) {
        Instant until = coolingDownUntil.get ( provider.name ( ) );
        return until != null && until.isAfter ( now );
    }
}
