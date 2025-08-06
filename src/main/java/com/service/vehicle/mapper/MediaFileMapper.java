package com.service.vehicle.mapper;


import com.service.vehicle.entity.MediaFile;
import com.service.vehicle.response.MediaFileResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MediaFileMapper {
    @Value("${file.base-url}")
    private String baseUrl;
    public MediaFileResponse toResponse(MediaFile save) {
        MediaFileResponse response = new MediaFileResponse ( );
        response.setId ( save.getId ( ) );
        response.setFileName ( save.getFileName ( ) );
        response.setOriginalFileName ( save.getOriginalFileName ( ) );
        response.setFilePath ( save.getFilePath ( ) );
        response.setMediaType ( save.getMediaType ( ) );
        response.setSize ( save.getSize ( ) );
        String url = baseUrl + "/uploads/" + save.getFilePath ( );
        response.setFileUrl (  url );
        return response;
    }

    public MediaFile toEntity(String fileName, String originalFilename, String subFolder, String contentType, long size) {
        MediaFile mediaFile = new MediaFile ( );
        mediaFile.setFileName ( fileName );
        mediaFile.setOriginalFileName ( originalFilename );
        mediaFile.setFilePath ( subFolder + "/" + fileName );
        mediaFile.setMediaType ( contentType );
        mediaFile.setSize ( size);
        return mediaFile;
    }
}
