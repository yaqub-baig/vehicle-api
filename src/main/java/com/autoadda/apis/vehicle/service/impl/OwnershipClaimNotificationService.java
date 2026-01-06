package com.autoadda.apis.vehicle.service.impl;

import com.autoadda.apis.vehicle.clients.AuthClient;
import com.autoadda.apis.vehicle.entity.VehicleDetails;
import com.autoadda.apis.vehicle.entity.VehicleOwnershipClaim;
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
public class OwnershipClaimNotificationService {

    public static final String OWNERSHIP_CLAIMS = "/ownership-claims/";
    public static final String NOT_PROVIDED = "Not provided";
    public static final String CUSTOMER = "CUSTOMER";
    private final AuthClient authClient;

    @Async
    public void sendClaimCreatedNotifications(VehicleOwnershipClaim claim, VehicleDetails vehicle) {
        try {
            log.info("Sending claim created notifications for claim ID: {}", claim.getId());

            UserResponse requester = authClient.getUser(claim.getRequesterId());
            UserResponse owner = authClient.getUser(claim.getCurrentOwnerId());

            sendNotificationsToOwner(claim, vehicle, requester, owner);
            sendConfirmationToRequester(claim, vehicle, requester);

            log.info("Claim created notifications sent successfully for claim ID: {}", claim.getId());
        } catch (Exception e) {
            log.error("Failed to send claim created notifications for claim {}: {}", claim.getId(), e.getMessage(), e);
        }
    }

    @Async
    public void sendClaimApprovedNotifications(VehicleOwnershipClaim claim, VehicleDetails vehicle) {
        try {
            log.info("Sending claim approved notifications for claim ID: {}", claim.getId());

            UserResponse newOwner = authClient.getUser(claim.getRequesterId());
            UserResponse previousOwner = authClient.getUser(claim.getCurrentOwnerId());

            sendApprovalToNewOwner(claim, vehicle, newOwner, previousOwner);
            sendApprovalConfirmationToPreviousOwner(claim, vehicle, newOwner, previousOwner);

            log.info("Claim approved notifications sent successfully for claim ID: {}", claim.getId());
        } catch (Exception e) {
            log.error("Failed to send claim approved notifications for claim {}: {}", claim.getId(), e.getMessage(), e);
        }
    }

    @Async
    public void sendClaimRejectedNotifications(VehicleOwnershipClaim claim, VehicleDetails vehicle) {
        try {
            log.info("Sending claim rejected notifications for claim ID: {}", claim.getId());

            UserResponse requester = authClient.getUser(claim.getRequesterId());
            UserResponse owner = authClient.getUser(claim.getCurrentOwnerId());

            sendRejectionToRequester(claim, vehicle, requester, owner);
            sendRejectionConfirmationToOwner(claim, vehicle, requester, owner);

            log.info("Claim rejected notifications sent successfully for claim ID: {}", claim.getId());
        } catch (Exception e) {
            log.error("Failed to send claim rejected notifications for claim {}: {}", claim.getId(), e.getMessage(), e);
        }
    }

    @Async
    public void sendClaimCancelledNotifications(VehicleOwnershipClaim claim, VehicleDetails vehicle) {
        try {
            log.info("Sending claim cancelled notifications for claim ID: {}", claim.getId());

            UserResponse requester = authClient.getUser(claim.getRequesterId());
            UserResponse owner = authClient.getUser(claim.getCurrentOwnerId());

            sendCancellationToRequester(claim, vehicle, requester);
            sendCancellationNoticeToOwner(claim, vehicle, requester, owner);

            log.info("Claim cancelled notifications sent successfully for claim ID: {}", claim.getId());
        } catch (Exception e) {
            log.error("Failed to send claim cancelled notifications for claim {}: {}", claim.getId(), e.getMessage(), e);
        }
    }

