package com.autoadda.apis.vehicle.service.impl;

import com.autoadda.apis.vehicle.clients.AuthClient;
import com.autoadda.apis.vehicle.entity.NumberPlateInterest;
import com.autoadda.apis.vehicle.entity.NumberPlateListing;
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
public class NumberPlateNotificationService {

    private static final String NUMBER_PLATE_INTERESTS = "/number-plate-interests/";
    private static final String CUSTOMER = "CUSTOMER";

    private final AuthClient authClient;

    @Async
    public void sendInterestCreatedNotification(NumberPlateInterest interest, NumberPlateListing listing) {
        try {
            log.info("Sending interest created notifications for interest ID: {}", interest.getId());

            UserResponse buyer = authClient.getUser(interest.getBuyerUserId());
            UserResponse seller = authClient.getUser(interest.getSellerUserId());

            sendNewInterestToSeller(interest, listing, seller, buyer);
            sendInterestSentConfirmationToBuyer(interest, listing, buyer);

            log.info("Interest created notifications sent for interest ID: {}", interest.getId());
        } catch (Exception e) {
            log.error("Failed to send interest created notifications for interest {}: {}",
                    interest.getId(), e.getMessage(), e);
        }
    }

    @Async
    public void sendInterestAcceptedNotification(NumberPlateInterest interest, NumberPlateListing listing) {
        try {
            log.info("Sending interest accepted notifications for interest ID: {}", interest.getId());

            UserResponse buyer = authClient.getUser(interest.getBuyerUserId());
            UserResponse seller = authClient.getUser(interest.getSellerUserId());

            sendAcceptedNotificationWithContact(interest, listing, buyer, seller);
            sendAcceptedNotificationWithContact(interest, listing, seller, buyer);

            log.info("Interest accepted notifications sent for interest ID: {}", interest.getId());
        } catch (Exception e) {
            log.error("Failed to send interest accepted notifications for interest {}: {}",
                    interest.getId(), e.getMessage(), e);
        }
    }

    @Async
    public void sendInterestRejectedNotification(NumberPlateInterest interest, NumberPlateListing listing) {
        try {
            log.info("Sending interest rejected notification for interest ID: {}", interest.getId());

            UserResponse buyer = authClient.getUser(interest.getBuyerUserId());
            UserResponse seller = authClient.getUser(interest.getSellerUserId());

            sendRejectionToBuyer(interest, listing, buyer, seller);

            log.info("Interest rejected notification sent for interest ID: {}", interest.getId());
        } catch (Exception e) {
            log.error("Failed to send interest rejected notification for interest {}: {}",
                    interest.getId(), e.getMessage(), e);
        }
    }

    @Async
    public void sendInterestCancelledNotification(NumberPlateInterest interest, NumberPlateListing listing) {
        try {
            log.info("Sending interest cancelled notification for interest ID: {}", interest.getId());

            UserResponse buyer = authClient.getUser(interest.getBuyerUserId());
            UserResponse seller = authClient.getUser(interest.getSellerUserId());

            sendCancellationToSeller(interest, listing, buyer, seller);

            log.info("Interest cancelled notification sent for interest ID: {}", interest.getId());
        } catch (Exception e) {
            log.error("Failed to send interest cancelled notification for interest {}: {}",
                    interest.getId(), e.getMessage(), e);
        }
    }

