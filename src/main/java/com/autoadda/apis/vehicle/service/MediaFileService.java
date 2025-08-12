package com.autoadda.apis.vehicle.service;



import com.autoadda.apis.vehicle.entity.MediaFile;
import com.autoadda.apis.vehicle.response.GenericResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface MediaFileService {

    List<MediaFile> upload(List<MultipartFile> files) throws IOException;

    GenericResponse delete(Long mediaFileId);

    GenericResponse getMediaFileById(Long mediaFileId);
}
