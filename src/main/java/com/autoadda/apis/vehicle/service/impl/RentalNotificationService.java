package com.autoadda.apis.vehicle.service.impl;

import com.autoadda.apis.vehicle.clients.AuthClient;
import com.autoadda.apis.vehicle.entity.CarRentalListing;
import com.autoadda.apis.vehicle.entity.RentalCycle;
import com.autoadda.apis.vehicle.entity.RentalRequest;
import com.autoadda.apis.vehicle.request.EmailRequest;
import com.autoadda.apis.vehicle.request.SendNotificationRequest;
import com.autoadda.apis.vehicle.request.SmsRequest;
import com.autoadda.apis.vehicle.response.CommunicationPreference;
import com.autoadda.apis.vehicle.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RentalNotificationService {

    private static final String RENTAL_REQUESTS = "/rental-requests/";
    private static final String RENTAL_CYCLES = "/rental-cycles/";
    private static final String CUSTOMER = "CUSTOMER";

    private final AuthClient authClient;

    @Async
    public void sendRequestCreatedNotification(RentalRequest request, CarRentalListing listing) {
        try {
            log.info("Sending rental request created notifications for request ID: {}", request.getId());

            UserResponse renter = authClient.getUser(request.getRenterId());
            UserResponse owner = authClient.getUser(request.getOwnerId());

            sendNewRequestToOwner(request, listing, owner, renter);
            sendRequestSentConfirmationToRenter(request, listing, renter);

            log.info("Rental request created notifications sent for request ID: {}", request.getId());
        } catch (Exception e) {
            log.error("Failed to send rental request created notifications for request {}: {}",
                    request.getId(), e.getMessage(), e);
        }
    }

    @Async
    public void sendRequestAcceptedNotification(RentalRequest request, CarRentalListing listing) {
        try {
            log.info("Sending rental request accepted notifications for request ID: {}", request.getId());

            UserResponse renter = authClient.getUser(request.getRenterId());
            UserResponse owner = authClient.getUser(request.getOwnerId());

            sendAcceptedNotificationWithContact(request, listing, renter, owner);
            sendAcceptedNotificationWithContact(request, listing, owner, renter);

            log.info("Rental request accepted notifications sent for request ID: {}", request.getId());
        } catch (Exception e) {
            log.error("Failed to send rental request accepted notifications for request {}: {}",
                    request.getId(), e.getMessage(), e);
        }
    }

    @Async
    public void sendRequestRejectedNotification(RentalRequest request, CarRentalListing listing) {
        try {
            log.info("Sending rental request rejected notification for request ID: {}", request.getId());

            UserResponse renter = authClient.getUser(request.getRenterId());
            UserResponse owner = authClient.getUser(request.getOwnerId());

            sendRejectionToRenter(request, listing, renter, owner);

            log.info("Rental request rejected notification sent for request ID: {}", request.getId());
        } catch (Exception e) {
            log.error("Failed to send rental request rejected notification for request {}: {}",
                    request.getId(), e.getMessage(), e);
        }
    }

    @Async
    public void sendRequestCancelledNotification(RentalRequest request, CarRentalListing listing) {
        try {
            log.info("Sending rental request cancelled notification for request ID: {}", request.getId());

            UserResponse renter = authClient.getUser(request.getRenterId());
            UserResponse owner = authClient.getUser(request.getOwnerId());

            sendCancellationToOwner(request, listing, renter, owner);

            log.info("Rental request cancelled notification sent for request ID: {}", request.getId());
        } catch (Exception e) {
            log.error("Failed to send rental request cancelled notification for request {}: {}",
                    request.getId(), e.getMessage(), e);
        }
    }

    @Async
    public void sendCycleStartedNotification(RentalCycle cycle) {
        try {
            log.info("Sending rental cycle started notification for cycle ID: {}", cycle.getId());

            UserResponse owner = authClient.getUser(cycle.getOwnerId());
            ResponseEntity<CommunicationPreference> prefsResponse =
                    authClient.getPreferencesByCustomerId1(owner.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getWebpushOnServiceUpdates())) {
                authClient.sendNotification(new SendNotificationRequest(
                        String.valueOf(owner.getId()),
                        CUSTOMER,
                        "Vehicle Picked Up",
                        "Your vehicle has been picked up by the renter.",
                        RENTAL_CYCLES + cycle.getId()
                ));
            }
        } catch (Exception e) {
            log.error("Failed to send rental cycle started notification for cycle {}: {}",
                    cycle.getId(), e.getMessage(), e);
        }
    }

    @Async
    public void sendCycleReturnedNotification(RentalCycle cycle) {
        try {
            log.info("Sending rental cycle returned notification for cycle ID: {}", cycle.getId());

            UserResponse renter = authClient.getUser(cycle.getRenterId());
            UserResponse owner = authClient.getUser(cycle.getOwnerId());

            sendReturnedNotificationWithBreakdown(cycle, renter);
            sendReturnedNotificationWithBreakdown(cycle, owner);

            log.info("Rental cycle returned notification sent for cycle ID: {}", cycle.getId());
        } catch (Exception e) {
            log.error("Failed to send rental cycle returned notification for cycle {}: {}",
                    cycle.getId(), e.getMessage(), e);
        }
    }

    @Async
    public void sendCycleCancelledNotification(RentalCycle cycle) {
        try {
            log.info("Sending rental cycle cancelled notification for cycle ID: {}", cycle.getId());

            UserResponse renter = authClient.getUser(cycle.getRenterId());
            UserResponse owner = authClient.getUser(cycle.getOwnerId());

            for (UserResponse recipient : new UserResponse[]{renter, owner}) {
                ResponseEntity<CommunicationPreference> prefsResponse =
                        authClient.getPreferencesByCustomerId1(recipient.getId());
                CommunicationPreference prefs = prefsResponse.getBody();

                if (prefs == null || Boolean.TRUE.equals(prefs.getWebpushOnServiceUpdates())) {
                    authClient.sendNotification(new SendNotificationRequest(
                            String.valueOf(recipient.getId()),
                            CUSTOMER,
                            "Rental Cancelled",
                            "The rental for this vehicle has been cancelled.",
                            RENTAL_CYCLES + cycle.getId()
                    ));
                }
            }
        } catch (Exception e) {
            log.error("Failed to send rental cycle cancelled notification for cycle {}: {}",
                    cycle.getId(), e.getMessage(), e);
        }
    }

    private void sendNewRequestToOwner(RentalRequest request, CarRentalListing listing,
                                        UserResponse owner, UserResponse renter) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse =
                    authClient.getPreferencesByCustomerId1(owner.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            String listingDescription = listingDescription(listing);

            if (prefs == null || Boolean.TRUE.equals(prefs.getEmailOnServiceUpdates())) {
                String subject = "New Rental Request";
                String body = String.format("""
                        Dear %s,

                        %s wants to rent your vehicle listing.

                        LISTING: %s
                        Requested rate: %s

                        Log in to your AutoAdda account to accept or decline this request.

                        Best regards,
                        AutoAdda Team""",
                        owner.getName(), renter.getName(), listingDescription, request.getRequestedRateType());

                authClient.sendEmail(owner.getId(), EmailRequest.builder()
                        .to(owner.getEmail())
                        .subject(subject)
                        .body(body)
                        .build());
            }

            if (prefs == null || Boolean.TRUE.equals(prefs.getWebpushOnServiceUpdates())) {
                authClient.sendNotification(new SendNotificationRequest(
                        String.valueOf(owner.getId()),
                        CUSTOMER,
                        "New Rental Request",
                        String.format("%s wants to rent your vehicle (%s)", renter.getName(), listingDescription),
                        RENTAL_REQUESTS + request.getId()
                ));
            }

            if (prefs != null && Boolean.TRUE.equals(prefs.getSmsOnServiceUpdates())) {
                String smsMessage = String.format("AutoAdda: %s wants to rent your vehicle (%s). Please review.",
                        renter.getName(), listingDescription);
                authClient.sendSms(new SmsRequest(owner.getMobileNumber(), smsMessage));
            }
        } catch (Exception e) {
            log.error("Failed to send new request notification to owner {}: {}", owner.getId(), e.getMessage());
        }
    }

    private void sendRequestSentConfirmationToRenter(RentalRequest request, CarRentalListing listing, UserResponse renter) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse =
                    authClient.getPreferencesByCustomerId1(renter.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getWebpushOnServiceUpdates())) {
                authClient.sendNotification(new SendNotificationRequest(
                        String.valueOf(renter.getId()),
                        CUSTOMER,
                        "Rental Request Sent",
                        String.format("Your request to rent the %s listing has been sent.", listingDescription(listing)),
                        RENTAL_REQUESTS + request.getId()
                ));
            }
        } catch (Exception e) {
            log.error("Failed to send request sent confirmation to renter {}: {}", renter.getId(), e.getMessage());
        }
    }

    private void sendAcceptedNotificationWithContact(RentalRequest request, CarRentalListing listing,
                                                       UserResponse recipient, UserResponse otherParty) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse =
                    authClient.getPreferencesByCustomerId1(recipient.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getEmailOnServiceUpdates())) {
                String subject = "Rental Request Accepted";
                String body = String.format("""
                        Dear %s,

                        Your rental request has been accepted! You can now coordinate pickup and negotiate directly.

                        LISTING: %s

                        CONTACT DETAILS:
                        - Name: %s
                        - Email: %s
                        - Phone: %s

                        Best regards,
                        AutoAdda Team""",
                        recipient.getName(), listingDescription(listing),
                        otherParty.getName(), otherParty.getEmail(), otherParty.getMobileNumber());

                authClient.sendEmail(recipient.getId(), EmailRequest.builder()
                        .to(recipient.getEmail())
                        .subject(subject)
                        .body(body)
                        .build());
            }

            if (prefs == null || Boolean.TRUE.equals(prefs.getWebpushOnServiceUpdates())) {
                authClient.sendNotification(new SendNotificationRequest(
                        String.valueOf(recipient.getId()),
                        CUSTOMER,
                        "Rental Request Accepted",
                        String.format("Your rental with %s has been accepted. Contact details have been shared.",
                                otherParty.getName()),
                        RENTAL_REQUESTS + request.getId()
                ));
            }

            if (prefs != null && Boolean.TRUE.equals(prefs.getSmsOnServiceUpdates())) {
                String smsMessage = String.format("AutoAdda: Your rental with %s is accepted. Contact: %s",
                        otherParty.getName(), otherParty.getMobileNumber());
                authClient.sendSms(new SmsRequest(recipient.getMobileNumber(), smsMessage));
            }
        } catch (Exception e) {
            log.error("Failed to send rental accepted notification to {}: {}", recipient.getId(), e.getMessage());
        }
    }

    private void sendRejectionToRenter(RentalRequest request, CarRentalListing listing,
                                        UserResponse renter, UserResponse owner) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse =
                    authClient.getPreferencesByCustomerId1(renter.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getEmailOnServiceUpdates())) {
                String subject = "Rental Request Declined";
                String body = String.format("""
                        Dear %s,

                        We regret to inform you that %s has declined your rental request for their vehicle listing (%s).

                        Reason: %s

                        Best regards,
                        AutoAdda Team""",
                        renter.getName(), owner.getName(), listingDescription(listing),
                        request.getRejectionReason() != null ? request.getRejectionReason() : "Not provided");

                authClient.sendEmail(renter.getId(), EmailRequest.builder()
                        .to(renter.getEmail())
                        .subject(subject)
                        .body(body)
                        .build());
            }

            if (prefs == null || Boolean.TRUE.equals(prefs.getWebpushOnServiceUpdates())) {
                authClient.sendNotification(new SendNotificationRequest(
                        String.valueOf(renter.getId()),
                        CUSTOMER,
                        "Rental Request Declined",
                        String.format("%s declined your rental request.", owner.getName()),
                        RENTAL_REQUESTS + request.getId()
                ));
            }
        } catch (Exception e) {
            log.error("Failed to send rejection notification to renter {}: {}", renter.getId(), e.getMessage());
        }
    }

    private void sendCancellationToOwner(RentalRequest request, CarRentalListing listing,
                                          UserResponse renter, UserResponse owner) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse =
                    authClient.getPreferencesByCustomerId1(owner.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getWebpushOnServiceUpdates())) {
                authClient.sendNotification(new SendNotificationRequest(
                        String.valueOf(owner.getId()),
                        CUSTOMER,
                        "Rental Request Withdrawn",
                        String.format("%s withdrew their rental request for your listing (%s).",
                                renter.getName(), listingDescription(listing)),
                        RENTAL_REQUESTS + request.getId()
                ));
            }
        } catch (Exception e) {
            log.error("Failed to send cancellation notification to owner {}: {}", owner.getId(), e.getMessage());
        }
    }

    private void sendReturnedNotificationWithBreakdown(RentalCycle cycle, UserResponse recipient) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse =
                    authClient.getPreferencesByCustomerId1(recipient.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getEmailOnServiceUpdates())) {
                String subject = "Rental Completed";
                String body = String.format("""
                        Dear %s,

                        The rental has been completed. Here is the final price breakdown:

                        Base price: %s
                        Discount: -%s
                        Excess km penalty: +%s
                        Late return penalty: +%s
                        FINAL PRICE: %s

                        Best regards,
                        AutoAdda Team""",
                        recipient.getName(), cycle.getBasePrice(), cycle.getDiscountAmount(),
                        cycle.getExcessKmPenalty(), cycle.getLateReturnPenalty(), cycle.getFinalPrice());

                authClient.sendEmail(recipient.getId(), EmailRequest.builder()
                        .to(recipient.getEmail())
                        .subject(subject)
                        .body(body)
                        .build());
            }

            if (prefs == null || Boolean.TRUE.equals(prefs.getWebpushOnServiceUpdates())) {
                authClient.sendNotification(new SendNotificationRequest(
                        String.valueOf(recipient.getId()),
                        CUSTOMER,
                        "Rental Completed",
                        String.format("The rental is complete. Final price: %s", cycle.getFinalPrice()),
                        RENTAL_CYCLES + cycle.getId()
                ));
            }
        } catch (Exception e) {
            log.error("Failed to send rental returned notification to {}: {}", recipient.getId(), e.getMessage());
        }
    }

    private String listingDescription(CarRentalListing listing) {
        if (listing == null) {
            return "your vehicle";
        }
        return String.format("Listing #%d (%s/day)", listing.getId(), listing.getPricePerDay());
    }
}
