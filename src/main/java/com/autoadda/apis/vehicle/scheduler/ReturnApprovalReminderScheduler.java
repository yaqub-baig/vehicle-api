package com.autoadda.apis.vehicle.scheduler;

import com.autoadda.apis.vehicle.dao.VehicleBorrowingRepository;
import com.autoadda.apis.vehicle.dao.VehicleDetailsRepository;
import com.autoadda.apis.vehicle.entity.VehicleBorrowing;
import com.autoadda.apis.vehicle.enums.BorrowingStatus;
import com.autoadda.apis.vehicle.service.impl.BorrowingNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReturnApprovalReminderScheduler {

    private final VehicleBorrowingRepository borrowingRepository;
    private final VehicleDetailsRepository vehicleRepository;
    private final BorrowingNotificationService notificationService;

    @Scheduled(fixedRateString = "${vehicle.borrowing.return-reminder.interval-ms:1800000}")
    public void sendReturnApprovalReminders() {
        List<VehicleBorrowing> pending =
                borrowingRepository.findByStatusAndIsDeletedFalse(BorrowingStatus.RETURN_INITIATED);

        log.info("Return approval reminder sweep: {} borrowing(s) pending owner approval", pending.size());

        for (VehicleBorrowing borrowing : pending) {
            try {
                vehicleRepository.findById(borrowing.getVehicleId())
                        .ifPresentOrElse(
                                vehicle -> notificationService.sendReturnApprovalReminderNotification(borrowing, vehicle),
                                () -> log.warn("Vehicle {} not found; skipping return reminder for borrowing {}",
                                        borrowing.getVehicleId(), borrowing.getId()));
            } catch (Exception e) {
                log.error("Failed to process return reminder for borrowing {}: {}",
                        borrowing.getId(), e.getMessage(), e);
            }
        }
    }
}
