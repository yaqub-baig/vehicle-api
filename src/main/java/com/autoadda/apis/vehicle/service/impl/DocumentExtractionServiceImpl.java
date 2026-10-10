package com.autoadda.apis.vehicle.service.impl;

import com.autoadda.apis.vehicle.adapter.GeminiClient;
import com.autoadda.apis.vehicle.enums.ExtractionDocumentType;
import com.autoadda.apis.vehicle.exception.DocumentExtractionException;
import com.autoadda.apis.vehicle.exception.InvalidInputException;
import com.autoadda.apis.vehicle.response.DocumentExtractionResponse;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.DocumentExtractionService;
import com.autoadda.apis.vehicle.service.extraction.ExtractionSpec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Sends document images to Gemini and returns the extracted fields for form auto-fill.
 * Images are not stored.
 */
@Slf4j
@Service
public class DocumentExtractionServiceImpl implements DocumentExtractionService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of (
            "image/jpeg", "image/png", "image/webp", "image/heic", "image/heif", "application/pdf" );
    private static final Map<String, String> TYPE_BY_EXTENSION = Map.of (
            "jpg", "image/jpeg", "jpeg", "image/jpeg", "png", "image/png", "webp", "image/webp",
            "heic", "image/heic", "heif", "image/heif", "pdf", "application/pdf" );

    private final GeminiClient geminiClient;
    private final Map<ExtractionDocumentType, ExtractionSpec> specs = new EnumMap<> ( ExtractionDocumentType.class );

    public DocumentExtractionServiceImpl(GeminiClient geminiClient, List<ExtractionSpec> specList) {
        this.geminiClient = geminiClient;
        specList.forEach ( spec -> specs.put ( spec.documentType ( ), spec ) );
    }

    @Override
    public GenericResponse extract(String documentType, MultipartFile front, MultipartFile back) {
        ExtractionDocumentType type = ExtractionDocumentType.from ( documentType );
        ExtractionSpec spec = specs.get ( type );
        if ( spec == null ) {
            throw new InvalidInputException ( "Unsupported document type: " + type );
        }

        List<GeminiClient.InlineFile> files = new ArrayList<> ( );
        files.add ( toInlineFile ( front, "front" ) );
        if ( back != null && !back.isEmpty ( ) ) {
            files.add ( toInlineFile ( back, "back" ) );
        }

        JsonNode result = geminiClient.generateJson ( files, spec.prompt ( ), spec.responseSchema ( ) );
        if ( !(result instanceof ObjectNode fields) ) {
            throw new DocumentExtractionException ( "Could not read the document, please fill the form manually" );
        }

        DocumentExtractionResponse body = DocumentExtractionResponse.builder ( )
                .documentType ( type )
                .fields ( spec.postProcess ( fields ) )
                .build ( );
        return GenericResponse.builder ( ).status ( "success" ).code ( "200" )
                .message ( "Document read successfully" ).body ( body ).build ( );
    }

    private GeminiClient.InlineFile toInlineFile(MultipartFile file, String partName) {
        if ( file == null || file.isEmpty ( ) ) {
            throw new InvalidInputException ( "The '" + partName + "' image is required" );
        }
        if ( file.getSize ( ) > MAX_FILE_SIZE ) {
            throw new InvalidInputException ( "The '" + partName + "' file must be less than 10MB" );
        }
        String mimeType = resolveMimeType ( file );
        if ( mimeType == null ) {
            throw new InvalidInputException ( "The '" + partName + "' file must be JPEG, PNG, WEBP, HEIC or PDF" );
        }
        try {
            return new GeminiClient.InlineFile ( file.getBytes ( ), mimeType );
        } catch ( IOException ex ) {
            throw new InvalidInputException ( "Could not read the '" + partName + "' file" );
        }
    }

    private static String resolveMimeType(MultipartFile file) {
        String contentType = file.getContentType ( );
        if ( contentType != null ) {
            String normalized = contentType.toLowerCase ( Locale.ROOT ).split ( ";" )[0].trim ( );
            if ( normalized.equals ( "image/jpg" ) ) {
                normalized = "image/jpeg";
            }
            if ( ALLOWED_TYPES.contains ( normalized ) ) {
                return normalized;
            }
        }
        // Some clients send application/octet-stream; fall back to the file extension
        String name = file.getOriginalFilename ( );
        if ( name != null && name.contains ( "." ) ) {
            return TYPE_BY_EXTENSION.get ( name.substring ( name.lastIndexOf ( '.' ) + 1 ).toLowerCase ( Locale.ROOT ) );
        }
        return null;
    }
}
