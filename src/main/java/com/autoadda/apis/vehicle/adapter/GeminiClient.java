package com.autoadda.apis.vehicle.adapter;

import com.autoadda.apis.vehicle.config.GeminiProperties;
import com.autoadda.apis.vehicle.exception.DocumentExtractionException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Calls Gemini generateContent with one or more files and a prompt, and asks for JSON
 * matching the given response schema.
 */
@Slf4j
@Component
public class GeminiClient {

    private final GeminiProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public GeminiClient(GeminiProperties properties, ObjectMapper objectMapper) {
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

    /** A file to send inline: raw bytes plus its MIME type (image/jpeg, application/pdf, ...). */
    public record InlineFile(byte[] data, String mimeType) {
    }

    public JsonNode generateJson(List<InlineFile> files, String prompt, Map<String, Object> responseSchema) {
        if ( !StringUtils.hasText ( properties.getApiKey ( ) ) ) {
            log.error ( "Gemini API key is not configured (set GEMINI_API_KEY)" );
            throw new DocumentExtractionException ( "Document reading is not configured on the server" );
        }

        List<Map<String, Object>> parts = new ArrayList<> ( );
        for ( InlineFile file : files ) {
            parts.add ( Map.of ( "inlineData", Map.of (
                    "mimeType", file.mimeType ( ),
                    "data", Base64.getEncoder ( ).encodeToString ( file.data ( ) ) ) ) );
        }
        parts.add ( Map.of ( "text", prompt ) );

        Map<String, Object> body = Map.of (
                "contents", List.of ( Map.of ( "role", "user", "parts", parts ) ),
                "generationConfig", Map.of (
                        "temperature", 0,
                        "responseMimeType", "application/json",
                        "responseSchema", responseSchema ) );

        JsonNode response;
        try {
            response = restClient.post ( )
                    .uri ( "/models/{model}:generateContent", properties.getModel ( ) )
                    .header ( "x-goog-api-key", properties.getApiKey ( ) )
                    .contentType ( MediaType.APPLICATION_JSON )
                    .body ( body )
                    .retrieve ( )
                    .body ( JsonNode.class );
        } catch ( RestClientResponseException ex ) {
            log.error ( "Gemini request failed: HTTP {} {}", ex.getStatusCode ( ).value ( ), ex.getResponseBodyAsString ( ) );
            throw new DocumentExtractionException ( "Could not read the document, please fill the form manually", ex );
        } catch ( ResourceAccessException ex ) {
            log.error ( "Gemini request failed: {}", ex.getMessage ( ) );
            throw new DocumentExtractionException ( "Document reading timed out, please try again", ex );
        }

        return parseResponse ( response );
    }

    private JsonNode parseResponse(JsonNode response) {
        if ( response == null ) {
            throw new DocumentExtractionException ( "Could not read the document, please fill the form manually" );
        }
        String blockReason = response.path ( "promptFeedback" ).path ( "blockReason" ).asText ( null );
        if ( blockReason != null ) {
            log.warn ( "Gemini blocked the request: {}", blockReason );
            throw new DocumentExtractionException ( "Could not read the document, please fill the form manually" );
        }

        JsonNode candidate = response.path ( "candidates" ).path ( 0 );
        StringBuilder text = new StringBuilder ( );
        for ( JsonNode part : candidate.path ( "content" ).path ( "parts" ) ) {
            if ( !part.path ( "thought" ).asBoolean ( false ) && part.hasNonNull ( "text" ) ) {
                text.append ( part.get ( "text" ).asText ( ) );
            }
        }
        if ( text.isEmpty ( ) ) {
            log.warn ( "Gemini returned no text, finishReason={}", candidate.path ( "finishReason" ).asText ( ) );
            throw new DocumentExtractionException ( "Could not read the document, please fill the form manually" );
        }

        try {
            return objectMapper.readTree ( text.toString ( ) );
        } catch ( JsonProcessingException ex ) {
            log.warn ( "Gemini returned invalid JSON ({} chars)", text.length ( ) );
            throw new DocumentExtractionException ( "Could not read the document, please fill the form manually", ex );
        }
    }
}
