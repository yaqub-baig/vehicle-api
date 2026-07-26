package com.autoadda.apis.vehicle.scheduler;

import com.autoadda.apis.vehicle.dao.CarpoolConnectionRepository;
import com.autoadda.apis.vehicle.dao.CarpoolListingRepository;
import com.autoadda.apis.vehicle.entity.CarpoolConnection;
import com.autoadda.apis.vehicle.entity.CarpoolListing;
import com.autoadda.apis.vehicle.enums.CarpoolConnectionStatus;
import com.autoadda.apis.vehicle.enums.CarpoolListingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CarpoolListingExpiryScheduler {

    private final CarpoolListingRepository listingRepository;
    private final CarpoolConnectionRepository connectionRepository;

    @Scheduled(fixedRateString = "${vehicle.carpool.expiry.interval-ms:1800000}")
    public void expireStaleListingsAndConnections() {
        List<CarpoolListing> staleListings = listingRepository
                .findByStatusAndTravelDateTimeBeforeAndIsDeletedFalse(CarpoolListingStatus.ACTIVE, LocalDateTime.now());

        log.info("Carpool expiry sweep: {} listing(s) past travel time", staleListings.size());

        for (CarpoolListing listing : staleListings) {
            try {
                listing.setStatus(CarpoolListingStatus.EXPIRED);
                listingRepository.save(listing);
            } catch (Exception e) {
                log.error("Failed to expire carpool listing {}: {}", listing.getId(), e.getMessage(), e);
            }
        }

        List<CarpoolConnection> pendingConnections =
                connectionRepository.findByStatusAndIsDeletedFalse(CarpoolConnectionStatus.PENDING);

        for (CarpoolConnection connection : pendingConnections) {
            try {
                boolean requesterInactive = listingRepository.findById(connection.getRequesterListingId())
                        .map(l -> l.getStatus() != CarpoolListingStatus.ACTIVE)
                        .orElse(true);
                boolean targetInactive = listingRepository.findById(connection.getTargetListingId())
                        .map(l -> l.getStatus() != CarpoolListingStatus.ACTIVE)
                        .orElse(true);

                if (requesterInactive || targetInactive) {
                    connection.setStatus(CarpoolConnectionStatus.EXPIRED);
                    connectionRepository.save(connection);
                }
            } catch (Exception e) {
                log.error("Failed to expire carpool connection {}: {}", connection.getId(), e.getMessage(), e);
            }
        }
    }
}
