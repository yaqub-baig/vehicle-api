package com.autoadda.apis.vehicle.config;

import lombok.Getter;
import lombok.Setter;

/** Settings shared by providers with an OpenAI-style chat completions API (Groq, Mistral). */
@Getter
@Setter
public abstract class OpenAiCompatibleProperties {

    private String apiKey;
    private String model;
    private String baseUrl;
    private int timeoutSeconds = 30;
}
