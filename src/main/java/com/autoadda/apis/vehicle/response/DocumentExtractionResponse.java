package com.autoadda.apis.vehicle.response;

import com.autoadda.apis.vehicle.enums.ExtractionDocumentType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentExtractionResponse {

    private ExtractionDocumentType documentType;
    /** Which LLM provider read the document (gemini, groq, mistral). */
    private String provider;
    /** Extracted values keyed by form field name; a field is null when it could not be read. */
    private ObjectNode fields;
}
