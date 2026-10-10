package com.autoadda.apis.vehicle.enums;

import com.autoadda.apis.vehicle.exception.InvalidInputException;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Documents the extraction API can read. Each type has a matching ExtractionSpec.
 */
public enum ExtractionDocumentType {
    MULKIYA;

    public static ExtractionDocumentType from(String value) {
        return Arrays.stream ( values ( ) )
                .filter ( t -> t.name ( ).equalsIgnoreCase ( value ) )
                .findFirst ( )
                .orElseThrow ( () -> new InvalidInputException ( "Unsupported document type: " + value + ". Supported: "
                        + Arrays.stream ( values ( ) ).map ( Enum::name ).collect ( Collectors.joining ( ", " ) ) ) );
    }
}
