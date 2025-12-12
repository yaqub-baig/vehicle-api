package com.autoadda.apis.vehicle.service.impl;

import com.autoadda.apis.vehicle.dao.CompanyRepository;
import com.autoadda.apis.vehicle.dao.MediaFileRepository;
import com.autoadda.apis.vehicle.dao.VehicleDetailsRepository;
import com.autoadda.apis.vehicle.entity.Company;
import com.autoadda.apis.vehicle.entity.MediaFile;
import com.autoadda.apis.vehicle.entity.VehicleDetails;
import com.autoadda.apis.vehicle.exception.ResourceNotFoundException;
import com.autoadda.apis.vehicle.mapper.MediaFileMapper;
import com.autoadda.apis.vehicle.mapper.VehicleMapper;
import com.autoadda.apis.vehicle.request.VehicleRegistrationRequest;
import com.autoadda.apis.vehicle.response.CompanyResponse;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.response.MediaFileResponse;
import com.autoadda.apis.vehicle.response.VehicleDetailResponse;
import com.autoadda.apis.vehicle.service.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleServiceImpl implements VehicleService {
    public static final String VEHICLE_NOT_FOUND = "Vehicle not found";
    public static final String SUCCESS = "success";
    private final VehicleDetailsRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;
    private final MediaFileMapper mediaFileMapper;
    private final CompanyService companyService;
    private final MediaFileRepository mediaFileRepository;
    private final CompanyRepository companyRepository;
    private final MediaFileCleanupService mediaFileCleanupService;

    @Override
    public GenericResponse addVehicleDetails(VehicleRegistrationRequest request, String autoAddaId) {
        Optional<VehicleDetails> byChassisNo = vehicleRepository.findByChassisNo(request.getChassisNo());
        if (byChassisNo.isPresent()) {
            throw new ResourceNotFoundException("Vehicle chassis number already exists");
        }
        VehicleDetails entity = vehicleMapper.toEntity(request);
        entity.setCustomerId(Integer.parseInt(autoAddaId));
        if (request.getCompanyId() != null) {
            entity.setCompanyId(request.getCompanyId());
        }
        if (request.getMulkiaImageFront() != null) {
            MediaFile mulkiaImageFront = mediaFileRepository.findById(request.getMulkiaImageFront()).orElseThrow(() -> new ResourceNotFoundException("Mulkia image not found"));
            entity.setMulkiaImageFront(mulkiaImageFront);
        }
        if (request.getMulkiaImageBack() != null) {
            MediaFile mulkiaImageBack = mediaFileRepository.findById(request.getMulkiaImageBack()).orElseThrow(() -> new ResourceNotFoundException("Mulkia image not found"));
            entity.setMulkiaImageBack(mulkiaImageBack);
        }
        if (request.getVehicleImages() != null) {
            List<MediaFile> vehicleImages = request.getVehicleImages().stream()
                    .map(id -> mediaFileRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Vehicle image not found"))).toList();
            entity.setVehicleImages(vehicleImages);
        }
        vehicleRepository.save(entity);
        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Vehicle added successfully")
                .build();
    }

    @Override
    public GenericResponse getVehicleDetails(String autoAddaId) {

        List<VehicleDetails> byCustomerMobileNumber = vehicleRepository.findByCustomerId(Integer.parseInt(autoAddaId));
        log.info("2. Vehicle list size: {}", byCustomerMobileNumber.size());
        List<VehicleDetailResponse> list1 = byCustomerMobileNumber.stream()
                .sorted((o1, o2) -> o2.getId().compareTo(o1.getId()))
                .filter(vehicleDetails -> !vehicleDetails.isDeleted())
                .map(vehicleMapper::toResponse)
                .map(this::addCompanyResponse)
                .toList();

        return GenericResponse.builder()
                .body(list1)
                .code("200")
                .message(SUCCESS)
                .status(SUCCESS)
                .build();
    }

    private VehicleDetailResponse addCompanyResponse(VehicleDetailResponse vehicleDetailResponse) {

        if (vehicleDetailResponse.getCompanyId() != null) {
            CompanyResponse company = companyService.getCompanyByIdV2(vehicleDetailResponse.getCompanyId());
            vehicleDetailResponse.setCompanyResponse(company);
        }
        return vehicleDetailResponse;
    }

    @Override
    public GenericResponse getVehicleDetailsByVehicleId(Integer vehicleId) {
        VehicleDetails vehicleDetails = vehicleRepository.findById(vehicleId).orElseThrow(() -> new ResourceNotFoundException(VEHICLE_NOT_FOUND));
        if (vehicleDetails.isDeleted()) {
            throw new ResourceNotFoundException("Vehicle deleted");
        }

        VehicleDetailResponse response = vehicleMapper.toResponse(vehicleDetails);
        VehicleDetailResponse responded = addCompanyResponse(response);
        return GenericResponse.builder().body(responded)
                .code("200")
                .message(SUCCESS).build();

    }

    @Override
    public GenericResponse getVehicleImagesByVehicleId(Integer vehicleId) {
        List<MediaFileResponse> list = vehicleRepository.findImagesByVehicleId(vehicleId)
                .stream().map(mediaFileMapper::toResponse).toList();
        return GenericResponse.builder().body(list.isEmpty() ? "No images found" : list)
                .code("200").message(SUCCESS).build();
    }

    @Override
    @Transactional
    public GenericResponse updateVehicleDetails(Integer vehicleId, VehicleRegistrationRequest request, String userId) {
        VehicleDetails existing = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));

        vehicleMapper.updateEntity(existing, request);

        List<Long> filesToCleanup = new ArrayList<>();

        handleMulkiaImageFront(request, existing, filesToCleanup);
        handleMulkiaImageBack(request, existing, filesToCleanup);
        handleVehicleImages(request, existing, filesToCleanup);
        VehicleDetails save = vehicleRepository.save(existing);

        if (!filesToCleanup.isEmpty()) {
            mediaFileCleanupService.scheduleMediaFileCleanup(filesToCleanup);
        }

        VehicleDetailResponse response = vehicleMapper.toResponse(save);
        VehicleDetailResponse responded = addCompanyResponse(response);
        return GenericResponse.builder()
                .body(responded)
                .code("200")
                .message(SUCCESS)
                .status(SUCCESS)
                .build();
    }

    @Override
    public GenericResponse deleteVehicleDetails(Integer vehicleId, String userId) {
        VehicleDetails vehicleDetails = vehicleRepository.findById(vehicleId).orElseThrow(() -> new ResourceNotFoundException(VEHICLE_NOT_FOUND));
        vehicleDetails.setDeleted(true);
        vehicleRepository.save(vehicleDetails);
        return GenericResponse.builder().body("Vehicle deleted").code("200").message(SUCCESS).status(SUCCESS).build();
    }

    @Override
    public VehicleDetailResponse getVehicleDetailsByVehicleIdForIsc(Integer vehicleId) {
        VehicleDetails vehicleDetails = vehicleRepository.findById(vehicleId).orElseThrow(() -> new ResourceNotFoundException(VEHICLE_NOT_FOUND));
        if (vehicleDetails.isDeleted()) {
            throw new ResourceNotFoundException("Vehicle deleted");
        }
        return vehicleMapper.toResponse(vehicleDetails);

    }

    @Override
    public GenericResponse getVehiclesByCompanyId(Long companyId) {
        List<VehicleDetails> byCompanyId = vehicleRepository.findByCompanyId(companyId);

        List<VehicleDetailResponse> list1 = byCompanyId.stream()
                .sorted((o1, o2) -> o2.getId().compareTo(o1.getId()))
                .filter(vehicleDetails -> !vehicleDetails.isDeleted())
                .map(vehicleMapper::toResponse)
                .map(this::addCompanyResponse)
                .toList();

        return GenericResponse.builder()
                .body(list1)
                .code("200")
                .message(list1.isEmpty() ? "No vehicles found" : SUCCESS)
                .status(SUCCESS)
                .build();

    }

    @Override
    public GenericResponse getVehiclesByOwnerId(Long ownerId) {
        List<VehicleDetails> byOwnerId = vehicleRepository.findByCustomerId(ownerId.intValue());

        List<VehicleDetailResponse> list1 = byOwnerId.stream()
                .sorted((o1, o2) -> o2.getId().compareTo(o1.getId()))
                .filter(vehicleDetails -> !vehicleDetails.isDeleted())
                .map(vehicleMapper::toResponse)
                .map(this::addCompanyResponse)
                .toList();

        return GenericResponse.builder()
                .body(list1)
                .code("200")
                .message(list1.isEmpty() ? "No vehicles found" : SUCCESS)
                .status(SUCCESS)
                .build();
    }

    @Override
    public GenericResponse getCompanyVehiclesByOwnerId(Integer ownerId) {
        Optional<Company> companyOptional = companyRepository.findByCompanyOwnerId(ownerId);

        if (companyOptional.isEmpty()) {
            return GenericResponse.builder()
                    .body(new ArrayList<>())
                    .code("200")
                    .message("No company found for this owner")
                    .status(SUCCESS)
                    .build();
        }

        Company company = companyOptional.get();
        List<VehicleDetails> companyVehicles = vehicleRepository.findByCompanyId(company.getId());

        List<VehicleDetailResponse> vehicleResponses = companyVehicles.stream()
                .sorted((o1, o2) -> o2.getId().compareTo(o1.getId()))
                .filter(vehicleDetails -> !vehicleDetails.isDeleted())
                .map(vehicleMapper::toResponse)
                .map(this::addCompanyResponse)
                .toList();

        return GenericResponse.builder()
                .body(vehicleResponses)
                .code("200")
                .message(vehicleResponses.isEmpty() ? "No vehicles found for this company" : SUCCESS)
                .status(SUCCESS)
                .build();
    }

    private void handleMulkiaImageFront(VehicleRegistrationRequest request, VehicleDetails existing, List<Long> filesToCleanup) {
        if (request.getMulkiaImageFront() != null) {
            Long prevImageId = existing.getMulkiaImageFront() != null ? existing.getMulkiaImageFront().getId() : null;
            MediaFile mediaFile = mediaFileRepository.findById(request.getMulkiaImageFront())
                    .orElseThrow(() -> new ResourceNotFoundException("Mulkia front image not found"));
            existing.setMulkiaImageFront(mediaFile);

            if (prevImageId != null && !prevImageId.equals(mediaFile.getId())) {
                filesToCleanup.add(prevImageId);
            }
        }
    }

    private void handleMulkiaImageBack(VehicleRegistrationRequest request, VehicleDetails existing, List<Long> filesToCleanup) {
        if (request.getMulkiaImageBack() != null) {
            Long prevImageId = existing.getMulkiaImageBack() != null ? existing.getMulkiaImageBack().getId() : null;
            MediaFile mediaFile = mediaFileRepository.findById(request.getMulkiaImageBack())
                    .orElseThrow(() -> new ResourceNotFoundException("Mulkia back image not found"));
            existing.setMulkiaImageBack(mediaFile);

            if (prevImageId != null && !prevImageId.equals(mediaFile.getId())) {
                filesToCleanup.add(prevImageId);
            }
        }
    }

    private void handleVehicleImages(VehicleRegistrationRequest request, VehicleDetails existing, List<Long> filesToCleanup) {
        if (request.getVehicleImages() != null && !request.getVehicleImages().isEmpty()) {
            List<Long> oldVehicleImageIds = existing.getVehicleImages().stream()
                    .map(MediaFile::getId)
                    .toList();
            if (!oldVehicleImageIds.isEmpty()) {
                filesToCleanup.addAll(oldVehicleImageIds);
            }

            List<MediaFile> newVehicleImages = new ArrayList<>();
            request.getVehicleImages().forEach(imageId -> {
                MediaFile mediaFile = mediaFileRepository.findById(imageId)
                        .orElseThrow(() -> new ResourceNotFoundException("Vehicle image not found"));
                newVehicleImages.add(mediaFile);
            });

            existing.getVehicleImages().clear();
            existing.getVehicleImages().addAll(newVehicleImages);
        }
    }

    @Override
    public boolean validateChassisNo(String chassisNo) {
        if (chassisNo == null || chassisNo.trim().isEmpty()) {
            return false;
        }
        Optional<VehicleDetails> byChassisNo = vehicleRepository.findByChassisNo(chassisNo);
        return byChassisNo.isPresent();
    }

    @Override
    public GenericResponse getAllVehicle() {
        List<VehicleDetails> allVehicles = vehicleRepository.findAll();
        log.info("2. Vehicle list size: {}", allVehicles.size());
        List<VehicleDetailResponse> list1 = allVehicles.stream()
                .sorted((o1, o2) -> o2.getId().compareTo(o1.getId()))
//                .filter(vehicleDetails -> !vehicleDetails.isDeleted())
                .map(vehicleMapper::toResponse)
                .map(this::addCompanyResponse)
                .toList();

        return GenericResponse.builder()
                .body(list1)
                .code("200")
                .message(SUCCESS)
                .status(SUCCESS)
                .build();
    }
}
