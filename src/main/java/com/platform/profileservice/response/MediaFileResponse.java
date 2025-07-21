package com.platform.profileservice.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MediaFileResponse {
    private Long id;
    private String fileName;
    private String originalFileName;
    private String filePath;
    private String mediaType;
    private Long size;
    private String fileUrl;
}
