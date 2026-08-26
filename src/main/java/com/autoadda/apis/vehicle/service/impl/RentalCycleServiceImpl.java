package com.autoadda.apis.vehicle.service.impl;

import com.autoadda.apis.vehicle.dao.RentalCycleRepository;
import com.autoadda.apis.vehicle.entity.RentalCycle;
import com.autoadda.apis.vehicle.enums.RentalCycleStatus;
import com.autoadda.apis.vehicle.enums.RentalRateType;
import com.autoadda.apis.vehicle.enums.RentalType;
import com.autoadda.apis.vehicle.mapper.RentalCycleMapper;
import com.autoadda.apis.vehicle.request.RentalCycleDiscountRequest;
import com.autoadda.apis.vehicle.request.RentalCycleReturnRequest;
import com.autoadda.apis.vehicle.request.RentalCycleStartRequest;
import com.autoadda.apis.vehicle.response.GenericResponse;
import com.autoadda.apis.vehicle.service.RentalCycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RentalCycleServiceImpl implements RentalCycleService {

    private static final String ERROR = "error";
    private static final String SUCCESS = "success";
    private static final String CYCLE_NOT_FOUND = "Rental cycle not found";
    private static final long SECONDS_PER_HOUR = 3600L;
    private static final long SECONDS_PER_DAY = 86400L;

    private final RentalCycleRepository cycleRepository;
    private final RentalCycleMapper mapper;
    private final RentalNotificationService notificationService;

    @Override
    public GenericResponse getCycleById(Long cycleId, Integer callerId) {
        Optional<RentalCycle> cycleOpt = cycleRepository.findByIdAndIsDeletedFalse(cycleId);
        if (cycleOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(CYCLE_NOT_FOUND).build();
        }

        RentalCycle cycle = cycleOpt.get();
        GenericResponse authError = checkParticipant(cycle, callerId);
        if (authError != null) {
            return authError;
        }

        return GenericResponse.builder()
                .status(SUCCESS).code("200")
                .message("Rental cycle retrieved successfully")
                .body(mapper.toResponse(cycle))
                .build();
    }

    @Override
    public GenericResponse getCycleByRequestId(Long requestId, Integer callerId) {
        Optional<RentalCycle> cycleOpt = cycleRepository.findByRentalRequestIdAndIsDeletedFalse(requestId);
        if (cycleOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(CYCLE_NOT_FOUND).build();
        }

        RentalCycle cycle = cycleOpt.get();
        GenericResponse authError = checkParticipant(cycle, callerId);
        if (authError != null) {
            return authError;
        }

        return GenericResponse.builder()
                .status(SUCCESS).code("200")
                .message("Rental cycle retrieved successfully")
                .body(mapper.toResponse(cycle))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse setDiscount(Long cycleId, RentalCycleDiscountRequest request, Integer ownerId) {
        log.info("Setting discount on rental cycle {} by owner {}", cycleId, ownerId);

        Optional<RentalCycle> cycleOpt = cycleRepository.findByIdAndIsDeletedFalse(cycleId);
        if (cycleOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(CYCLE_NOT_FOUND).build();
        }

        RentalCycle cycle = cycleOpt.get();

        if (!cycle.getOwnerId().equals(ownerId)) {
            return GenericResponse.builder().status(ERROR).code("403").message("Only the owner can set the discount").build();
        }

        if (cycle.getStatus() == RentalCycleStatus.COMPLETED || cycle.getStatus() == RentalCycleStatus.CANCELLED) {
            return GenericResponse.builder().status(ERROR).code("400").message("Cannot change the discount on a finished rental").build();
        }

        if (request.getDiscountAmount().compareTo(BigDecimal.ZERO) < 0) {
            return GenericResponse.builder().status(ERROR).code("400").message("Discount amount cannot be negative").build();
        }

        cycle.setDiscountAmount(request.getDiscountAmount());
        RentalCycle saved = cycleRepository.save(cycle);

        return GenericResponse.builder()
                .status(SUCCESS).code("200")
                .message("Discount updated successfully")
                .body(mapper.toResponse(saved))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse startCycle(Long cycleId, RentalCycleStartRequest request, Integer renterId) {
        log.info("Starting rental cycle {} by renter {}", cycleId, renterId);

        Optional<RentalCycle> cycleOpt = cycleRepository.findByIdAndIsDeletedFalse(cycleId);
        if (cycleOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(CYCLE_NOT_FOUND).build();
        }

        RentalCycle cycle = cycleOpt.get();

        if (!cycle.getRenterId().equals(renterId)) {
            return GenericResponse.builder().status(ERROR).code("403").message("Only the renter can start this rental").build();
        }

        if (cycle.getStatus() != RentalCycleStatus.AWAITING_START) {
            return GenericResponse.builder().status(ERROR).code("400").message("Only a rental awaiting pickup can be started").build();
        }

        cycle.setStartOdometer(request.getStartOdometer());
        LocalDateTime startedAt = LocalDateTime.now();
        cycle.setStartedAt(startedAt);

        if (cycle.getRentalType() == RentalType.LIMITED) {
            // Binding commitment: server computes expectedReturnAt from the duration snapshotted
            // at accept time, ignoring anything the client sends in expectedReturnAt for this cycle.
            LocalDateTime computedExpectedReturnAt = cycle.getRateType() == RentalRateType.HOURLY
                    ? startedAt.plusHours(cycle.getDurationValue())
                    : startedAt.plusDays(cycle.getDurationValue());
            cycle.setExpectedReturnAt(computedExpectedReturnAt);
        } else {
            cycle.setExpectedReturnAt(request.getExpectedReturnAt());
        }

        cycle.setStatus(RentalCycleStatus.ACTIVE);
        RentalCycle saved = cycleRepository.save(cycle);

        log.info("Rental cycle {} started by renter {}", cycleId, renterId);

        notificationService.sendCycleStartedNotification(saved);

        return GenericResponse.builder()
                .status(SUCCESS).code("200")
                .message("Rental started. Enjoy your trip!")
                .body(mapper.toResponse(saved))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse returnCycle(Long cycleId, RentalCycleReturnRequest request, Integer renterId) {
        log.info("Returning rental cycle {} by renter {}", cycleId, renterId);

        Optional<RentalCycle> cycleOpt = cycleRepository.findByIdAndIsDeletedFalse(cycleId);
        if (cycleOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(CYCLE_NOT_FOUND).build();
        }

        RentalCycle cycle = cycleOpt.get();

        if (!cycle.getRenterId().equals(renterId)) {
            return GenericResponse.builder().status(ERROR).code("403").message("Only the renter can return this rental").build();
        }

        if (cycle.getStatus() != RentalCycleStatus.ACTIVE) {
            return GenericResponse.builder().status(ERROR).code("400").message("Only an active rental can be returned").build();
        }

        if (request.getEndOdometer() < cycle.getStartOdometer()) {
            return GenericResponse.builder().status(ERROR).code("400").message("End odometer cannot be less than the start odometer").build();
        }

        cycle.setEndOdometer(request.getEndOdometer());
        cycle.setActualReturnAt(LocalDateTime.now());
        computeFinalPrice(cycle);
        cycle.setStatus(RentalCycleStatus.COMPLETED);
        RentalCycle saved = cycleRepository.save(cycle);

        log.info("Rental cycle {} returned by renter {}; final price {}", cycleId, renterId, saved.getFinalPrice());

        notificationService.sendCycleReturnedNotification(saved);

        return GenericResponse.builder()
                .status(SUCCESS).code("200")
                .message("Rental completed successfully")
                .body(mapper.toResponse(saved))
                .build();
    }

    @Override
    @Transactional
    public GenericResponse cancelCycle(Long cycleId, Integer callerId) {
        log.info("Cancelling rental cycle {} by caller {}", cycleId, callerId);

        Optional<RentalCycle> cycleOpt = cycleRepository.findByIdAndIsDeletedFalse(cycleId);
        if (cycleOpt.isEmpty()) {
            return GenericResponse.builder().status(ERROR).code("404").message(CYCLE_NOT_FOUND).build();
        }

        RentalCycle cycle = cycleOpt.get();

        if (!cycle.getRenterId().equals(callerId) && !cycle.getOwnerId().equals(callerId)) {
            return GenericResponse.builder().status(ERROR).code("403").message("You are not a participant in this rental").build();
        }

        if (cycle.getStatus() != RentalCycleStatus.AWAITING_START) {
            return GenericResponse.builder().status(ERROR).code("400").message("Only a rental that hasn't started can be cancelled").build();
        }

        cycle.setStatus(RentalCycleStatus.CANCELLED);
        RentalCycle saved = cycleRepository.save(cycle);

        log.info("Rental cycle {} cancelled by caller {}", cycleId, callerId);

        notificationService.sendCycleCancelledNotification(saved);

        return GenericResponse.builder()
                .status(SUCCESS).code("200")
                .message("Rental cancelled")
                .body(mapper.toResponse(saved))
                .build();
    }

    @Override
    public GenericResponse getMyRentals(Integer renterId) {
        List<RentalCycle> cycles = cycleRepository.findByRenterIdAndIsDeletedFalseOrderByCreatedAtDesc(renterId);
        return GenericResponse.builder()
                .status(SUCCESS).code("200")
                .message("Rentals retrieved successfully")
                .body(cycles.stream().map(mapper::toResponse).toList())
                .build();
    }

    @Override
    public GenericResponse getMyVehicleRentals(Integer ownerId) {
        List<RentalCycle> cycles = cycleRepository.findByOwnerIdAndIsDeletedFalseOrderByCreatedAtDesc(ownerId);
        return GenericResponse.builder()
                .status(SUCCESS).code("200")
                .message("Rentals retrieved successfully")
                .body(cycles.stream().map(mapper::toResponse).toList())
                .build();
    }

    private GenericResponse checkParticipant(RentalCycle cycle, Integer callerId) {
        if (!cycle.getRenterId().equals(callerId) && !cycle.getOwnerId().equals(callerId)) {
            return GenericResponse.builder().status(ERROR).code("403").message("You are not a participant in this rental").build();
        }
        return null;
    }

    private void computeFinalPrice(RentalCycle cycle) {
        long totalSeconds = Math.max(0, Duration.between(cycle.getStartedAt(), cycle.getActualReturnAt()).getSeconds());

        long durationUnits = cycle.getRateType() == RentalRateType.HOURLY
                ? ceilDiv(totalSeconds, SECONDS_PER_HOUR)
                : ceilDiv(totalSeconds, SECONDS_PER_DAY);
        durationUnits = Math.max(1, durationUnits);

        BigDecimal basePrice = cycle.getRateAtStart().multiply(BigDecimal.valueOf(durationUnits));

        long daysForKmLimit = Math.max(1, ceilDiv(totalSeconds, SECONDS_PER_DAY));
        long allowedKm = (long) cycle.getKmLimitPerDayAtStart() * daysForKmLimit;
        long actualKm = (long) cycle.getEndOdometer() - cycle.getStartOdometer();
        long excessKm = Math.max(0, actualKm - allowedKm);
        BigDecimal excessKmPenalty = cycle.getExcessKmPenaltyRateAtStart().multiply(BigDecimal.valueOf(excessKm));

        long lateHours = 0;
        if (cycle.getExpectedReturnAt() != null) {
            long lateSeconds = Math.max(0,
                    Duration.between(cycle.getExpectedReturnAt(), cycle.getActualReturnAt()).getSeconds());
            lateHours = ceilDiv(lateSeconds, SECONDS_PER_HOUR);
        }
        BigDecimal lateReturnPenalty = cycle.getLateReturnPenaltyRateAtStart().multiply(BigDecimal.valueOf(lateHours));

        BigDecimal discount = cycle.getDiscountAmount() != null ? cycle.getDiscountAmount() : BigDecimal.ZERO;

        BigDecimal finalPrice = basePrice.subtract(discount).add(excessKmPenalty).add(lateReturnPenalty);
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            finalPrice = BigDecimal.ZERO;
        }

        cycle.setBasePrice(round(basePrice));
        cycle.setExcessKmPenalty(round(excessKmPenalty));
        cycle.setLateReturnPenalty(round(lateReturnPenalty));
        cycle.setFinalPrice(round(finalPrice));
    }

    private BigDecimal round(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private long ceilDiv(long numerator, long denominator) {
        return (numerator + denominator - 1) / denominator;
    }
}