    private void sendNewInterestToSeller(NumberPlateInterest interest, NumberPlateListing listing,
                                          UserResponse seller, UserResponse buyer) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse =
                    authClient.getPreferencesByCustomerId1(seller.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            String plateDescription = plateDescription(listing);

            if (prefs == null || Boolean.TRUE.equals(prefs.getEmailOnServiceUpdates())) {
                String subject = "New Interest in Your Number Plate Listing";
                String body = String.format("""
                        Dear %s,

                        %s is interested in your number plate listing.

                        LISTING:
                        - Plate: %s
                        - Interested buyer: %s

                        Log in to your AutoAdda account to accept or decline this interest.

                        Best regards,
                        AutoAdda Team""",
                        seller.getName(), buyer.getName(), plateDescription, buyer.getName());

                authClient.sendEmail(seller.getId(), EmailRequest.builder()
                        .to(seller.getEmail())
                        .subject(subject)
                        .body(body)
                        .build());
            }

            if (prefs == null || Boolean.TRUE.equals(prefs.getWebpushOnServiceUpdates())) {
                authClient.sendNotification(new SendNotificationRequest(
                        String.valueOf(seller.getId()),
                        CUSTOMER,
                        "New Interest in Your Number Plate Listing",
                        String.format("%s is interested in your listing (%s)", buyer.getName(), plateDescription),
                        NUMBER_PLATE_INTERESTS + interest.getId()
                ));
            }

            if (prefs != null && Boolean.TRUE.equals(prefs.getSmsOnServiceUpdates())) {
                String smsMessage = String.format("AutoAdda: %s is interested in your number plate listing (%s). Please review.",
                        buyer.getName(), plateDescription);
                authClient.sendSms(new SmsRequest(seller.getMobileNumber(), smsMessage));
            }
        } catch (Exception e) {
            log.error("Failed to send new interest notification to seller {}: {}", seller.getId(), e.getMessage());
        }
    }

    private void sendInterestSentConfirmationToBuyer(NumberPlateInterest interest, NumberPlateListing listing,
                                                       UserResponse buyer) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse =
                    authClient.getPreferencesByCustomerId1(buyer.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getWebpushOnServiceUpdates())) {
                authClient.sendNotification(new SendNotificationRequest(
                        String.valueOf(buyer.getId()),
                        CUSTOMER,
                        "Interest Sent",
                        String.format("Your interest in the %s listing has been sent.", plateDescription(listing)),
                        NUMBER_PLATE_INTERESTS + interest.getId()
                ));
            }
        } catch (Exception e) {
            log.error("Failed to send interest sent confirmation to buyer {}: {}", buyer.getId(), e.getMessage());
        }
    }

    private void sendAcceptedNotificationWithContact(NumberPlateInterest interest, NumberPlateListing listing,
                                                       UserResponse recipient, UserResponse otherParty) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse =
                    authClient.getPreferencesByCustomerId1(recipient.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getEmailOnServiceUpdates())) {
                String subject = "Number Plate Interest Accepted";
                String body = String.format("""
                        Dear %s,

                        Your number plate interest has been accepted! You can now coordinate the sale directly.

                        LISTING: %s

                        CONTACT DETAILS:
                        - Name: %s
                        - Email: %s
                        - Phone: %s

                        Best regards,
                        AutoAdda Team""",
                        recipient.getName(), plateDescription(listing),
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
                        "Number Plate Interest Accepted",
                        String.format("Your interest with %s has been accepted. Contact details have been shared.",
                                otherParty.getName()),
                        NUMBER_PLATE_INTERESTS + interest.getId()
                ));
            }

            if (prefs != null && Boolean.TRUE.equals(prefs.getSmsOnServiceUpdates())) {
                String smsMessage = String.format("AutoAdda: Your number plate interest with %s is accepted. Contact: %s",
                        otherParty.getName(), otherParty.getMobileNumber());
                authClient.sendSms(new SmsRequest(recipient.getMobileNumber(), smsMessage));
            }
        } catch (Exception e) {
            log.error("Failed to send interest accepted notification to {}: {}", recipient.getId(), e.getMessage());
        }
    }

    private void sendRejectionToBuyer(NumberPlateInterest interest, NumberPlateListing listing,
                                       UserResponse buyer, UserResponse seller) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse =
                    authClient.getPreferencesByCustomerId1(buyer.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getEmailOnServiceUpdates())) {
                String subject = "Number Plate Interest Declined";
                String body = String.format("""
                        Dear %s,

                        We regret to inform you that %s has declined your interest in their number plate listing (%s).

                        Reason: %s

                        Best regards,
                        AutoAdda Team""",
                        buyer.getName(), seller.getName(), plateDescription(listing),
                        interest.getRejectionReason() != null ? interest.getRejectionReason() : "Not provided");

                authClient.sendEmail(buyer.getId(), EmailRequest.builder()
                        .to(buyer.getEmail())
                        .subject(subject)
                        .body(body)
                        .build());
            }

            if (prefs == null || Boolean.TRUE.equals(prefs.getWebpushOnServiceUpdates())) {
                authClient.sendNotification(new SendNotificationRequest(
                        String.valueOf(buyer.getId()),
                        CUSTOMER,
                        "Number Plate Interest Declined",
                        String.format("%s declined your interest in their number plate listing.", seller.getName()),
                        NUMBER_PLATE_INTERESTS + interest.getId()
                ));
            }
        } catch (Exception e) {
            log.error("Failed to send rejection notification to buyer {}: {}", buyer.getId(), e.getMessage());
        }
    }

    private void sendCancellationToSeller(NumberPlateInterest interest, NumberPlateListing listing,
                                           UserResponse buyer, UserResponse seller) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse =
                    authClient.getPreferencesByCustomerId1(seller.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getWebpushOnServiceUpdates())) {
                authClient.sendNotification(new SendNotificationRequest(
                        String.valueOf(seller.getId()),
                        CUSTOMER,
                        "Number Plate Interest Withdrawn",
                        String.format("%s withdrew their interest in your listing (%s).",
                                buyer.getName(), plateDescription(listing)),
                        NUMBER_PLATE_INTERESTS + interest.getId()
                ));
            }
        } catch (Exception e) {
            log.error("Failed to send cancellation notification to seller {}: {}", seller.getId(), e.getMessage());
        }
    }

    private String plateDescription(NumberPlateListing listing) {
        if (listing == null) {
            return "your number plate";
        }
        return String.format("%s (%s)", listing.getPlateNumber(), listing.getEmirate());
    }
}