    private void sendNotificationsToOwner(VehicleOwnershipClaim claim, VehicleDetails vehicle,
                                          UserResponse requester, UserResponse owner) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse = authClient.getPreferencesByCustomerId1(owner.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getEmailOnServiceUpdates())) {
                String subject = String.format("New Ownership Claim Request for Your %s %s",
                        vehicle.getBrand(), vehicle.getModel());
                String body = buildOwnerClaimReceivedEmailBody(claim, vehicle, requester, owner);

                authClient.sendEmail(owner.getId(), EmailRequest.builder()
                        .to(owner.getEmail())
                        .subject(subject)
                        .body(body)
                        .build());
                log.info("Email sent to owner {} for claim {}", owner.getId(), claim.getId());
            }

            if (prefs == null || Boolean.TRUE.equals(prefs.getWebpushOnServiceUpdates())) {
                String pushTitle = "New Ownership Request";
                String pushBody = String.format("%s has requested ownership of your %s %s",
                        requester.getName(), vehicle.getBrand(), vehicle.getModel());

                authClient.sendNotification(new SendNotificationRequest(
                        String.valueOf(owner.getId()),
                        CUSTOMER,
                        pushTitle,
                        pushBody,
                        "/ownership-claims/pending"
                ));
                log.info("Push notification sent to owner {} for claim {}", owner.getId(), claim.getId());
            }

            if (prefs != null && Boolean.TRUE.equals(prefs.getSmsOnServiceUpdates())) {
                String smsMessage = String.format("AutoAdda: %s has requested ownership of your %s %s (Chassis: %s). Please review and respond.",
                        requester.getName(), vehicle.getBrand(), vehicle.getModel(), claim.getChassisNo());

                authClient.sendSms(new SmsRequest(owner.getMobileNumber(), smsMessage));
                log.info("SMS sent to owner {} for claim {}", owner.getId(), claim.getId());
            }
        } catch (Exception e) {
            log.error("Failed to send notifications to owner {}: {}", owner.getId(), e.getMessage());
        }
    }

    private void sendConfirmationToRequester(VehicleOwnershipClaim claim, VehicleDetails vehicle,
                                             UserResponse requester) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse = authClient.getPreferencesByCustomerId1(requester.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getEmailOnServiceUpdates())) {
                String subject = "Your Ownership Claim Has Been Submitted";
                String body = buildRequesterConfirmationEmailBody(claim, vehicle, requester);

                authClient.sendEmail(requester.getId(), EmailRequest.builder()
                        .to(requester.getEmail())
                        .subject(subject)
                        .body(body)
                        .build());
                log.info("Confirmation email sent to requester {} for claim {}", requester.getId(), claim.getId());
            }

            if (prefs == null || Boolean.TRUE.equals(prefs.getWebpushOnServiceUpdates())) {
                String pushTitle = "Claim Submitted Successfully";
                String pushBody = String.format("Your claim for %s %s is pending owner approval",
                        vehicle.getBrand(), vehicle.getModel());

                authClient.sendNotification(new SendNotificationRequest(
                        String.valueOf(requester.getId()),
                        CUSTOMER,
                        pushTitle,
                        pushBody,
                        "/ownership-claims/my-requests"
                ));
                log.info("Confirmation push notification sent to requester {} for claim {}", requester.getId(), claim.getId());
            }
        } catch (Exception e) {
            log.error("Failed to send confirmation to requester {}: {}", requester.getId(), e.getMessage());
        }
    }

    private void sendApprovalToNewOwner(VehicleOwnershipClaim claim, VehicleDetails vehicle,
                                        UserResponse newOwner, UserResponse previousOwner) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse = authClient.getPreferencesByCustomerId1(newOwner.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getEmailOnServiceUpdates())) {
                String subject = String.format("Congratulations! You Are Now The Owner of %s %s",
                        vehicle.getBrand(), vehicle.getModel());
                String body = buildNewOwnerApprovalEmailBody(claim, vehicle, newOwner, previousOwner);

                authClient.sendEmail(newOwner.getId(), EmailRequest.builder()
                        .to(newOwner.getEmail())
                        .subject(subject)
                        .body(body)
                        .build());
                log.info("Approval email sent to new owner {} for claim {}", newOwner.getId(), claim.getId());
            }

            if (prefs == null || Boolean.TRUE.equals(prefs.getWebpushOnServiceUpdates())) {
                String pushTitle = "Ownership Transferred!";
                String pushBody = String.format("Your claim for %s %s has been approved",
                        vehicle.getBrand(), vehicle.getModel());

                authClient.sendNotification(new SendNotificationRequest(
                        String.valueOf(newOwner.getId()),
                        CUSTOMER,
                        pushTitle,
                        pushBody,
                        "/vehicles/" + vehicle.getId()
                ));
                log.info("Approval push notification sent to new owner {} for claim {}", newOwner.getId(), claim.getId());
            }

            if (prefs != null && Boolean.TRUE.equals(prefs.getSmsOnServiceUpdates())) {
                String smsMessage = String.format("AutoAdda: Your ownership claim for %s %s (Chassis: %s) has been APPROVED. You are now the registered owner.",
                        vehicle.getBrand(), vehicle.getModel(), claim.getChassisNo());

                authClient.sendSms(new SmsRequest(newOwner.getMobileNumber(), smsMessage));
                log.info("Approval SMS sent to new owner {} for claim {}", newOwner.getId(), claim.getId());
            }
        } catch (Exception e) {
            log.error("Failed to send approval to new owner {}: {}", newOwner.getId(), e.getMessage());
        }
    }

    private void sendApprovalConfirmationToPreviousOwner(VehicleOwnershipClaim claim, VehicleDetails vehicle,
                                                         UserResponse newOwner, UserResponse previousOwner) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse = authClient.getPreferencesByCustomerId1(previousOwner.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getEmailOnServiceUpdates())) {
                String subject = String.format("Vehicle Ownership Transfer Completed for %s %s",
                        vehicle.getBrand(), vehicle.getModel());
                String body = buildPreviousOwnerApprovalEmailBody(claim, vehicle, newOwner, previousOwner);

                authClient.sendEmail(previousOwner.getId(), EmailRequest.builder()
                        .to(previousOwner.getEmail())
                        .subject(subject)
                        .body(body)
                        .build());
                log.info("Approval confirmation email sent to previous owner {} for claim {}", previousOwner.getId(), claim.getId());
            }

            if (prefs == null || Boolean.TRUE.equals(prefs.getWebpushOnServiceUpdates())) {
                String pushTitle = "Ownership Transfer Confirmed";
                String pushBody = String.format("You have transferred %s %s to %s",
                        vehicle.getBrand(), vehicle.getModel(), newOwner.getName());

                authClient.sendNotification(new SendNotificationRequest(
                        String.valueOf(previousOwner.getId()),
                        CUSTOMER,
                        pushTitle,
                        pushBody,
                        OWNERSHIP_CLAIMS + claim.getId()
                ));
                log.info("Approval confirmation push notification sent to previous owner {} for claim {}", previousOwner.getId(), claim.getId());
            }
        } catch (Exception e) {
            log.error("Failed to send approval confirmation to previous owner {}: {}", previousOwner.getId(), e.getMessage());
        }
    }

    private void sendRejectionToRequester(VehicleOwnershipClaim claim, VehicleDetails vehicle,
                                          UserResponse requester, UserResponse owner) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse = authClient.getPreferencesByCustomerId1(requester.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getEmailOnServiceUpdates())) {
                String subject = String.format("Your Ownership Claim for %s %s Was Not Approved",
                        vehicle.getBrand(), vehicle.getModel());
                String body = buildRequesterRejectionEmailBody(claim, vehicle, requester, owner);

                authClient.sendEmail(requester.getId(), EmailRequest.builder()
                        .to(requester.getEmail())
                        .subject(subject)
                        .body(body)
                        .build());
                log.info("Rejection email sent to requester {} for claim {}", requester.getId(), claim.getId());
            }

            if (prefs == null || Boolean.TRUE.equals(prefs.getWebpushOnServiceUpdates())) {
                String pushTitle = "Claim Rejected";
                String pushBody = String.format("Your claim for %s %s has been declined by the owner",
                        vehicle.getBrand(), vehicle.getModel());

                authClient.sendNotification(new SendNotificationRequest(
                        String.valueOf(requester.getId()),
                        CUSTOMER,
                        pushTitle,
                        pushBody,
                        OWNERSHIP_CLAIMS + claim.getId()
                ));
                log.info("Rejection push notification sent to requester {} for claim {}", requester.getId(), claim.getId());
            }

            if (prefs != null && Boolean.TRUE.equals(prefs.getSmsOnServiceUpdates())) {
                String smsMessage = String.format("AutoAdda: Your ownership claim for %s %s (Chassis: %s) has been REJECTED by the owner.",
                        vehicle.getBrand(), vehicle.getModel(), claim.getChassisNo());

                authClient.sendSms(new SmsRequest(requester.getMobileNumber(), smsMessage));
                log.info("Rejection SMS sent to requester {} for claim {}", requester.getId(), claim.getId());
            }
        } catch (Exception e) {
            log.error("Failed to send rejection to requester {}: {}", requester.getId(), e.getMessage());
        }
    }

    private void sendRejectionConfirmationToOwner(VehicleOwnershipClaim claim, VehicleDetails vehicle,
                                                  UserResponse requester, UserResponse owner) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse = authClient.getPreferencesByCustomerId1(owner.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getEmailOnServiceUpdates())) {
                String subject = String.format("You Have Rejected an Ownership Claim for %s %s",
                        vehicle.getBrand(), vehicle.getModel());
                String body = buildOwnerRejectionConfirmationEmailBody(claim, vehicle, requester, owner);

                authClient.sendEmail(owner.getId(), EmailRequest.builder()
                        .to(owner.getEmail())
                        .subject(subject)
                        .body(body)
                        .build());
                log.info("Rejection confirmation email sent to owner {} for claim {}", owner.getId(), claim.getId());
            }

            if (prefs == null || Boolean.TRUE.equals(prefs.getWebpushOnServiceUpdates())) {
                String pushTitle = "Claim Rejection Confirmed";
                String pushBody = String.format("You rejected the ownership request from %s", requester.getName());

                authClient.sendNotification(new SendNotificationRequest(
                        String.valueOf(owner.getId()),
                        CUSTOMER,
                        pushTitle,
                        pushBody,
                        OWNERSHIP_CLAIMS + claim.getId()
                ));
                log.info("Rejection confirmation push notification sent to owner {} for claim {}", owner.getId(), claim.getId());
            }
        } catch (Exception e) {
            log.error("Failed to send rejection confirmation to owner {}: {}", owner.getId(), e.getMessage());
        }
    }

    private void sendCancellationToRequester(VehicleOwnershipClaim claim, VehicleDetails vehicle,
                                             UserResponse requester) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse = authClient.getPreferencesByCustomerId1(requester.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getEmailOnServiceUpdates())) {
                String subject = String.format("Your Ownership Claim for %s %s Has Been Cancelled",
                        vehicle.getBrand(), vehicle.getModel());
                String body = buildRequesterCancellationEmailBody(claim, vehicle, requester);

                authClient.sendEmail(requester.getId(), EmailRequest.builder()
                        .to(requester.getEmail())
                        .subject(subject)
                        .body(body)
                        .build());
                log.info("Cancellation email sent to requester {} for claim {}", requester.getId(), claim.getId());
            }

            if (prefs == null || Boolean.TRUE.equals(prefs.getWebpushOnServiceUpdates())) {
                String pushTitle = "Claim Cancelled";
                String pushBody = String.format("You cancelled your claim for %s %s",
                        vehicle.getBrand(), vehicle.getModel());

                authClient.sendNotification(new SendNotificationRequest(
                        String.valueOf(requester.getId()),
                        CUSTOMER,
                        pushTitle,
                        pushBody,
                        OWNERSHIP_CLAIMS + claim.getId()
                ));
                log.info("Cancellation push notification sent to requester {} for claim {}", requester.getId(), claim.getId());
            }
        } catch (Exception e) {
            log.error("Failed to send cancellation to requester {}: {}", requester.getId(), e.getMessage());
        }
    }

    private void sendCancellationNoticeToOwner(VehicleOwnershipClaim claim, VehicleDetails vehicle,
                                               UserResponse requester, UserResponse owner) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse = authClient.getPreferencesByCustomerId1(owner.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getEmailOnServiceUpdates())) {
                String subject = String.format("Ownership Claim for Your %s %s Was Cancelled",
                        vehicle.getBrand(), vehicle.getModel());
                String body = buildOwnerCancellationNoticeEmailBody(claim, vehicle, requester, owner);

                authClient.sendEmail(owner.getId(), EmailRequest.builder()
                        .to(owner.getEmail())
                        .subject(subject)
                        .body(body)
                        .build());
                log.info("Cancellation notice email sent to owner {} for claim {}", owner.getId(), claim.getId());
            }

            if (prefs == null || Boolean.TRUE.equals(prefs.getWebpushOnServiceUpdates())) {
                String pushTitle = "Claim Cancelled by Requester";
                String pushBody = String.format("%s cancelled their claim for your %s %s",
                        requester.getName(), vehicle.getBrand(), vehicle.getModel());

                authClient.sendNotification(new SendNotificationRequest(
                        String.valueOf(owner.getId()),
                        CUSTOMER,
                        pushTitle,
                        pushBody,
                        OWNERSHIP_CLAIMS + claim.getId()
                ));
                log.info("Cancellation notice push notification sent to owner {} for claim {}", owner.getId(), claim.getId());
            }
        } catch (Exception e) {
            log.error("Failed to send cancellation notice to owner {}: {}", owner.getId(), e.getMessage());
        }
    }

    private String buildOwnerClaimReceivedEmailBody(VehicleOwnershipClaim claim, VehicleDetails vehicle,
                                                    UserResponse requester, UserResponse owner) {
        return String.format("""
                Dear %s,

                You have received a new ownership claim request for your vehicle.

                CLAIM DETAILS:
                - Claim ID: %d
                - Vehicle: %s %s (%s)
                - Chassis Number: %s
                - Requester: %s
                - Requester Email: %s
                - Requester Phone: %s
                - Reason: %s
                - Request Date: %s

                Please review this claim and take appropriate action (approve or reject) at your earliest convenience.

                To review and respond to this claim, please log in to your AutoAdda account.

                Best regards,
                AutoAdda Team""",
                owner.getName(),
                claim.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear(),
                claim.getChassisNo(),
                requester.getName(),
                requester.getEmail(),
                requester.getMobileNumber(),
                claim.getReason() != null ? claim.getReason() : "Not specified",
                claim.getRequestDate()
        );
    }

    private String buildRequesterConfirmationEmailBody(VehicleOwnershipClaim claim, VehicleDetails vehicle,
                                                       UserResponse requester) {
        return String.format("""
                Dear %s,

                Your ownership claim has been submitted successfully.

                CLAIM DETAILS:
                - Claim ID: %d
                - Vehicle: %s %s (%s)
                - Chassis Number: %s
                - Status: PENDING
                - Reason: %s
                - Request Date: %s

                The current vehicle owner has been notified and will review your request. You will be notified once the owner takes action on your claim.

                You can track the status of your claim by logging in to your AutoAdda account.

                Best regards,
                AutoAdda Team""",
                requester.getName(),
                claim.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear(),
                claim.getChassisNo(),
                claim.getReason() != null ? claim.getReason() : "Not specified",
                claim.getRequestDate()
        );
    }

    private String buildNewOwnerApprovalEmailBody(VehicleOwnershipClaim claim, VehicleDetails vehicle,
                                                  UserResponse newOwner, UserResponse previousOwner) {
        return String.format("""
                Dear %s,

                Congratulations! Your ownership claim has been APPROVED.

                OWNERSHIP DETAILS:
                - Claim ID: %d
                - Vehicle: %s %s (%s)
                - Chassis Number: %s
                - Previous Owner: %s
                - Transfer Date: %s
                - Response Notes: %s

                You are now the registered owner of this vehicle in the AutoAdda system. You can now manage your vehicle, schedule services, and access all owner features.

                To view and manage your vehicle, please log in to your AutoAdda account.

                Best regards,
                AutoAdda Team""",
                newOwner.getName(),
                claim.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear(),
                claim.getChassisNo(),
                previousOwner.getName(),
                claim.getResponseDate(),
                claim.getResponseNotes() != null ? claim.getResponseNotes() : NOT_PROVIDED
        );
    }

    private String buildPreviousOwnerApprovalEmailBody(VehicleOwnershipClaim claim, VehicleDetails vehicle,
                                                       UserResponse newOwner, UserResponse previousOwner) {
        return String.format("""
                Dear %s,

                The ownership transfer for your vehicle has been completed successfully.

                TRANSFER DETAILS:
                - Claim ID: %d
                - Vehicle: %s %s (%s)
                - Chassis Number: %s
                - New Owner: %s
                - Transfer Date: %s
                - Your Notes: %s

                The vehicle has been transferred to %s. Thank you for using AutoAdda for your vehicle management needs.

                Best regards,
                AutoAdda Team""",
                previousOwner.getName(),
                claim.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear(),
                claim.getChassisNo(),
                newOwner.getName(),
                claim.getResponseDate(),
                claim.getResponseNotes() != null ? claim.getResponseNotes() : NOT_PROVIDED,
                newOwner.getName()
        );
    }

    private String buildRequesterRejectionEmailBody(VehicleOwnershipClaim claim, VehicleDetails vehicle,
                                                    UserResponse requester, UserResponse owner) {
        return String.format("""
                Dear %s,

                We regret to inform you that your ownership claim has been REJECTED by the current vehicle owner.

                CLAIM DETAILS:
                - Claim ID: %d
                - Vehicle: %s %s (%s)
                - Chassis Number: %s
                - Current Owner: %s
                - Response Date: %s
                - Owner's Response: %s

                If you believe this rejection is in error, please contact the vehicle owner directly or reach out to AutoAdda support for assistance.

                Best regards,
                AutoAdda Team""",
                requester.getName(),
                claim.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear(),
                claim.getChassisNo(),
                owner.getName(),
                claim.getResponseDate(),
                claim.getResponseNotes() != null ? claim.getResponseNotes() : NOT_PROVIDED
        );
    }

    private String buildOwnerRejectionConfirmationEmailBody(VehicleOwnershipClaim claim, VehicleDetails vehicle,
                                                            UserResponse requester, UserResponse owner) {
        return String.format("""
                Dear %s,

                You have successfully rejected the ownership claim request for your vehicle.

                REJECTION DETAILS:
                - Claim ID: %d
                - Vehicle: %s %s (%s)
                - Chassis Number: %s
                - Requester: %s
                - Response Date: %s
                - Your Response: %s

                The requester has been notified of your decision. Your vehicle ownership remains unchanged.

                Best regards,
                AutoAdda Team""",
                owner.getName(),
                claim.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear(),
                claim.getChassisNo(),
                requester.getName(),
                claim.getResponseDate(),
                claim.getResponseNotes() != null ? claim.getResponseNotes() : NOT_PROVIDED
        );
    }

    private String buildRequesterCancellationEmailBody(VehicleOwnershipClaim claim, VehicleDetails vehicle,
                                                       UserResponse requester) {
        return String.format("""
                Dear %s,

                Your ownership claim has been cancelled as requested.

                CANCELLATION DETAILS:
                - Claim ID: %d
                - Vehicle: %s %s (%s)
                - Chassis Number: %s
                - Cancellation Date: %s

                If you need to request ownership again in the future, you can submit a new claim through your AutoAdda account.

                Best regards,
                AutoAdda Team""",
                requester.getName(),
                claim.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear(),
                claim.getChassisNo(),
                claim.getResponseDate()
        );
    }

    private String buildOwnerCancellationNoticeEmailBody(VehicleOwnershipClaim claim, VehicleDetails vehicle,
                                                         UserResponse requester, UserResponse owner) {
        return String.format("""
                Dear %s,

                The ownership claim request for your vehicle has been cancelled by the requester.

                CANCELLATION DETAILS:
                - Claim ID: %d
                - Vehicle: %s %s (%s)
                - Chassis Number: %s
                - Requester: %s
                - Cancellation Date: %s

                No action is required from you. The claim has been closed automatically.

                Best regards,
                AutoAdda Team""",
                owner.getName(),
                claim.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear(),
                claim.getChassisNo(),
                requester.getName(),
                claim.getResponseDate()
        );
    }
}
