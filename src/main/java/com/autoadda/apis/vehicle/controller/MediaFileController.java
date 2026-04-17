package com.autoadda.apis.vehicle.controller;


import com.autoadda.apis.vehicle.entity.MediaFile;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.MediaFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
@CrossOrigin("*")
@RestController
@RequestMapping("/mediafiles")
@RequiredArgsConstructor
public class MediaFileController {
    private final MediaFileService mediaFileService;
    private static final long MAX_VIDEO_SIZE = 30L * 1024 * 1024;
    private static final long MAX_OTHER_SIZE = 10L * 1024 * 1024;

    @PostMapping(value = "/upload", consumes = {"multipart/form-data"})
    public List<MediaFile> upload(@RequestPart("files") List<MultipartFile> files) throws IOException {
        for (MultipartFile file : files) {
            boolean isVideo = isVideoFile(file);
            long maxSize = isVideo ? MAX_VIDEO_SIZE : MAX_OTHER_SIZE;
            if (file.getSize() > maxSize) {
                throw new IllegalArgumentException(
                        "File size must be less than " + (isVideo ? "30MB" : "10MB") + ": " + file.getOriginalFilename());
            }
        }
        return mediaFileService.upload ( files );
    }

    private boolean isVideoFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null && contentType.startsWith("video/")) {
            return true;
        }
        String name = file.getOriginalFilename();
        if (name != null) {
            String lower = name.toLowerCase();
            return lower.endsWith(".mp4") || lower.endsWith(".avi") || lower.endsWith(".mov")
                    || lower.endsWith(".mkv") || lower.endsWith(".webm");
        }
        return false;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GenericResponse> deleteImage(@PathVariable("id") Long id) {
        return ResponseEntity.ok ( mediaFileService.delete ( id ) );
    }

}
