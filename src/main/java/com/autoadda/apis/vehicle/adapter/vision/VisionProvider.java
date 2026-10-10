package com.autoadda.apis.vehicle.adapter.vision;

import com.autoadda.apis.vehicle.service.extraction.ExtractionSpec;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * One LLM provider that can read document images and answer with JSON for an ExtractionSpec.
 * Implementations throw ProviderUnavailableException for temporary failures (overloaded, rate limited,
 * timed out) and DocumentExtractionException for anything else.
 */
public interface VisionProvider {

    /** Short id used in config (extraction.providers) and logs, e.g. "gemini". */
    String name();

    /** False when no API key is set; the provider is then skipped. */
    boolean isConfigured();

    boolean supports(String mimeType);

    JsonNode extract(List<InlineFile> files, ExtractionSpec spec);
}
