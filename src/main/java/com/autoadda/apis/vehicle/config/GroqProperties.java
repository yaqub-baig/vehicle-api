package com.autoadda.apis.vehicle.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Groq settings. The API key comes from the GROQ_API_KEY environment variable and is never committed. */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "groq")
public class GroqProperties extends OpenAiCompatibleProperties {

    /** Sent as reasoning_effort when set; "none" keeps reasoning models fast. Leave empty for models without it. */
    private String reasoningEffort;
}
