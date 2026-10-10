package com.autoadda.apis.vehicle.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Google Gemini settings used for document text extraction.
 * The API key comes from the GEMINI_API_KEY environment variable and is never committed.
 */
@Data
@Component
@ConfigurationProperties(prefix = "gemini")
public class GeminiProperties {

    private String apiKey;
    private String model = "gemini-2.5-flash";
    private String baseUrl = "https://generativelanguage.googleapis.com/v1beta";
    private int timeoutSeconds = 90;
    /** Gemini thinking budget in tokens; 0 disables thinking (flash models). Leave empty to use the model default. */
    private Integer thinkingBudget = 0;
}
