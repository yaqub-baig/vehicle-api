package com.autoadda.apis.vehicle.service.impl;

import com.autoadda.apis.vehicle.clients.AuthClient;
import com.autoadda.apis.vehicle.entity.CarpoolConnection;
import com.autoadda.apis.vehicle.entity.CarpoolListing;
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
public class CarpoolNotificationService {

    private static final String CARPOOL_CONNECTIONS = "/carpool-connections/";
    private static final String CUSTOMER = "CUSTOMER";

    private final AuthClient authClient;

    @Async
    public void sendConnectionRequestNotification(CarpoolConnection connection, CarpoolListing requesterListing,
                                                   CarpoolListing targetListing) {
        try {
            log.info("Sending connection request notifications for connection ID: {}", connection.getId());

            UserResponse requesterUser = authClient.getUser(connection.getRequesterUserId());
            UserResponse targetUser = authClient.getUser(connection.getTargetUserId());

            sendNewRequestToTarget(connection, requesterListing, targetUser, requesterUser);
            sendRequestSentConfirmationToRequester(connection, requesterListing, requesterUser);

            log.info("Connection request notifications sent for connection ID: {}", connection.getId());
        } catch (Exception e) {
            log.error("Failed to send connection request notifications for connection {}: {}",
                    connection.getId(), e.getMessage(), e);
        }
    }

    @Async
    public void sendConnectionAcceptedNotification(CarpoolConnection connection, CarpoolListing requesterListing,
                                                    CarpoolListing targetListing) {
        try {
            log.info("Sending connection accepted notifications for connection ID: {}", connection.getId());

            UserResponse requesterUser = authClient.getUser(connection.getRequesterUserId());
            UserResponse targetUser = authClient.getUser(connection.getTargetUserId());

            sendAcceptedNotificationWithContact(connection, requesterUser, targetUser);
            sendAcceptedNotificationWithContact(connection, targetUser, requesterUser);

            log.info("Connection accepted notifications sent for connection ID: {}", connection.getId());
        } catch (Exception e) {
            log.error("Failed to send connection accepted notifications for connection {}: {}",
                    connection.getId(), e.getMessage(), e);
        }
    }

    @Async
    public void sendConnectionRejectedNotification(CarpoolConnection connection, CarpoolListing requesterListing,
                                                     CarpoolListing targetListing) {
        try {
            log.info("Sending connection rejected notification for connection ID: {}", connection.getId());

            UserResponse requesterUser = authClient.getUser(connection.getRequesterUserId());
            UserResponse targetUser = authClient.getUser(connection.getTargetUserId());

            sendRejectionToRequester(connection, requesterUser, targetUser);

            log.info("Connection rejected notification sent for connection ID: {}", connection.getId());
        } catch (Exception e) {
            log.error("Failed to send connection rejected notification for connection {}: {}",
                    connection.getId(), e.getMessage(), e);
        }
    }

    @Async
    public void sendConnectionCancelledNotification(CarpoolConnection connection, CarpoolListing requesterListing,
                                                      CarpoolListing targetListing) {
        try {
            log.info("Sending connection cancelled notification for connection ID: {}", connection.getId());

            UserResponse requesterUser = authClient.getUser(connection.getRequesterUserId());
            UserResponse targetUser = authClient.getUser(connection.getTargetUserId());

            sendCancellationToTarget(connection, requesterUser, targetUser);

            log.info("Connection cancelled notification sent for connection ID: {}", connection.getId());
        } catch (Exception e) {
            log.error("Failed to send connection cancelled notification for connection {}: {}",
                    connection.getId(), e.getMessage(), e);
        }
    }

