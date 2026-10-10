package com.autoadda.apis.vehicle.service.extraction;

import com.autoadda.apis.vehicle.enums.ExtractionDocumentType;
import com.autoadda.apis.vehicle.util.PolicyTypeEnum;
import com.autoadda.apis.vehicle.util.VehicleTypeCategoryEnum;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * UAE vehicle registration card (Mulkiya). Field names match VehicleRegistrationRequest
 * so the frontend can drop the values straight into the registration form.
 */
@Component
public class MulkiyaExtractionSpec implements ExtractionSpec {

    private static final List<String> PLATE_SOURCE_CODES = List.of (
            "ABU_DHABI", "DUBAI", "SHARJAH", "AJMAN", "UMM_AL_QUWAIN", "RAS_AL_KHAIMAH", "FUJAIRAH" );

    private static final Set<String> DATE_FIELDS = Set.of ( "regDate", "regExpiry", "insuExpiry" );
    private static final Set<String> UPPERCASE_FIELDS = Set.of ( "chassisNo", "engineNumber", "code" );
    private static final Map<String, List<String>> CHOICE_FIELDS = Map.of (
            "state", PLATE_SOURCE_CODES,
            "vehicleTypeCategory", Arrays.stream ( VehicleTypeCategoryEnum.values ( ) ).map ( Enum::name ).toList ( ),
            "policyType", Arrays.stream ( PolicyTypeEnum.values ( ) ).map ( Enum::name ).toList ( ) );

    private static final DateTimeFormatter OUTPUT_DATE = DateTimeFormatter.ofPattern ( "dd-MM-yyyy" );
    private static final List<DateTimeFormatter> INPUT_DATES = List.of (
            DateTimeFormatter.ofPattern ( "d-M-yyyy" ),
            DateTimeFormatter.ofPattern ( "d/M/yyyy" ),
            DateTimeFormatter.ofPattern ( "d.M.yyyy" ),
            DateTimeFormatter.ofPattern ( "yyyy-M-d" ),
            DateTimeFormatter.ofPattern ( "yyyy/M/d" ) );

    private static final String PROMPT = """
            You are reading a UAE vehicle registration card (Mulkiya). The images are the front and, \
            if present, the back of the same card. Text may be in Arabic and English.

            Extract the fields defined in the response schema. Rules:
            - Return values in English. Transliterate Arabic names into English if no English text is printed.
            - Use null for any field that is not printed or not clearly readable. Never guess or invent values.
            - Dates must be dd-MM-yyyy.
            - state: the emirate that issued the card / plate (Place of Issue), as one of the allowed codes.
            - number: the plate number digits only. code: the plate code / letter / colour printed with the plate number.
            - brand: the manufacturer (e.g. TOYOTA). model: the model name without the brand (e.g. LAND CRUISER).
            - year: the 4-digit model year.
            - chassisNo: the full chassis / VIN number, without spaces.
            - vehicleTypeCategory: the registration / plate category, mapped to one of the allowed values.
            - policyType: only if the insurance type is printed.
            - Weights are in kg, digits only.
            """;

    @Override
    public ExtractionDocumentType documentType() {
        return ExtractionDocumentType.MULKIYA;
    }

    @Override
    public String prompt() {
        return PROMPT;
    }

