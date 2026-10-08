package com.autoadda.apis.vehicle.service.impl;

import com.autoadda.apis.vehicle.dao.PlateTypeRepository;
import com.autoadda.apis.vehicle.entity.PlateType;
import com.autoadda.apis.vehicle.exception.ConflictException;
import com.autoadda.apis.vehicle.exception.ResourceNotFoundException;
import com.autoadda.apis.vehicle.mapper.PlateTypeMapper;
import com.autoadda.apis.vehicle.request.PlateTypeRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlateTypeService {

    private static final String SUCCESS = "success";
    private static final String PLATE_TYPE_NOT_FOUND = "Plate type not found";
    private static final String DUPLICATE_CODE = "A plate type with this code already exists for this plate source";

    private final PlateTypeRepository plateTypeRepository;
    private final PlateTypeMapper plateTypeMapper;

    @Transactional
    public GenericResponse createPlateType(PlateTypeRequest request) {
        plateTypeRepository.findByPlateSourceIdAndCodeIgnoreCase(request.getPlateSourceId(), request.getCode())
                .ifPresent(existing -> {
                    throw new ConflictException(DUPLICATE_CODE);
                });
        PlateType saved = plateTypeRepository.save(plateTypeMapper.toEntity(request));
        return GenericResponse.builder()
                .status(SUCCESS)
                .code("201")
                .message("Plate type created successfully")
                .body(plateTypeMapper.toResponse(saved))
                .build();
    }

    /**
     * GET /plate-types?plateSourceId=1 to power a source -> type cascading dropdown.
     */
    public GenericResponse getAllPlateTypes(Long plateSourceId) {
        List<PlateType> plateTypes = plateSourceId != null
                ? plateTypeRepository.findByPlateSourceIdAndIsActiveTrueOrderByDisplayOrderAscIdAsc(plateSourceId)
                : plateTypeRepository.findByIsActiveTrueOrderByDisplayOrderAscIdAsc();
        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Plate types retrieved successfully")
                .body(plateTypes.stream().map(plateTypeMapper::toResponse).toList())
                .build();
    }

    public GenericResponse getPlateTypeById(Long id) {
        PlateType plateType = findOrThrow(id);
        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Plate type retrieved successfully")
                .body(plateTypeMapper.toResponse(plateType))
                .build();
    }

    @Transactional
    public GenericResponse updatePlateType(Long id, PlateTypeRequest request) {
        PlateType plateType = findOrThrow(id);
        plateTypeRepository.findByPlateSourceIdAndCodeIgnoreCase(request.getPlateSourceId(), request.getCode())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new ConflictException(DUPLICATE_CODE);
                    }
                });
        plateTypeMapper.applyUpdate(plateType, request);
        PlateType saved = plateTypeRepository.save(plateType);
        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Plate type updated successfully")
                .body(plateTypeMapper.toResponse(saved))
                .build();
    }

    @Transactional
    public GenericResponse deletePlateType(Long id) {
        PlateType plateType = findOrThrow(id);
        plateType.setIsActive(false);
        plateTypeRepository.save(plateType);
        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Plate type deleted successfully")
                .build();
    }

    private PlateType findOrThrow(Long id) {
        return plateTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PLATE_TYPE_NOT_FOUND));
    }
}