    private void sendNewRequestToTarget(CarpoolConnection connection, CarpoolListing requesterListing,
                                         UserResponse targetUser, UserResponse requesterUser) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse =
                    authClient.getPreferencesByCustomerId1(targetUser.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            String route = routeDescription(requesterListing);

            if (prefs == null || Boolean.TRUE.equals(prefs.getEmailOnServiceUpdates())) {
                String subject = "New Carpool Connect Request";
                String body = String.format("""
                        Dear %s,

                        %s wants to connect with you for a carpool trip.

                        TRIP DETAILS:
                        - Route: %s
                        - Requested by: %s

                        Log in to your AutoAdda account to accept or decline this request.

                        Best regards,
                        AutoAdda Team""",
                        targetUser.getName(), requesterUser.getName(), route, requesterUser.getName());

                authClient.sendEmail(targetUser.getId(), EmailRequest.builder()
                        .to(targetUser.getEmail())
                        .subject(subject)
                        .body(body)
                        .build());
            }

            if (prefs == null || Boolean.TRUE.equals(prefs.getWebpushOnServiceUpdates())) {
                authClient.sendNotification(new SendNotificationRequest(
                        String.valueOf(targetUser.getId()),
                        CUSTOMER,
                        "New Carpool Connect Request",
                        String.format("%s wants to connect with you for a carpool trip (%s)",
                                requesterUser.getName(), route),
                        CARPOOL_CONNECTIONS + connection.getId()
                ));
            }

            if (prefs != null && Boolean.TRUE.equals(prefs.getSmsOnServiceUpdates())) {
                String smsMessage = String.format("AutoAdda: %s wants to connect with you for a carpool trip (%s). Please review.",
                        requesterUser.getName(), route);
                authClient.sendSms(new SmsRequest(targetUser.getMobileNumber(), smsMessage));
            }
        } catch (Exception e) {
            log.error("Failed to send new connect request to target {}: {}", targetUser.getId(), e.getMessage());
        }
    }

    private void sendRequestSentConfirmationToRequester(CarpoolConnection connection, CarpoolListing requesterListing,
                                                          UserResponse requesterUser) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse =
                    authClient.getPreferencesByCustomerId1(requesterUser.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getWebpushOnServiceUpdates())) {
                authClient.sendNotification(new SendNotificationRequest(
                        String.valueOf(requesterUser.getId()),
                        CUSTOMER,
                        "Connect Request Sent",
                        String.format("Your connect request for the %s trip has been sent.",
                                routeDescription(requesterListing)),
                        CARPOOL_CONNECTIONS + connection.getId()
                ));
            }
        } catch (Exception e) {
            log.error("Failed to send connect request confirmation to requester {}: {}",
                    requesterUser.getId(), e.getMessage());
        }
    }

    private void sendAcceptedNotificationWithContact(CarpoolConnection connection, UserResponse recipient,
                                                       UserResponse otherParty) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse =
                    authClient.getPreferencesByCustomerId1(recipient.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getEmailOnServiceUpdates())) {
                String subject = "Carpool Connection Accepted";
                String body = String.format("""
                        Dear %s,

                        Your carpool connection has been accepted! You can now coordinate the trip directly.

                        CONTACT DETAILS:
                        - Name: %s
                        - Email: %s
                        - Phone: %s

                        Best regards,
                        AutoAdda Team""",
                        recipient.getName(), otherParty.getName(), otherParty.getEmail(), otherParty.getMobileNumber());

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
                        "Carpool Connection Accepted",
                        String.format("Your connection with %s has been accepted. Contact details have been shared.",
                                otherParty.getName()),
                        CARPOOL_CONNECTIONS + connection.getId()
                ));
            }

            if (prefs != null && Boolean.TRUE.equals(prefs.getSmsOnServiceUpdates())) {
                String smsMessage = String.format("AutoAdda: Your carpool connection with %s is accepted. Contact: %s",
                        otherParty.getName(), otherParty.getMobileNumber());
                authClient.sendSms(new SmsRequest(recipient.getMobileNumber(), smsMessage));
            }
        } catch (Exception e) {
            log.error("Failed to send connection accepted notification to {}: {}", recipient.getId(), e.getMessage());
        }
    }

    private void sendRejectionToRequester(CarpoolConnection connection, UserResponse requesterUser,
                                           UserResponse targetUser) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse =
                    authClient.getPreferencesByCustomerId1(requesterUser.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getEmailOnServiceUpdates())) {
                String subject = "Carpool Connection Declined";
                String body = String.format("""
                        Dear %s,

                        We regret to inform you that %s has declined your carpool connection request.

                        Reason: %s

                        Best regards,
                        AutoAdda Team""",
                        requesterUser.getName(), targetUser.getName(),
                        connection.getRejectionReason() != null ? connection.getRejectionReason() : "Not provided");

                authClient.sendEmail(requesterUser.getId(), EmailRequest.builder()
                        .to(requesterUser.getEmail())
                        .subject(subject)
                        .body(body)
                        .build());
            }

            if (prefs == null || Boolean.TRUE.equals(prefs.getWebpushOnServiceUpdates())) {
                authClient.sendNotification(new SendNotificationRequest(
                        String.valueOf(requesterUser.getId()),
                        CUSTOMER,
                        "Carpool Connection Declined",
                        String.format("%s declined your carpool connection request.", targetUser.getName()),
                        CARPOOL_CONNECTIONS + connection.getId()
                ));
            }
        } catch (Exception e) {
            log.error("Failed to send rejection notification to requester {}: {}", requesterUser.getId(), e.getMessage());
        }
    }

    private void sendCancellationToTarget(CarpoolConnection connection, UserResponse requesterUser,
                                           UserResponse targetUser) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse =
                    authClient.getPreferencesByCustomerId1(targetUser.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getWebpushOnServiceUpdates())) {
                authClient.sendNotification(new SendNotificationRequest(
                        String.valueOf(targetUser.getId()),
                        CUSTOMER,
                        "Carpool Connection Request Withdrawn",
                        String.format("%s withdrew their carpool connection request.", requesterUser.getName()),
                        CARPOOL_CONNECTIONS + connection.getId()
                ));
            }
        } catch (Exception e) {
            log.error("Failed to send cancellation notification to target {}: {}", targetUser.getId(), e.getMessage());
        }
    }

    private String routeDescription(CarpoolListing listing) {
        if (listing == null) {
            return "your carpool trip";
        }
        return String.format("%s to %s on %s", listing.getOriginCity(), listing.getDestinationCity(),
                listing.getTravelDateTime());
    }
}
