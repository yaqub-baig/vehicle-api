package com.autoadda.apis.vehicle.service.impl;

import com.autoadda.apis.vehicle.dao.MediaFileRepository;
import com.autoadda.apis.vehicle.dao.VehicleDetailsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaFileCleanupService {

    private final MediaFileRepository mediaFileRepository;
    private final VehicleDetailsRepository vehicleDetailsRepository;


    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void scheduleMediaFileCleanup(List<Long> mediaFileIds) {
        try {
            Thread.sleep(3000);

            for (Long fileId : mediaFileIds) {
                try {
                    if (!isMediaFileStillReferenced(fileId)) {
                        mediaFileRepository.deleteById(fileId);
                        log.info("Successfully cleaned up media file: {}", fileId);
                    } else {
                        log.info("Media file {} still referenced, skipping cleanup", fileId);
                    }
                } catch (Exception e) {
                    log.error("Error cleaning up media file {}: {}", fileId, e.getMessage());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Media file cleanup interrupted: {}", e.getMessage());
        }
    }

    private boolean isMediaFileStillReferenced(Long fileId) {
        // Check vehicle references
        long mulkiaFrontCount = vehicleDetailsRepository.countByMulkiaImageFrontId(fileId);
        long mulkiaBackCount = vehicleDetailsRepository.countByMulkiaImageBackId(fileId);
        long vehicleImageCount = vehicleDetailsRepository.countByVehicleImageId(fileId);
        long totalReferences = mulkiaFrontCount + mulkiaBackCount + vehicleImageCount;
        return totalReferences > 0;
    }
}
