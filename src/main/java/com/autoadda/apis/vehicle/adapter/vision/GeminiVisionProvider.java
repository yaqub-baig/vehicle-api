package com.autoadda.apis.vehicle.adapter.vision;

import com.autoadda.apis.vehicle.config.GeminiProperties;
import com.autoadda.apis.vehicle.exception.DocumentExtractionException;
import com.autoadda.apis.vehicle.service.extraction.ExtractionSpec;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Google Gemini generateContent with inline files; the answer is constrained by the spec's response schema.
 * Gemini also reads HEIC and PDF, which the other providers can't.
 */
@Slf4j
@Component
public class GeminiVisionProvider implements VisionProvider {

    private static final Set<String> SUPPORTED_TYPES = Set.of (
            "image/jpeg", "image/png", "image/webp", "image/heic", "image/heif", "application/pdf" );

    private final GeminiProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public GeminiVisionProvider(GeminiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory ( );
        requestFactory.setConnectTimeout ( Duration.ofSeconds ( 10 ) );
        requestFactory.setReadTimeout ( Duration.ofSeconds ( properties.getTimeoutSeconds ( ) ) );
        this.restClient = RestClient.builder ( )
                .baseUrl ( properties.getBaseUrl ( ) )
                .requestFactory ( requestFactory )
                .build ( );
    }

    @Override
    public String name() {
        return "gemini";
    }

    @Override
    public boolean isConfigured() {
        return StringUtils.hasText ( properties.getApiKey ( ) );
    }

    @Override
    public boolean supports(String mimeType) {
        return SUPPORTED_TYPES.contains ( mimeType );
    }

    @Override
    public JsonNode extract(List<InlineFile> files, ExtractionSpec spec) {
        List<Map<String, Object>> parts = new ArrayList<> ( );
        for ( InlineFile file : files ) {
            parts.add ( Map.of ( "inlineData", Map.of (
                    "mimeType", file.mimeType ( ),
                    "data", Base64.getEncoder ( ).encodeToString ( file.data ( ) ) ) ) );
        }
        parts.add ( Map.of ( "text", spec.prompt ( ) ) );

        Map<String, Object> generationConfig = new HashMap<> ( );
        generationConfig.put ( "temperature", 0 );
        generationConfig.put ( "responseMimeType", "application/json" );
        generationConfig.put ( "responseSchema", spec.responseSchema ( ) );
        // Thinking adds a long delay and is not needed to read printed text; 0 turns it off on flash models
        if ( properties.getThinkingBudget ( ) != null ) {
            generationConfig.put ( "thinkingConfig", Map.of ( "thinkingBudget", properties.getThinkingBudget ( ) ) );
        }

        Map<String, Object> body = Map.of (
                "contents", List.of ( Map.of ( "role", "user", "parts", parts ) ),
                "generationConfig", generationConfig );

        long startedAt = System.currentTimeMillis ( );
        JsonNode response;
        try {
            response = restClient.post ( )
                    .uri ( "/models/{model}:generateContent", properties.getModel ( ) )
                    .header ( "x-goog-api-key", properties.getApiKey ( ) )
                    .contentType ( MediaType.APPLICATION_JSON )
                    .body ( body )
                    .retrieve ( )
                    .body ( JsonNode.class );
        } catch ( RestClientException ex ) {
            throw ProviderErrors.map ( name ( ), ex, System.currentTimeMillis ( ) - startedAt );
        }

        log.info ( "gemini extraction took {} ms", System.currentTimeMillis ( ) - startedAt );
        return parseResponse ( response );
    }

    private JsonNode parseResponse(JsonNode response) {
        if ( response == null ) {
            throw new DocumentExtractionException ( "gemini returned an empty response" );
        }
        String blockReason = response.path ( "promptFeedback" ).path ( "blockReason" ).asText ( null );
        if ( blockReason != null ) {
            throw new DocumentExtractionException ( "gemini blocked the request: " + blockReason );
        }

        JsonNode candidate = response.path ( "candidates" ).path ( 0 );
        StringBuilder text = new StringBuilder ( );
        for ( JsonNode part : candidate.path ( "content" ).path ( "parts" ) ) {
            if ( !part.path ( "thought" ).asBoolean ( false ) && part.hasNonNull ( "text" ) ) {
                text.append ( part.get ( "text" ).asText ( ) );
            }
        }
        if ( text.isEmpty ( ) ) {
            throw new DocumentExtractionException ( "gemini returned no text, finishReason="
                    + candidate.path ( "finishReason" ).asText ( ) );
        }

        try {
            return objectMapper.readTree ( text.toString ( ) );
        } catch ( JsonProcessingException ex ) {
            throw new DocumentExtractionException ( "gemini returned invalid JSON (" + text.length ( ) + " chars)", ex );
        }
    }
}
