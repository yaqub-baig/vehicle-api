package com.autoadda.apis.vehicle.service.extraction;

import com.autoadda.apis.vehicle.enums.ExtractionDocumentType;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;

/**
 * What to ask the LLM for one document type: the prompt, the JSON schema it must answer in,
 * and clean-up of its answer. Add a new document type by adding a new spec bean.
 */
public interface ExtractionSpec {

    ExtractionDocumentType documentType();

    String prompt();

    /** Gemini responseSchema (OpenAPI subset: type OBJECT/STRING, properties, enum, nullable). */
    Map<String, Object> responseSchema();

    /** Normalizes the model output in place and returns it. */
    ObjectNode postProcess(ObjectNode fields);
}
