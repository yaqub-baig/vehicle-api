package com.autoadda.apis.vehicle.service.impl;



import com.autoadda.apis.vehicle.dao.MediaFileRepository;
import com.autoadda.apis.vehicle.entity.MediaFile;
import com.autoadda.apis.vehicle.exception.ResourceNotFoundException;
import com.autoadda.apis.vehicle.mapper.MediaFileMapper;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.response.MediaFileResponse;
import com.autoadda.apis.vehicle.service.MediaFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaFileServiceImpl implements MediaFileService {

    private final MediaFileRepository mediaFileRepository;
    private final MediaFileMapper mediaFileMapper;
    @Value("${file.upload-dir}")
    private String uploadDir;
    @Value("${file.base-url}")
    private String baseUrl;

    @Override
    public List<MediaFile> upload(List<MultipartFile> files) throws IOException {

        List<MediaFile> mediaFileResponses = new ArrayList<> ( );
        if ( files.isEmpty ( ) ) {
             throw new ResourceNotFoundException ( "No files uploaded" );
        }
        for (MultipartFile file : files) {
            // 1. Detect type
            log.info ( "1. Detecting type" );
            String contentType = file.getContentType ( );

            String subFolder = determineSubFolder ( contentType, file.getOriginalFilename ( ) );
            // 2. Generate unique name
            log.info ( "2. Generating unique name" );
            String fileName = UUID.randomUUID ( ) + "_" + file.getOriginalFilename ( );

            // 3. Save file
            log.info ( "3. Saving file" );
            Path target = Paths.get ( uploadDir, subFolder, fileName );
            Files.copy ( file.getInputStream ( ), target, StandardCopyOption.REPLACE_EXISTING );

            // 4. Save DB record (MediaFile entity)
            log.info ( "4. Saving DB record" );
            MediaFile mediaFile = mediaFileMapper.toEntity ( fileName, file.getOriginalFilename ( ), subFolder, contentType, file.getSize ( ) );
            MediaFile save = mediaFileRepository.save ( mediaFile );

            String url = baseUrl + "/uploads/" + subFolder + "/" + fileName;
            log.info ( "5. Adding to response" );
            log.info ( "File uploaded: {}, URL: {}", file.getOriginalFilename ( ), url );
            // 5. Add to response
            mediaFileResponses.add ( save );
        }
        return mediaFileResponses;
    }

    @Override
    public GenericResponse delete(Long mediaFileId) {
        MediaFile mediaFile = mediaFileRepository.findById ( mediaFileId ).orElseThrow ( () -> new ResourceNotFoundException ( "File not found" ) );
        mediaFileRepository.delete ( mediaFile);
        return GenericResponse.builder ( ).code ( "200" ).status ( "success" ).message ( "File deleted successfully" ).build ( );
    }

    @Override
    public GenericResponse getMediaFileById(Long mediaFileId) {
        MediaFile mediaFile = mediaFileRepository.findById ( mediaFileId ).orElseThrow ( () -> new ResourceNotFoundException ( "File not found" ) );
        String url = baseUrl + "/uploads/" + mediaFile.getFilePath();
        MediaFileResponse mediaFileResponse = mediaFileMapper.toResponse(mediaFile,url);
        return GenericResponse.builder ( ).body ( mediaFileResponse ).code ( "200" ).status ( "success" ).build ( );
    }


    private String determineSubFolder(String contentType, String originalFileName) {
        if ( contentType != null ) {
            if ( contentType.startsWith ( "image/" ) ) {
                return "photos";
            } else if ( contentType.equals ( "application/pdf" ) ) {
                return "pdfs";
            } else if ( contentType.equals ( "application/msword" ) ||
                    contentType.equals ( "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ) ) {
                return "docs";
            }
        }
        // Fallback: check file extension if contentType is null or unknown
        String lowerName = originalFileName.toLowerCase ( );
        if ( lowerName.endsWith ( ".jpg" ) || lowerName.endsWith ( ".jpeg" ) || lowerName.endsWith ( ".png" ) || lowerName.endsWith ( ".gif" ) ||
                lowerName.endsWith(".webp") ) {
            return "photos";
        } else if ( lowerName.endsWith ( ".pdf" ) ) {
            return "pdfs";
        } else if ( lowerName.endsWith ( ".doc" ) || lowerName.endsWith ( ".docx" ) ) {
            return "docs";
        }
        throw new IllegalArgumentException ( "Unsupported file type: " + contentType + " / " + originalFileName );
    }


}
