package com.autoadda.apis.vehicle.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Mistral settings. The API key comes from the MISTRAL_API_KEY environment variable and is never committed. */
@Component
@ConfigurationProperties(prefix = "mistral")
public class MistralProperties extends OpenAiCompatibleProperties {
}
