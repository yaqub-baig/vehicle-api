package com.autoadda.apis.vehicle.controller;

import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.DocumentExtractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin("*")
@RequiredArgsConstructor
@RestController
@RequestMapping("/document-extraction")
public class DocumentExtractionController {

    private final DocumentExtractionService documentExtractionService;

    /**
     * POST /document-extraction/{documentType} - reads the uploaded document image(s) with the LLM and
     * returns the fields for auto-filling the matching form. Supported types: MULKIYA.
     */
    @PostMapping(value = "/{documentType}", consumes = {"multipart/form-data"})
    public GenericResponse extract(@PathVariable String documentType,
                                   @RequestPart("front") MultipartFile front,
                                   @RequestPart(value = "back", required = false) MultipartFile back) {
        return documentExtractionService.extract ( documentType, front, back );
    }
}
