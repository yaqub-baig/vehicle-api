package com.autoadda.apis.vehicle.service.impl;

import com.autoadda.apis.vehicle.adapter.vision.InlineFile;
import com.autoadda.apis.vehicle.enums.ExtractionDocumentType;
import com.autoadda.apis.vehicle.exception.InvalidInputException;
import com.autoadda.apis.vehicle.response.DocumentExtractionResponse;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.DocumentExtractionService;
import com.autoadda.apis.vehicle.service.extraction.DocumentExtractionRouter;
import com.autoadda.apis.vehicle.service.extraction.ExtractionSpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Sends document images to the LLM providers (via DocumentExtractionRouter) and returns the extracted fields for form auto-fill.
 * Images are not stored.
 */
@Slf4j
@Service
public class DocumentExtractionServiceImpl implements DocumentExtractionService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final int MAX_IMAGE_SIDE = 2000;
    private static final int MAX_UNRESIZED_BYTES = 1024 * 1024;
    private static final float JPEG_QUALITY = 0.85f;
    private static final Set<String> ALLOWED_TYPES = Set.of (
            "image/jpeg", "image/png", "image/webp", "image/heic", "image/heif", "application/pdf" );
    private static final Map<String, String> TYPE_BY_EXTENSION = Map.of (
            "jpg", "image/jpeg", "jpeg", "image/jpeg", "png", "image/png", "webp", "image/webp",
            "heic", "image/heic", "heif", "image/heif", "pdf", "application/pdf" );

    private final DocumentExtractionRouter router;
    private final Map<ExtractionDocumentType, ExtractionSpec> specs = new EnumMap<> ( ExtractionDocumentType.class );

    public DocumentExtractionServiceImpl(DocumentExtractionRouter router, List<ExtractionSpec> specList) {
        this.router = router;
        specList.forEach ( spec -> specs.put ( spec.documentType ( ), spec ) );
    }

    @Override
    public GenericResponse extract(String documentType, MultipartFile front, MultipartFile back) {
        ExtractionDocumentType type = ExtractionDocumentType.from ( documentType );
        ExtractionSpec spec = specs.get ( type );
        if ( spec == null ) {
            throw new InvalidInputException ( "Unsupported document type: " + type );
        }

        List<InlineFile> files = new ArrayList<> ( );
        files.add ( toInlineFile ( front, "front" ) );
        if ( back != null && !back.isEmpty ( ) ) {
            files.add ( toInlineFile ( back, "back" ) );
        }

        DocumentExtractionRouter.Result result = router.extract ( files, spec );

        DocumentExtractionResponse body = DocumentExtractionResponse.builder ( )
                .documentType ( type )
                .provider ( result.provider ( ) )
                .fields ( spec.postProcess ( result.fields ( ) ) )
                .build ( );
        return GenericResponse.builder ( ).status ( "success" ).code ( "200" )
                .message ( "Document read successfully" ).body ( body ).build ( );
    }

    private InlineFile toInlineFile(MultipartFile file, String partName) {
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
        byte[] data;
        try {
            data = file.getBytes ( );
        } catch ( IOException ex ) {
            throw new InvalidInputException ( "Could not read the '" + partName + "' file" );
        }
        return shrinkIfLarge ( data, mimeType );
    }

    /**
     * Phone photos are often 4000px+ and several MB; Gemini doesn't need that to read the text and the
     * upload makes the call slow. Images larger than MAX_IMAGE_SIDE are resized and re-encoded as JPEG.
     * Anything ImageIO can't decode (HEIC, PDF, some WEBP) is sent unchanged.
     */
    private static InlineFile shrinkIfLarge(byte[] data, String mimeType) {
        if ( !mimeType.startsWith ( "image/" ) ) {
            return new InlineFile ( data, mimeType );
        }
        try {
            BufferedImage source = ImageIO.read ( new ByteArrayInputStream ( data ) );
            if ( source == null ) {
                return new InlineFile ( data, mimeType );
            }
            int longest = Math.max ( source.getWidth ( ), source.getHeight ( ) );
            if ( longest <= MAX_IMAGE_SIDE && data.length <= MAX_UNRESIZED_BYTES ) {
                return new InlineFile ( data, mimeType );
            }

            double scale = Math.min ( 1.0, (double) MAX_IMAGE_SIDE / longest );
            int width = Math.max ( 1, (int) Math.round ( source.getWidth ( ) * scale ) );
            int height = Math.max ( 1, (int) Math.round ( source.getHeight ( ) * scale ) );
            BufferedImage resized = new BufferedImage ( width, height, BufferedImage.TYPE_INT_RGB );
            Graphics2D g = resized.createGraphics ( );
            g.setColor ( Color.WHITE );
            g.fillRect ( 0, 0, width, height );
            g.setRenderingHint ( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR );
            g.setRenderingHint ( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
            g.drawImage ( source, 0, 0, width, height, null );
            g.dispose ( );

            byte[] jpeg = encodeJpeg ( resized );
            log.info ( "Resized document image {}x{} ({} KB) -> {}x{} ({} KB)", source.getWidth ( ), source.getHeight ( ),
                    data.length / 1024, width, height, jpeg.length / 1024 );
            return new InlineFile ( jpeg, "image/jpeg" );
        } catch ( IOException | RuntimeException ex ) {
            log.warn ( "Could not resize document image, sending original: {}", ex.getMessage ( ) );
            return new InlineFile ( data, mimeType );
        }
    }

    private static byte[] encodeJpeg(BufferedImage image) throws IOException {
        ImageWriter writer = ImageIO.getImageWritersByFormatName ( "jpeg" ).next ( );
        ImageWriteParam param = writer.getDefaultWriteParam ( );
        param.setCompressionMode ( ImageWriteParam.MODE_EXPLICIT );
        param.setCompressionQuality ( JPEG_QUALITY );
        ByteArrayOutputStream out = new ByteArrayOutputStream ( );
        try ( ImageOutputStream ios = ImageIO.createImageOutputStream ( out ) ) {
            writer.setOutput ( ios );
            writer.write ( null, new IIOImage ( image, null, null ), param );
        } finally {
            writer.dispose ( );
        }
        return out.toByteArray ( );
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
