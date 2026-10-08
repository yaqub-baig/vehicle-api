package com.autoadda.apis.vehicle.service.impl;

import com.autoadda.apis.vehicle.dao.PlateCodeRepository;
import com.autoadda.apis.vehicle.dao.PlateSourceRepository;
import com.autoadda.apis.vehicle.dao.PlateTypeRepository;
import com.autoadda.apis.vehicle.entity.PlateCode;
import com.autoadda.apis.vehicle.entity.PlateSource;
import com.autoadda.apis.vehicle.entity.PlateType;
import com.autoadda.apis.vehicle.exception.ConflictException;
import com.autoadda.apis.vehicle.exception.ResourceNotFoundException;
import com.autoadda.apis.vehicle.mapper.PlateSourceMapper;
import com.autoadda.apis.vehicle.request.PlateSourceRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.response.PlateMasterTreeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlateSourceService {

    private static final String SUCCESS = "success";
    private static final String PLATE_SOURCE_NOT_FOUND = "Plate source not found";

    private final PlateSourceRepository plateSourceRepository;
    private final PlateTypeRepository plateTypeRepository;
    private final PlateCodeRepository plateCodeRepository;
    private final PlateSourceMapper plateSourceMapper;

    @Transactional
    public GenericResponse createPlateSource(PlateSourceRequest request) {
        plateSourceRepository.findByCodeIgnoreCase(request.getCode())
                .ifPresent(existing -> {
                    throw new ConflictException("A plate source with this code already exists");
                });
        PlateSource saved = plateSourceRepository.save(plateSourceMapper.toEntity(request));
        return GenericResponse.builder()
                .status(SUCCESS)
                .code("201")
                .message("Plate source created successfully")
                .body(plateSourceMapper.toResponse(saved))
                .build();
    }

    public GenericResponse getAllPlateSources() {
        List<PlateSource> plateSources = plateSourceRepository.findByIsActiveTrueOrderByDisplayOrderAscIdAsc();
        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Plate sources retrieved successfully")
                .body(plateSources.stream().map(plateSourceMapper::toResponse).toList())
                .build();
    }

    /**
     * Full active source -> type -> code hierarchy in one call. Loads each level with a single
     * query and groups in memory rather than walking the tree per row.
     */
    public GenericResponse getPlateMasterTree() {
        Map<Long, List<PlateCode>> codesByTypeId = plateCodeRepository.findByIsActiveTrueOrderByDisplayOrderAscIdAsc()
                .stream()
                .collect(Collectors.groupingBy(code -> code.getPlateType().getId()));
        Map<Long, List<PlateType>> typesBySourceId = plateTypeRepository.findByIsActiveTrueOrderByDisplayOrderAscIdAsc()
                .stream()
                .collect(Collectors.groupingBy(type -> type.getPlateSource().getId()));

        List<PlateMasterTreeResponse> tree = plateSourceRepository.findByIsActiveTrueOrderByDisplayOrderAscIdAsc()
                .stream()
                .map(source -> PlateMasterTreeResponse.builder()
                        .id(source.getId())
                        .code(source.getCode())
                        .name(source.getName())
                        .plateTypes(typesBySourceId.getOrDefault(source.getId(), List.of()).stream()
                                .map(type -> PlateMasterTreeResponse.PlateTypeNode.builder()
                                        .id(type.getId())
                                        .code(type.getCode())
                                        .name(type.getName())
                                        .plateCodes(codesByTypeId.getOrDefault(type.getId(), List.of()).stream()
                                                .map(code -> PlateMasterTreeResponse.PlateCodeNode.builder()
                                                        .id(code.getId())
                                                        .code(code.getCode())
                                                        .build())
                                                .toList())
                                        .build())
                                .toList())
                        .build())
                .toList();

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Plate master data retrieved successfully")
                .body(tree)
                .build();
    }

    public GenericResponse getPlateSourceById(Long id) {
        PlateSource plateSource = findOrThrow(id);
        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Plate source retrieved successfully")
                .body(plateSourceMapper.toResponse(plateSource))
                .build();
    }

    @Transactional
    public GenericResponse updatePlateSource(Long id, PlateSourceRequest request) {
        PlateSource plateSource = findOrThrow(id);
        plateSourceRepository.findByCodeIgnoreCase(request.getCode())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new ConflictException("A plate source with this code already exists");
                    }
                });
        plateSourceMapper.applyUpdate(plateSource, request);
        PlateSource saved = plateSourceRepository.save(plateSource);
        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Plate source updated successfully")
                .body(plateSourceMapper.toResponse(saved))
                .build();
    }

    @Transactional
    public GenericResponse deletePlateSource(Long id) {
        PlateSource plateSource = findOrThrow(id);
        plateSource.setIsActive(false);
        plateSourceRepository.save(plateSource);
        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Plate source deleted successfully")
                .build();
    }

    private PlateSource findOrThrow(Long id) {
        return plateSourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PLATE_SOURCE_NOT_FOUND));
    }
}
