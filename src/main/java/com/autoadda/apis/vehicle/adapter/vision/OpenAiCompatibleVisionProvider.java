package com.autoadda.apis.vehicle.adapter.vision;

import com.autoadda.apis.vehicle.config.OpenAiCompatibleProperties;
import com.autoadda.apis.vehicle.exception.DocumentExtractionException;
import com.autoadda.apis.vehicle.service.extraction.ExtractionSpec;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
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
 * Base for providers with an OpenAI-style /chat/completions API and image_url content parts.
 * These APIs don't take Gemini's schema format, so the expected fields are described in the
 * system message and JSON mode (response_format json_object) is used.
 */
@Slf4j
public abstract class OpenAiCompatibleVisionProvider implements VisionProvider {

    private static final Set<String> SUPPORTED_TYPES = Set.of ( "image/jpeg", "image/png", "image/webp", "image/gif" );

    private final OpenAiCompatibleProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    protected OpenAiCompatibleVisionProvider(OpenAiCompatibleProperties properties, ObjectMapper objectMapper) {
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

    /** The image_url value for a data URL; providers differ on string vs {"url": ...}. */
    protected abstract Object imageUrl(String dataUrl);

    /** Provider-specific request fields, e.g. reasoning_effort. */
    protected void addExtraFields(Map<String, Object> body) {
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
        List<Map<String, Object>> content = new ArrayList<> ( );
        content.add ( Map.of ( "type", "text", "text", spec.prompt ( ) ) );
        for ( InlineFile file : files ) {
            String dataUrl = "data:" + file.mimeType ( ) + ";base64," + Base64.getEncoder ( ).encodeToString ( file.data ( ) );
            content.add ( Map.of ( "type", "image_url", "image_url", imageUrl ( dataUrl ) ) );
        }

        Map<String, Object> body = new HashMap<> ( );
        body.put ( "model", properties.getModel ( ) );
        body.put ( "temperature", 0 );
        body.put ( "response_format", Map.of ( "type", "json_object" ) );
        body.put ( "messages", List.of (
                Map.of ( "role", "system", "content", describeFields ( spec.responseSchema ( ) ) ),
                Map.of ( "role", "user", "content", content ) ) );
        addExtraFields ( body );

        long startedAt = System.currentTimeMillis ( );
        JsonNode response;
        try {
            response = restClient.post ( )
                    .uri ( "/chat/completions" )
                    .header ( "Authorization", "Bearer " + properties.getApiKey ( ) )
                    .contentType ( MediaType.APPLICATION_JSON )
                    .body ( body )
                    .retrieve ( )
                    .body ( JsonNode.class );
        } catch ( RestClientException ex ) {
            throw ProviderErrors.map ( name ( ), ex, System.currentTimeMillis ( ) - startedAt );
        }

        log.info ( "{} extraction took {} ms", name ( ), System.currentTimeMillis ( ) - startedAt );
        return parseResponse ( response );
    }

    private JsonNode parseResponse(JsonNode response) {
        String text = response == null ? null
                : response.path ( "choices" ).path ( 0 ).path ( "message" ).path ( "content" ).asText ( null );
        if ( !StringUtils.hasText ( text ) ) {
            throw new DocumentExtractionException ( name ( ) + " returned no text" );
        }
        text = stripCodeFence ( text.trim ( ) );
        try {
            return objectMapper.readTree ( text );
        } catch ( JsonProcessingException ex ) {
            throw new DocumentExtractionException ( name ( ) + " returned invalid JSON (" + text.length ( ) + " chars)", ex );
        }
    }

    private static String stripCodeFence(String text) {
        if ( text.startsWith ( "```" ) ) {
            int firstLineEnd = text.indexOf ( '\n' );
            int closing = text.lastIndexOf ( "```" );
            if ( firstLineEnd > 0 && closing > firstLineEnd ) {
                return text.substring ( firstLineEnd + 1, closing ).trim ( );
            }
        }
        return text;
    }

    /** Turns the spec's Gemini-style schema into plain instructions listing every key and its allowed values. */
    @SuppressWarnings("unchecked")
    private static String describeFields(Map<String, Object> schema) {
        StringBuilder sb = new StringBuilder ( "Respond with only a JSON object with exactly these keys. "
                + "Every value is a string or null; use null when the value is not printed or not clearly readable.\n" );
        Map<String, Object> properties = (Map<String, Object>) schema.getOrDefault ( "properties", Map.of ( ) );
        properties.forEach ( (key, value) -> {
            Map<String, Object> field = (Map<String, Object>) value;
            sb.append ( "- " ).append ( key );
            Object description = field.get ( "description" );
            if ( description != null ) {
                sb.append ( ": " ).append ( description );
            }
            Object allowed = field.get ( "enum" );
            if ( allowed instanceof List<?> values ) {
                sb.append ( ". One of: " ).append ( String.join ( ", ", (List<String>) values ) );
            }
            sb.append ( '\n' );
        } );
        return sb.toString ( );
    }
}
