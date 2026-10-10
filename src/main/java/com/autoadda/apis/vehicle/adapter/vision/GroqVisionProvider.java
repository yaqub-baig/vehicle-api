package com.autoadda.apis.vehicle.adapter.vision;

import com.autoadda.apis.vehicle.config.GroqProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

/** Groq chat completions (OpenAI format: image_url is {"url": ...}, max 3 images per request). */
@Component
public class GroqVisionProvider extends OpenAiCompatibleVisionProvider {

    private final GroqProperties properties;

    public GroqVisionProvider(GroqProperties properties, ObjectMapper objectMapper) {
        super ( properties, objectMapper );
        this.properties = properties;
    }

    @Override
    public String name() {
        return "groq";
    }

    @Override
    protected Object imageUrl(String dataUrl) {
        return Map.of ( "url", dataUrl );
    }

    @Override
    protected void addExtraFields(Map<String, Object> body) {
        if ( StringUtils.hasText ( properties.getReasoningEffort ( ) ) ) {
            body.put ( "reasoning_effort", properties.getReasoningEffort ( ) );
        }
    }
}