    @Override
    public Map<String, Object> responseSchema() {
        Map<String, Object> properties = new LinkedHashMap<> ( );
        properties.put ( "number", text ( "Plate number" ) );
        properties.put ( "code", text ( "Plate code, letter or colour" ) );
        properties.put ( "state", choice ( "Issuing emirate", PLATE_SOURCE_CODES ) );
        properties.put ( "tcNumber", text ( "Traffic code number (T.C. No.)" ) );
        properties.put ( "ownerName", text ( "Owner name" ) );
        properties.put ( "nationality", text ( "Owner nationality" ) );
        properties.put ( "brand", text ( "Vehicle make / manufacturer" ) );
        properties.put ( "model", text ( "Vehicle model name" ) );
        properties.put ( "year", text ( "Model year, 4 digits" ) );
        properties.put ( "vehicleColor", text ( "Vehicle colour" ) );
        properties.put ( "vehicleType", text ( "Vehicle type as printed, e.g. Saloon, Station Wagon, Pick Up" ) );
        properties.put ( "vehicleTypeCategory", choice ( "Registration / plate category", CHOICE_FIELDS.get ( "vehicleTypeCategory" ) ) );
        properties.put ( "chassisNo", text ( "Chassis / VIN number" ) );
        properties.put ( "engineNumber", text ( "Engine number" ) );
        properties.put ( "origin", text ( "Country of origin" ) );
        properties.put ( "numOfPassenger", text ( "Number of passengers" ) );
        properties.put ( "emptyWeight", text ( "Empty weight in kg" ) );
        properties.put ( "grossWeight", text ( "Gross vehicle weight in kg" ) );
        properties.put ( "regDate", text ( "Registration date, dd-MM-yyyy" ) );
        properties.put ( "regExpiry", text ( "Registration expiry date, dd-MM-yyyy" ) );
        properties.put ( "insuExpiry", text ( "Insurance expiry date, dd-MM-yyyy" ) );
        properties.put ( "insuCompany", text ( "Insurance company" ) );
        properties.put ( "insuPolicy", text ( "Insurance policy number" ) );
        properties.put ( "policyType", choice ( "Insurance policy type", CHOICE_FIELDS.get ( "policyType" ) ) );

        return Map.of (
                "type", "OBJECT",
                "properties", properties,
                "propertyOrdering", List.copyOf ( properties.keySet ( ) ) );
    }

    /**
     * Returns exactly the schema fields in schema order: missing ones become null, extra keys are dropped,
     * and choice fields not in their allowed list become null (JSON-mode providers don't enforce the schema).
     */
    @Override
    @SuppressWarnings("unchecked")
    public ObjectNode postProcess(ObjectNode fields) {
        Map<String, Object> schemaFields = (Map<String, Object>) responseSchema ( ).get ( "properties" );
        ObjectNode cleaned = fields.objectNode ( );
        for ( String name : schemaFields.keySet ( ) ) {
            String text = clean ( name, fields.get ( name ) );
            if ( text == null ) {
                cleaned.putNull ( name );
            } else {
                cleaned.put ( name, text );
            }
        }
        return cleaned;
    }

    private static String clean(String name, JsonNode value) {
        if ( value == null || value.isNull ( ) || value.isContainerNode ( ) ) {
            return null;
        }
        String text = value.asText ( ).trim ( );
        if ( text.isEmpty ( ) || text.equalsIgnoreCase ( "null" ) ) {
            return null;
        }
        if ( DATE_FIELDS.contains ( name ) ) {
            return normalizeDate ( text );
        }
        if ( CHOICE_FIELDS.containsKey ( name ) ) {
            String code = text.toUpperCase ( Locale.ROOT ).replaceAll ( "[\\s-]+", "_" );
            return CHOICE_FIELDS.get ( name ).contains ( code ) ? code : null;
        }
        if ( UPPERCASE_FIELDS.contains ( name ) ) {
            text = text.toUpperCase ( Locale.ROOT );
            if ( name.equals ( "chassisNo" ) ) {
                text = text.replaceAll ( "\\s+", "" );
            }
        }
        return text;
    }

    private static String normalizeDate(String value) {
        for ( DateTimeFormatter format : INPUT_DATES ) {
            try {
                return LocalDate.parse ( value, format ).format ( OUTPUT_DATE );
            } catch ( DateTimeParseException ignored ) {
                // try the next format
            }
        }
        return null;
    }

    private static Map<String, Object> text(String description) {
        return Map.of ( "type", "STRING", "nullable", true, "description", description );
    }

    private static Map<String, Object> choice(String description, List<String> values) {
        return Map.of ( "type", "STRING", "format", "enum", "enum", values, "nullable", true, "description", description );
    }
}
