package com.autoadda.apis.vehicle.service.impl;

import com.autoadda.apis.vehicle.dao.PlateCodeRepository;
import com.autoadda.apis.vehicle.entity.PlateCode;
import com.autoadda.apis.vehicle.exception.ConflictException;
import com.autoadda.apis.vehicle.exception.ResourceNotFoundException;
import com.autoadda.apis.vehicle.mapper.PlateCodeMapper;
import com.autoadda.apis.vehicle.request.PlateCodeRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlateCodeService {

    private static final String SUCCESS = "success";
    private static final String PLATE_CODE_NOT_FOUND = "Plate code not found";
    private static final String DUPLICATE_CODE = "This plate code already exists for this plate type";

    private final PlateCodeRepository plateCodeRepository;
    private final PlateCodeMapper plateCodeMapper;

    @Transactional
    public GenericResponse createPlateCode(PlateCodeRequest request) {
        plateCodeRepository.findByPlateTypeIdAndCodeIgnoreCase(request.getPlateTypeId(), request.getCode())
                .ifPresent(existing -> {
                    throw new ConflictException(DUPLICATE_CODE);
                });
        PlateCode saved = plateCodeRepository.save(plateCodeMapper.toEntity(request));
        return GenericResponse.builder()
                .status(SUCCESS)
                .code("201")
                .message("Plate code created successfully")
                .body(plateCodeMapper.toResponse(saved))
                .build();
    }

    /**
     * GET /plate-codes?plateTypeId=1 to power a type -> code cascading dropdown.
     */
    public GenericResponse getAllPlateCodes(Long plateTypeId) {
        List<PlateCode> plateCodes = plateTypeId != null
                ? plateCodeRepository.findByPlateTypeIdAndIsActiveTrueOrderByDisplayOrderAscIdAsc(plateTypeId)
                : plateCodeRepository.findByIsActiveTrueOrderByDisplayOrderAscIdAsc();
        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Plate codes retrieved successfully")
                .body(plateCodes.stream().map(plateCodeMapper::toResponse).toList())
                .build();
    }

    public GenericResponse getPlateCodeById(Long id) {
        PlateCode plateCode = findOrThrow(id);
        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Plate code retrieved successfully")
                .body(plateCodeMapper.toResponse(plateCode))
                .build();
    }

    @Transactional
    public GenericResponse updatePlateCode(Long id, PlateCodeRequest request) {
        PlateCode plateCode = findOrThrow(id);
        plateCodeRepository.findByPlateTypeIdAndCodeIgnoreCase(request.getPlateTypeId(), request.getCode())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new ConflictException(DUPLICATE_CODE);
                    }
                });
        plateCodeMapper.applyUpdate(plateCode, request);
        PlateCode saved = plateCodeRepository.save(plateCode);
        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Plate code updated successfully")
                .body(plateCodeMapper.toResponse(saved))
                .build();
    }

    @Transactional
    public GenericResponse deletePlateCode(Long id) {
        PlateCode plateCode = findOrThrow(id);
        plateCode.setIsActive(false);
        plateCodeRepository.save(plateCode);
        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Plate code deleted successfully")
                .build();
    }

    private PlateCode findOrThrow(Long id) {
        return plateCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PLATE_CODE_NOT_FOUND));
    }
}
