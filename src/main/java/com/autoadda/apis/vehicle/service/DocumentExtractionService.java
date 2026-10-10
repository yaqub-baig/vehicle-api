package com.autoadda.apis.vehicle.service;

import com.autoadda.apis.vehicle.response.GenericResponse;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentExtractionService {

    GenericResponse extract(String documentType, MultipartFile front, MultipartFile back);
}
