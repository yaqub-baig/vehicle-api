package com.autoadda.apis.vehicle.adapter.vision;

import com.autoadda.apis.vehicle.config.MistralProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

/** Mistral chat completions (image_url is the data URL string). */
@Component
public class MistralVisionProvider extends OpenAiCompatibleVisionProvider {

    public MistralVisionProvider(MistralProperties properties, ObjectMapper objectMapper) {
        super ( properties, objectMapper );
    }

    @Override
    public String name() {
        return "mistral";
    }

    @Override
    protected Object imageUrl(String dataUrl) {
        return dataUrl;
    }
}
