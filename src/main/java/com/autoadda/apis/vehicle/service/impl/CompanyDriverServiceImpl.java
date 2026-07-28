package com.autoadda.apis.vehicle.service.impl;

import com.autoadda.apis.vehicle.clients.AuthClient;
import com.autoadda.apis.vehicle.dao.CompanyDriverRepository;
import com.autoadda.apis.vehicle.dao.CompanyRepository;
import com.autoadda.apis.vehicle.entity.Company;
import com.autoadda.apis.vehicle.entity.CompanyDriver;
import com.autoadda.apis.vehicle.exception.ResourceNotFoundException;
import com.autoadda.apis.vehicle.exception.UnauthorizedAccessException;
import com.autoadda.apis.vehicle.request.AddCompanyDriverRequest;
import com.autoadda.apis.vehicle.response.CompanyDriverResponse;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.response.UserResponse;
import com.autoadda.apis.vehicle.service.CompanyDriverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyDriverServiceImpl implements CompanyDriverService {

    private static final String ERROR = "error";
    private static final String SUCCESS = "success";
    private static final String COMPANY_NOT_FOUND = "Company not found";

    private final CompanyDriverRepository companyDriverRepository;
    private final CompanyRepository companyRepository;
    private final AuthClient authClient;

    @Override
    @Transactional
    public GenericResponse addDriver(Long companyId, Integer callerId, AddCompanyDriverRequest request) {
        requireCompanyOwner(companyId, callerId);

        Integer driverUserId = request.getDriverUserId();
        log.info("Company {} adding driver {}", companyId, driverUserId);

        try {
            UserResponse driver = authClient.getUser(driverUserId);
            if (driver == null) {
                return GenericResponse.builder()
                        .status(ERROR)
                        .code("404")
                        .message("Driver not found")
                        .build();
            }
        } catch (Exception e) {
            log.error("Error fetching driver {}: {}", driverUserId, e.getMessage());
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("404")
                    .message("Driver not found")
                    .build();
        }

        Optional<CompanyDriver> existingOpt = companyDriverRepository.findByCompanyIdAndDriverUserId(companyId, driverUserId);

        if (existingOpt.isPresent()) {
            CompanyDriver existing = existingOpt.get();
            if (Boolean.FALSE.equals(existing.getIsDeleted())) {
                return GenericResponse.builder()
                        .status(ERROR)
                        .code("400")
                        .message("This user is already a driver for this company")
                        .build();
            }
            existing.setIsDeleted(false);
            CompanyDriver saved = companyDriverRepository.save(existing);
            return GenericResponse.builder()
                    .status(SUCCESS)
                    .code("200")
                    .message("Driver added")
                    .body(toResponse(saved))
                    .build();
        }

        CompanyDriver companyDriver = CompanyDriver.builder()
                .companyId(companyId)
                .driverUserId(driverUserId)
                .isDeleted(false)
                .build();
        CompanyDriver saved = companyDriverRepository.save(companyDriver);

        log.info("Driver {} added for company {}", driverUserId, companyId);

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("201")
                .message("Driver added")
                .body(toResponse(saved))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse removeDriver(Long companyId, Integer callerId, Integer driverUserId) {
        requireCompanyOwner(companyId, callerId);

        log.info("Company {} removing driver {}", companyId, driverUserId);

        Optional<CompanyDriver> existingOpt = companyDriverRepository.findByCompanyIdAndDriverUserId(companyId, driverUserId);
        if (existingOpt.isEmpty() || Boolean.TRUE.equals(existingOpt.get().getIsDeleted())) {
            return GenericResponse.builder()
                    .status(ERROR)
                    .code("404")
                    .message("Driver not found")
                    .build();
        }

        CompanyDriver existing = existingOpt.get();
        existing.setIsDeleted(true);
        companyDriverRepository.save(existing);

        log.info("Driver {} removed for company {}", driverUserId, companyId);

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Driver removed")
                .build();
    }

    @Override
    public GenericResponse getDrivers(Long companyId, Integer callerId) {
        requireCompanyOwner(companyId, callerId);

        log.info("Fetching drivers for company {}", companyId);

        List<CompanyDriver> drivers =
                companyDriverRepository.findByCompanyIdAndIsDeletedFalseOrderByCreatedAtDesc(companyId);

        return GenericResponse.builder()
                .status(SUCCESS)
                .code("200")
                .message("Drivers retrieved successfully")
                .body(drivers.stream().map(this::toResponse).toList())
                .build();
    }

    private void requireCompanyOwner(Long companyId, Integer callerId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(COMPANY_NOT_FOUND));
        if (!callerId.equals(company.getCompanyOwnerId())) {
            throw new UnauthorizedAccessException("Only the company owner can manage drivers");
        }
    }

    private CompanyDriverResponse toResponse(CompanyDriver entity) {
        CompanyDriverResponse.CompanyDriverResponseBuilder builder = CompanyDriverResponse.builder()
                .id(entity.getId())
                .companyId(entity.getCompanyId())
                .driverUserId(entity.getDriverUserId())
                .createdAt(entity.getCreatedAt());

        try {
            UserResponse driver = authClient.getUser(entity.getDriverUserId());
            if (driver != null) {
                builder.driverName(driver.getName());
                builder.driverMobileNumber(driver.getMobileNumber());
            }
        } catch (Exception e) {
            log.warn("Failed to fetch driver details for ID {}: {}", entity.getDriverUserId(), e.getMessage());
        }

        return builder.build();
    }
}
