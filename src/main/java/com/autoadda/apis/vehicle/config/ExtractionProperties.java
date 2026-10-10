package com.autoadda.apis.vehicle.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Which document-reading providers to use, in order, and how long to skip one after it fails. */
@Data
@Component
@ConfigurationProperties(prefix = "extraction")
public class ExtractionProperties {

    /** Fallback ladder: the first provider is tried first, the next one only if it fails. */
    private List<String> providers = new ArrayList<> ( List.of ( "gemini", "groq", "mistral" ) );
    /** After a temporary failure (overloaded, rate limited, timed out) the provider is tried last for this long. */
    private int cooldownSeconds = 60;
}
