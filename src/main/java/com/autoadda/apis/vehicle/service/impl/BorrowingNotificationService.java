package com.autoadda.apis.vehicle.service.impl;

import com.autoadda.apis.vehicle.clients.AuthClient;
import com.autoadda.apis.vehicle.entity.BorrowingTransfer;
import com.autoadda.apis.vehicle.entity.VehicleBorrowing;
import com.autoadda.apis.vehicle.entity.VehicleDetails;
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
public class BorrowingNotificationService {

    private static final String VEHICLE_BORROWINGS = "/vehicle-borrowings/";
    private static final String CUSTOMER = "CUSTOMER";

    private final AuthClient authClient;

    @Async
    public void sendBorrowingRequestNotification(VehicleBorrowing borrowing, VehicleDetails vehicle) {
        try {
            log.info("Sending borrowing request notifications for borrowing ID: {}", borrowing.getId());

            UserResponse borrower = authClient.getUser(borrowing.getBorrowerId());
            UserResponse owner = authClient.getUser(borrowing.getOwnerId());

            sendNotificationsToOwnerForNewRequest(borrowing, vehicle, borrower, owner);
            sendConfirmationToBorrower(borrowing, vehicle, borrower);

            log.info("Borrowing request notifications sent for borrowing ID: {}", borrowing.getId());
        } catch (Exception e) {
            log.error("Failed to send borrowing request notifications for borrowing {}: {}",
                    borrowing.getId(), e.getMessage(), e);
        }
    }

    @Async
    public void sendApprovalNotification(VehicleBorrowing borrowing, VehicleDetails vehicle) {
        try {
            log.info("Sending approval notifications for borrowing ID: {}", borrowing.getId());

            UserResponse borrower = authClient.getUser(borrowing.getBorrowerId());
            UserResponse owner = authClient.getUser(borrowing.getOwnerId());

            sendApprovalToBorrower(borrowing, vehicle, borrower, owner);
            sendApprovalConfirmationToOwner(borrowing, vehicle, borrower, owner);

            log.info("Approval notifications sent for borrowing ID: {}", borrowing.getId());
        } catch (Exception e) {
            log.error("Failed to send approval notifications for borrowing {}: {}",
                    borrowing.getId(), e.getMessage(), e);
        }
    }

    @Async
    public void sendRejectionNotification(VehicleBorrowing borrowing, VehicleDetails vehicle) {
        try {
            log.info("Sending rejection notifications for borrowing ID: {}", borrowing.getId());

            UserResponse borrower = authClient.getUser(borrowing.getBorrowerId());
            UserResponse owner = authClient.getUser(borrowing.getOwnerId());

            sendRejectionToBorrower(borrowing, vehicle, borrower, owner);
            sendRejectionConfirmationToOwner(borrowing, vehicle, borrower, owner);

            log.info("Rejection notifications sent for borrowing ID: {}", borrowing.getId());
        } catch (Exception e) {
            log.error("Failed to send rejection notifications for borrowing {}: {}",
                    borrowing.getId(), e.getMessage(), e);
        }
    }

    @Async
    public void sendBorrowingStartedNotification(VehicleBorrowing borrowing, VehicleDetails vehicle) {
        try {
            log.info("Sending borrowing started notifications for borrowing ID: {}", borrowing.getId());

            UserResponse borrower = authClient.getUser(borrowing.getBorrowerId());
            UserResponse owner = authClient.getUser(borrowing.getOwnerId());

            sendBorrowingStartedToOwner(borrowing, vehicle, borrower, owner);

            log.info("Borrowing started notifications sent for borrowing ID: {}", borrowing.getId());
        } catch (Exception e) {
            log.error("Failed to send borrowing started notifications for borrowing {}: {}",
                    borrowing.getId(), e.getMessage(), e);
        }
    }

    @Async
    public void sendReturnInitiatedNotification(VehicleBorrowing borrowing, VehicleDetails vehicle) {
        try {
            log.info("Sending return initiated notifications for borrowing ID: {}", borrowing.getId());

            UserResponse borrower = authClient.getUser(borrowing.getBorrowerId());
            UserResponse owner = authClient.getUser(borrowing.getOwnerId());

            sendReturnInitiatedToOwner(borrowing, vehicle, borrower, owner);

            log.info("Return initiated notifications sent for borrowing ID: {}", borrowing.getId());
        } catch (Exception e) {
            log.error("Failed to send return initiated notifications for borrowing {}: {}",
                    borrowing.getId(), e.getMessage(), e);
        }
    }

    @Async
    public void sendReturnConfirmedNotification(VehicleBorrowing borrowing, VehicleDetails vehicle) {
        try {
            log.info("Sending return confirmed notifications for borrowing ID: {}", borrowing.getId());

            UserResponse borrower = authClient.getUser(borrowing.getBorrowerId());
            UserResponse owner = authClient.getUser(borrowing.getOwnerId());

            sendReturnConfirmedToBorrower(borrowing, vehicle, borrower);
            sendReturnConfirmedToOwner(borrowing, vehicle, owner);

            log.info("Return confirmed notifications sent for borrowing ID: {}", borrowing.getId());
        } catch (Exception e) {
            log.error("Failed to send return confirmed notifications for borrowing {}: {}",
                    borrowing.getId(), e.getMessage(), e);
        }
    }

    @Async
    public void sendCancellationNotification(VehicleBorrowing borrowing, VehicleDetails vehicle) {
        try {
            log.info("Sending cancellation notifications for borrowing ID: {}", borrowing.getId());

            UserResponse borrower = authClient.getUser(borrowing.getBorrowerId());
            UserResponse owner = authClient.getUser(borrowing.getOwnerId());

            sendCancellationToOwner(borrowing, vehicle, borrower, owner);

            log.info("Cancellation notifications sent for borrowing ID: {}", borrowing.getId());
        } catch (Exception e) {
            log.error("Failed to send cancellation notifications for borrowing {}: {}",
                    borrowing.getId(), e.getMessage(), e);
        }
    }

    @Async
    public void sendOwnerAssignmentNotification(VehicleBorrowing borrowing, VehicleDetails vehicle) {
        try {
            log.info("Sending owner assignment notifications for borrowing ID: {}", borrowing.getId());

            UserResponse borrower = authClient.getUser(borrowing.getBorrowerId());
            UserResponse owner = authClient.getUser(borrowing.getOwnerId());

            sendAssignmentToBorrower(borrowing, vehicle, borrower, owner);
            sendAssignmentConfirmationToOwner(borrowing, vehicle, borrower, owner);

            log.info("Owner assignment notifications sent for borrowing ID: {}", borrowing.getId());
        } catch (Exception e) {
            log.error("Failed to send owner assignment notifications for borrowing {}: {}",
                    borrowing.getId(), e.getMessage(), e);
        }
    }

    @Async
    public void sendTransferRequestedNotification(VehicleBorrowing borrowing, BorrowingTransfer transfer,
                                                  VehicleDetails vehicle) {
        try {
            log.info("Sending transfer requested notifications for borrowing ID: {}, transfer ID: {}",
                    borrowing.getId(), transfer.getId());

            UserResponse fromBorrower = authClient.getUser(transfer.getFromBorrowerId());
            UserResponse toBorrower = authClient.getUser(transfer.getToBorrowerId());
            UserResponse owner = authClient.getUser(borrowing.getOwnerId());

            sendTransferRequestedToRecipient(borrowing, transfer, vehicle, fromBorrower, toBorrower);
            sendTransferRequestedToOwner(borrowing, transfer, vehicle, fromBorrower, toBorrower, owner);

            log.info("Transfer requested notifications sent for transfer ID: {}", transfer.getId());
        } catch (Exception e) {
            log.error("Failed to send transfer requested notifications for transfer {}: {}",
                    transfer.getId(), e.getMessage(), e);
        }
    }

    @Async
    public void sendTransferAcceptedNotification(VehicleBorrowing borrowing, BorrowingTransfer transfer,
                                                 VehicleDetails vehicle) {
        try {
            log.info("Sending transfer accepted notifications for transfer ID: {}", transfer.getId());

            UserResponse fromBorrower = authClient.getUser(transfer.getFromBorrowerId());
            UserResponse toBorrower = authClient.getUser(transfer.getToBorrowerId());
            UserResponse owner = authClient.getUser(borrowing.getOwnerId());

            sendTransferAcceptedToPreviousBorrower(borrowing, transfer, vehicle, fromBorrower, toBorrower);
            sendTransferAcceptedToOwner(borrowing, transfer, vehicle, fromBorrower, toBorrower, owner);

            log.info("Transfer accepted notifications sent for transfer ID: {}", transfer.getId());
        } catch (Exception e) {
            log.error("Failed to send transfer accepted notifications for transfer {}: {}",
                    transfer.getId(), e.getMessage(), e);
        }
    }

    @Async
    public void sendTransferRejectedNotification(VehicleBorrowing borrowing, BorrowingTransfer transfer,
                                                 VehicleDetails vehicle) {
        try {
            log.info("Sending transfer rejected notifications for transfer ID: {}", transfer.getId());

            UserResponse fromBorrower = authClient.getUser(transfer.getFromBorrowerId());
            UserResponse toBorrower = authClient.getUser(transfer.getToBorrowerId());
            UserResponse owner = authClient.getUser(borrowing.getOwnerId());

            sendTransferRejectedToRequester(borrowing, transfer, vehicle, fromBorrower, toBorrower);
            sendTransferRejectedToOwner(borrowing, transfer, vehicle, fromBorrower, toBorrower, owner);

            log.info("Transfer rejected notifications sent for transfer ID: {}", transfer.getId());
        } catch (Exception e) {
            log.error("Failed to send transfer rejected notifications for transfer {}: {}",
                    transfer.getId(), e.getMessage(), e);
        }
    }

    @Async
    public void sendTransferCancelledNotification(VehicleBorrowing borrowing, BorrowingTransfer transfer,
                                                  VehicleDetails vehicle) {
        try {
            log.info("Sending transfer cancelled notifications for transfer ID: {}", transfer.getId());

            UserResponse fromBorrower = authClient.getUser(transfer.getFromBorrowerId());
            UserResponse toBorrower = authClient.getUser(transfer.getToBorrowerId());
            UserResponse owner = authClient.getUser(borrowing.getOwnerId());

            sendTransferCancelledToRecipient(borrowing, transfer, vehicle, fromBorrower, toBorrower);
            sendTransferCancelledToOwner(borrowing, transfer, vehicle, fromBorrower, toBorrower, owner);

            log.info("Transfer cancelled notifications sent for transfer ID: {}", transfer.getId());
        } catch (Exception e) {
            log.error("Failed to send transfer cancelled notifications for transfer {}: {}",
                    transfer.getId(), e.getMessage(), e);
        }
    }

    private void sendAssignmentToBorrower(VehicleBorrowing borrowing, VehicleDetails vehicle,
                                          UserResponse borrower, UserResponse owner) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse =
                    authClient.getPreferencesByCustomerId1(borrower.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getEmailOnServiceUpdates())) {
                String subject = String.format("Vehicle Assigned to You - %s %s",
                        vehicle.getBrand(), vehicle.getModel());
                String body = buildAssignmentEmailBody(borrowing, vehicle, borrower, owner);

                authClient.sendEmail(borrower.getId(), EmailRequest.builder()
                        .to(borrower.getEmail())
                        .subject(subject)
                        .body(body)
                        .build());
            }

            if (prefs == null || Boolean.TRUE.equals(prefs.getWebpushOnServiceUpdates())) {
                authClient.sendNotification(new SendNotificationRequest(
                        String.valueOf(borrower.getId()),
                        CUSTOMER,
                        "Vehicle Assigned to You",
                        String.format("%s has assigned you their %s %s. You can pick it up now.",
                                owner.getName(), vehicle.getBrand(), vehicle.getModel()),
                        VEHICLE_BORROWINGS + borrowing.getId()
                ));
            }

            if (prefs != null && Boolean.TRUE.equals(prefs.getSmsOnServiceUpdates())) {
                String smsMessage = String.format("AutoAdda: %s has assigned you their %s %s. You can pick it up now.",
                        owner.getName(), vehicle.getBrand(), vehicle.getModel());
                authClient.sendSms(new SmsRequest(borrower.getMobileNumber(), smsMessage));
            }
        } catch (Exception e) {
            log.error("Failed to send assignment to borrower {}: {}", borrower.getId(), e.getMessage());
        }
    }

    private String buildAssignmentEmailBody(VehicleBorrowing borrowing, VehicleDetails vehicle,
                                            UserResponse borrower, UserResponse owner) {
        return String.format("""
                Dear %s,

                Great news! %s has assigned you their vehicle directly.

                BORROWING DETAILS:
                - Borrowing ID: %d
                - Vehicle: %s %s (%s)
                - Registration: %s
                - Owner: %s
                - Owner Contact: %s
                - Purpose: %s
                - Expected Return: %s

                You can now coordinate with the owner to pick up the vehicle. Once you have the vehicle, please mark the borrowing as "Started" in the app.

                Best regards,
                AutoAdda Team""",
                borrower.getName(),
                owner.getName(),
                borrowing.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear() != null ? vehicle.getYear() : "N/A",
                vehicle.getNumber() != null ? vehicle.getNumber() : "N/A",
                owner.getName(),
                owner.getMobileNumber(),
                borrowing.getBorrowingPurpose() != null ? borrowing.getBorrowingPurpose() : "Not specified",
                borrowing.getExpectedReturnTime()
        );
    }

    private void sendNotificationsToOwnerForNewRequest(VehicleBorrowing borrowing, VehicleDetails vehicle,
                                                        UserResponse borrower, UserResponse owner) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse =
                    authClient.getPreferencesByCustomerId1(owner.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getEmailOnServiceUpdates())) {
                String subject = String.format("Vehicle Borrowing Request for Your %s %s",
                        vehicle.getBrand(), vehicle.getModel());
                String body = buildOwnerRequestEmailBody(borrowing, vehicle, borrower, owner);

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
                        "Vehicle Borrowing Request",
                        String.format("%s wants to borrow your %s %s",
                                borrower.getName(), vehicle.getBrand(), vehicle.getModel()),
                        VEHICLE_BORROWINGS + borrowing.getId()
                ));
            }

            if (prefs != null && Boolean.TRUE.equals(prefs.getSmsOnServiceUpdates())) {
                String smsMessage = String.format("AutoAdda: %s has requested to borrow your %s %s. Please review.",
                        borrower.getName(), vehicle.getBrand(), vehicle.getModel());
                authClient.sendSms(new SmsRequest(owner.getMobileNumber(), smsMessage));
            }
        } catch (Exception e) {
            log.error("Failed to send notifications to owner {}: {}", owner.getId(), e.getMessage());
        }
    }

    private void sendConfirmationToBorrower(VehicleBorrowing borrowing, VehicleDetails vehicle,
                                            UserResponse borrower) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse =
                    authClient.getPreferencesByCustomerId1(borrower.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getWebpushOnServiceUpdates())) {
                authClient.sendNotification(new SendNotificationRequest(
                        String.valueOf(borrower.getId()),
                        CUSTOMER,
                        "Borrowing Request Submitted",
                        String.format("Your request to borrow %s %s is pending owner approval",
                                vehicle.getBrand(), vehicle.getModel()),
                        VEHICLE_BORROWINGS + borrowing.getId()
                ));
            }
        } catch (Exception e) {
            log.error("Failed to send confirmation to borrower {}: {}", borrower.getId(), e.getMessage());
        }
    }

    private void sendApprovalToBorrower(VehicleBorrowing borrowing, VehicleDetails vehicle,
                                         UserResponse borrower, UserResponse owner) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse =
                    authClient.getPreferencesByCustomerId1(borrower.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getEmailOnServiceUpdates())) {
                String subject = String.format("Your Borrowing Request Approved - %s %s",
                        vehicle.getBrand(), vehicle.getModel());
                String body = buildBorrowerApprovalEmailBody(borrowing, vehicle, borrower, owner);

                authClient.sendEmail(borrower.getId(), EmailRequest.builder()
                        .to(borrower.getEmail())
                        .subject(subject)
                        .body(body)
                        .build());
            }

            if (prefs == null || Boolean.TRUE.equals(prefs.getWebpushOnServiceUpdates())) {
                authClient.sendNotification(new SendNotificationRequest(
                        String.valueOf(borrower.getId()),
                        CUSTOMER,
                        "Request Approved!",
                        String.format("Your request to borrow %s %s has been approved. You can now pick up the vehicle.",
                                vehicle.getBrand(), vehicle.getModel()),
                        VEHICLE_BORROWINGS + borrowing.getId()
                ));
            }

            if (prefs != null && Boolean.TRUE.equals(prefs.getSmsOnServiceUpdates())) {
                String smsMessage = String.format("AutoAdda: Your borrowing request for %s %s has been APPROVED. Contact the owner to arrange pickup.",
                        vehicle.getBrand(), vehicle.getModel());
                authClient.sendSms(new SmsRequest(borrower.getMobileNumber(), smsMessage));
            }
        } catch (Exception e) {
            log.error("Failed to send approval to borrower {}: {}", borrower.getId(), e.getMessage());
        }
    }

    private void sendRejectionToBorrower(VehicleBorrowing borrowing, VehicleDetails vehicle,
                                          UserResponse borrower, UserResponse owner) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse =
                    authClient.getPreferencesByCustomerId1(borrower.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getEmailOnServiceUpdates())) {
                String subject = String.format("Your Borrowing Request Declined - %s %s",
                        vehicle.getBrand(), vehicle.getModel());
                String body = buildBorrowerRejectionEmailBody(borrowing, vehicle, borrower, owner);

                authClient.sendEmail(borrower.getId(), EmailRequest.builder()
                        .to(borrower.getEmail())
                        .subject(subject)
                        .body(body)
                        .build());
            }

            if (prefs == null || Boolean.TRUE.equals(prefs.getWebpushOnServiceUpdates())) {
                authClient.sendNotification(new SendNotificationRequest(
                        String.valueOf(borrower.getId()),
                        CUSTOMER,
                        "Request Declined",
                        String.format("Your request to borrow %s %s has been declined by the owner.",
                                vehicle.getBrand(), vehicle.getModel()),
                        VEHICLE_BORROWINGS + borrowing.getId()
                ));
            }
        } catch (Exception e) {
            log.error("Failed to send rejection to borrower {}: {}", borrower.getId(), e.getMessage());
        }
    }

    private void sendBorrowingStartedToOwner(VehicleBorrowing borrowing, VehicleDetails vehicle,
                                              UserResponse borrower, UserResponse owner) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse =
                    authClient.getPreferencesByCustomerId1(owner.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getEmailOnServiceUpdates())) {
                String subject = String.format("Vehicle Picked Up - %s %s",
                        vehicle.getBrand(), vehicle.getModel());
                String body = buildBorrowingStartedEmailBody(borrowing, vehicle, borrower, owner);
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
                        "Vehicle Picked Up",
                        String.format("%s has picked up your %s %s",
                                borrower.getName(), vehicle.getBrand(), vehicle.getModel()),
                        VEHICLE_BORROWINGS + borrowing.getId()
                ));
            }

            if (prefs != null && Boolean.TRUE.equals(prefs.getSmsOnServiceUpdates())) {
                String smsMessage = String.format("AutoAdda: %s has picked up your %s %s.",
                        borrower.getName(), vehicle.getBrand(), vehicle.getModel());
                authClient.sendSms(new SmsRequest(owner.getMobileNumber(), smsMessage));
            }
        } catch (Exception e) {
            log.error("Failed to send borrowing started notification to owner {}: {}", owner.getId(), e.getMessage());
        }
    }

    private void sendReturnInitiatedToOwner(VehicleBorrowing borrowing, VehicleDetails vehicle,
                                             UserResponse borrower, UserResponse owner) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse =
                    authClient.getPreferencesByCustomerId1(owner.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getEmailOnServiceUpdates())) {
                String subject = String.format("Vehicle Return Initiated - %s %s",
                        vehicle.getBrand(), vehicle.getModel());
                String body = buildReturnInitiatedEmailBody(borrowing, vehicle, borrower, owner);

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
                        "Vehicle Return Initiated",
                        String.format("%s has initiated return of your %s %s. Please confirm when received.",
                                borrower.getName(), vehicle.getBrand(), vehicle.getModel()),
                        VEHICLE_BORROWINGS + borrowing.getId()
                ));
            }
        } catch (Exception e) {
            log.error("Failed to send return initiated notification to owner {}: {}", owner.getId(), e.getMessage());
        }
    }

    private void sendReturnConfirmedToBorrower(VehicleBorrowing borrowing, VehicleDetails vehicle,
                                                UserResponse borrower) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse =
                    authClient.getPreferencesByCustomerId1(borrower.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getWebpushOnServiceUpdates())) {
                authClient.sendNotification(new SendNotificationRequest(
                        String.valueOf(borrower.getId()),
                        CUSTOMER,
                        "Vehicle Return Completed",
                        String.format("Your borrowing of %s %s has been completed. Thank you!",
                                vehicle.getBrand(), vehicle.getModel()),
                        VEHICLE_BORROWINGS + borrowing.getId()
                ));
            }
        } catch (Exception e) {
            log.error("Failed to send return confirmed notification to borrower {}: {}", borrower.getId(), e.getMessage());
        }
    }

    private void sendReturnConfirmedToOwner(VehicleBorrowing borrowing, VehicleDetails vehicle,
                                             UserResponse owner) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse =
                    authClient.getPreferencesByCustomerId1(owner.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getEmailOnServiceUpdates())) {
                String subject = String.format("Vehicle Return Completed - %s %s",
                        vehicle.getBrand(), vehicle.getModel());
                String body = buildReturnCompletedOwnerEmailBody(borrowing, vehicle, owner);
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
                        "Vehicle Return Completed",
                        String.format("Your %s %s has been returned successfully.",
                                vehicle.getBrand(), vehicle.getModel()),
                        VEHICLE_BORROWINGS + borrowing.getId()
                ));
            }

            if (prefs != null && Boolean.TRUE.equals(prefs.getSmsOnServiceUpdates())) {
                String smsMessage = String.format("AutoAdda: Your %s %s has been returned successfully.",
                        vehicle.getBrand(), vehicle.getModel());
                authClient.sendSms(new SmsRequest(owner.getMobileNumber(), smsMessage));
            }
        } catch (Exception e) {
            log.error("Failed to send return confirmed notification to owner {}: {}", owner.getId(), e.getMessage());
        }
    }

    private void sendCancellationToOwner(VehicleBorrowing borrowing, VehicleDetails vehicle,
                                          UserResponse borrower, UserResponse owner) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse =
                    authClient.getPreferencesByCustomerId1(owner.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getEmailOnServiceUpdates())) {
                String subject = String.format("Borrowing Request Cancelled - %s %s",
                        vehicle.getBrand(), vehicle.getModel());
                String body = buildCancellationOwnerEmailBody(borrowing, vehicle, borrower, owner);
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
                        "Borrowing Request Cancelled",
                        String.format("%s has cancelled their borrowing request for your %s %s",
                                borrower.getName(), vehicle.getBrand(), vehicle.getModel()),
                        VEHICLE_BORROWINGS + borrowing.getId()
                ));
            }

            if (prefs != null && Boolean.TRUE.equals(prefs.getSmsOnServiceUpdates())) {
                String smsMessage = String.format("AutoAdda: %s has cancelled their request to borrow your %s %s.",
                        borrower.getName(), vehicle.getBrand(), vehicle.getModel());
                authClient.sendSms(new SmsRequest(owner.getMobileNumber(), smsMessage));
            }
        } catch (Exception e) {
            log.error("Failed to send cancellation notification to owner {}: {}", owner.getId(), e.getMessage());
        }
    }

    private String buildOwnerRequestEmailBody(VehicleBorrowing borrowing, VehicleDetails vehicle,
                                               UserResponse borrower, UserResponse owner) {
        return String.format("""
                Dear %s,

                You have received a vehicle borrowing request.

                BORROWING DETAILS:
                - Borrowing ID: %d
                - Vehicle: %s %s (%s)
                - Registration: %s
                - Borrower: %s
                - Borrower Contact: %s
                - Purpose: %s
                - Expected Return: %s
                - Request Date: %s

                Please log in to your AutoAdda account to approve or reject this request.

                Best regards,
                AutoAdda Team""",
                owner.getName(),
                borrowing.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear() != null ? vehicle.getYear() : "N/A",
                vehicle.getNumber() != null ? vehicle.getNumber() : "N/A",
                borrower.getName(),
                borrower.getMobileNumber(),
                borrowing.getBorrowingPurpose() != null ? borrowing.getBorrowingPurpose() : "Not specified",
                borrowing.getExpectedReturnTime(),
                borrowing.getCreatedAt()
        );
    }

    private String buildBorrowerApprovalEmailBody(VehicleBorrowing borrowing, VehicleDetails vehicle,
                                                   UserResponse borrower, UserResponse owner) {
        return String.format("""
                Dear %s,

                Great news! Your vehicle borrowing request has been APPROVED.

                BORROWING DETAILS:
                - Borrowing ID: %d
                - Vehicle: %s %s (%s)
                - Registration: %s
                - Owner: %s
                - Owner Contact: %s
                - Expected Return: %s

                You can now coordinate with the owner to pick up the vehicle. Once you have the vehicle, please mark the borrowing as "Started" in the app.

                Best regards,
                AutoAdda Team""",
                borrower.getName(),
                borrowing.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear() != null ? vehicle.getYear() : "N/A",
                vehicle.getNumber() != null ? vehicle.getNumber() : "N/A",
                owner.getName(),
                owner.getMobileNumber(),
                borrowing.getExpectedReturnTime()
        );
    }

    private String buildBorrowerRejectionEmailBody(VehicleBorrowing borrowing, VehicleDetails vehicle,
                                                    UserResponse borrower, UserResponse owner) {
        return String.format("""
                Dear %s,

                We regret to inform you that your vehicle borrowing request has been DECLINED.

                BORROWING DETAILS:
                - Borrowing ID: %d
                - Vehicle: %s %s (%s)
                - Owner: %s
                - Rejection Reason: %s

                If you have any questions, please contact the vehicle owner directly.

                Best regards,
                AutoAdda Team""",
                borrower.getName(),
                borrowing.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear() != null ? vehicle.getYear() : "N/A",
                owner.getName(),
                borrowing.getRejectionReason() != null ? borrowing.getRejectionReason() : "Not provided"
        );
    }

    private void sendApprovalConfirmationToOwner(VehicleBorrowing borrowing, VehicleDetails vehicle,
                                                  UserResponse borrower, UserResponse owner) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse =
                    authClient.getPreferencesByCustomerId1(owner.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getEmailOnServiceUpdates())) {
                String subject = String.format("You Approved a Borrowing Request - %s %s",
                        vehicle.getBrand(), vehicle.getModel());
                String body = buildOwnerApprovalConfirmationEmailBody(borrowing, vehicle, borrower, owner);
                authClient.sendEmail(owner.getId(), EmailRequest.builder()
                        .to(owner.getEmail())
                        .subject(subject)
                        .body(body)
                        .build());
            }
        } catch (Exception e) {
            log.error("Failed to send approval confirmation to owner {}: {}", owner.getId(), e.getMessage());
        }
    }

    private void sendRejectionConfirmationToOwner(VehicleBorrowing borrowing, VehicleDetails vehicle,
                                                   UserResponse borrower, UserResponse owner) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse =
                    authClient.getPreferencesByCustomerId1(owner.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getEmailOnServiceUpdates())) {
                String subject = String.format("You Declined a Borrowing Request - %s %s",
                        vehicle.getBrand(), vehicle.getModel());
                String body = buildOwnerRejectionConfirmationEmailBody(borrowing, vehicle, borrower, owner);
                authClient.sendEmail(owner.getId(), EmailRequest.builder()
                        .to(owner.getEmail())
                        .subject(subject)
                        .body(body)
                        .build());
            }
        } catch (Exception e) {
            log.error("Failed to send rejection confirmation to owner {}: {}", owner.getId(), e.getMessage());
        }
    }

    private void sendAssignmentConfirmationToOwner(VehicleBorrowing borrowing, VehicleDetails vehicle,
                                                    UserResponse borrower, UserResponse owner) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse =
                    authClient.getPreferencesByCustomerId1(owner.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getEmailOnServiceUpdates())) {
                String subject = String.format("You Assigned Your Vehicle - %s %s",
                        vehicle.getBrand(), vehicle.getModel());
                String body = buildOwnerAssignmentConfirmationEmailBody(borrowing, vehicle, borrower, owner);
                authClient.sendEmail(owner.getId(), EmailRequest.builder()
                        .to(owner.getEmail())
                        .subject(subject)
                        .body(body)
                        .build());
            }
        } catch (Exception e) {
            log.error("Failed to send assignment confirmation to owner {}: {}", owner.getId(), e.getMessage());
        }
    }

    private void sendTransferRequestedToRecipient(VehicleBorrowing borrowing, BorrowingTransfer transfer,
                                                   VehicleDetails vehicle, UserResponse fromBorrower,
                                                   UserResponse toBorrower) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse =
                    authClient.getPreferencesByCustomerId1(toBorrower.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getEmailOnServiceUpdates())) {
                String subject = String.format("Vehicle Transfer Requested - %s %s",
                        vehicle.getBrand(), vehicle.getModel());
                String body = buildTransferRequestRecipientEmailBody(borrowing, transfer, vehicle,
                        fromBorrower, toBorrower);
                authClient.sendEmail(toBorrower.getId(), EmailRequest.builder()
                        .to(toBorrower.getEmail())
                        .subject(subject)
                        .body(body)
                        .build());
            }

            if (prefs == null || Boolean.TRUE.equals(prefs.getWebpushOnServiceUpdates())) {
                authClient.sendNotification(new SendNotificationRequest(
                        String.valueOf(toBorrower.getId()),
                        CUSTOMER,
                        "Vehicle Transfer Requested",
                        String.format("%s wants to hand over %s %s to you. Please review.",
                                fromBorrower.getName(), vehicle.getBrand(), vehicle.getModel()),
                        VEHICLE_BORROWINGS + borrowing.getId()
                ));
            }

            if (prefs != null && Boolean.TRUE.equals(prefs.getSmsOnServiceUpdates())) {
                String smsMessage = String.format("AutoAdda: %s wants to transfer %s %s to you. Please review.",
                        fromBorrower.getName(), vehicle.getBrand(), vehicle.getModel());
                authClient.sendSms(new SmsRequest(toBorrower.getMobileNumber(), smsMessage));
            }
        } catch (Exception e) {
            log.error("Failed to send transfer request to recipient {}: {}", toBorrower.getId(), e.getMessage());
        }
    }

    private void sendTransferRequestedToOwner(VehicleBorrowing borrowing, BorrowingTransfer transfer,
                                               VehicleDetails vehicle, UserResponse fromBorrower,
                                               UserResponse toBorrower, UserResponse owner) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse =
                    authClient.getPreferencesByCustomerId1(owner.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getEmailOnServiceUpdates())) {
                String subject = String.format("Vehicle Transfer Requested - %s %s",
                        vehicle.getBrand(), vehicle.getModel());
                String body = buildTransferRequestOwnerEmailBody(borrowing, transfer, vehicle,
                        fromBorrower, toBorrower, owner);
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
                        "Vehicle Transfer Requested",
                        String.format("%s wants to hand over your %s %s to %s",
                                fromBorrower.getName(), vehicle.getBrand(), vehicle.getModel(), toBorrower.getName()),
                        VEHICLE_BORROWINGS + borrowing.getId()
                ));
            }

            if (prefs != null && Boolean.TRUE.equals(prefs.getSmsOnServiceUpdates())) {
                String smsMessage = String.format("AutoAdda: %s wants to transfer your %s %s to %s.",
                        fromBorrower.getName(), vehicle.getBrand(), vehicle.getModel(), toBorrower.getName());
                authClient.sendSms(new SmsRequest(owner.getMobileNumber(), smsMessage));
            }
        } catch (Exception e) {
            log.error("Failed to send transfer request to owner {}: {}", owner.getId(), e.getMessage());
        }
    }

    private void sendTransferAcceptedToPreviousBorrower(VehicleBorrowing borrowing, BorrowingTransfer transfer,
                                                        VehicleDetails vehicle, UserResponse fromBorrower,
                                                        UserResponse toBorrower) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse =
                    authClient.getPreferencesByCustomerId1(fromBorrower.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getEmailOnServiceUpdates())) {
                String subject = String.format("Vehicle Transfer Accepted - %s %s",
                        vehicle.getBrand(), vehicle.getModel());
                String body = buildTransferAcceptedRequesterEmailBody(borrowing, transfer, vehicle,
                        fromBorrower, toBorrower);
                authClient.sendEmail(fromBorrower.getId(), EmailRequest.builder()
                        .to(fromBorrower.getEmail())
                        .subject(subject)
                        .body(body)
                        .build());
            }

            if (prefs == null || Boolean.TRUE.equals(prefs.getWebpushOnServiceUpdates())) {
                authClient.sendNotification(new SendNotificationRequest(
                        String.valueOf(fromBorrower.getId()),
                        CUSTOMER,
                        "Transfer Accepted",
                        String.format("%s has accepted the transfer of %s %s.",
                                toBorrower.getName(), vehicle.getBrand(), vehicle.getModel()),
                        VEHICLE_BORROWINGS + borrowing.getId()
                ));
            }

            if (prefs != null && Boolean.TRUE.equals(prefs.getSmsOnServiceUpdates())) {
                String smsMessage = String.format("AutoAdda: %s has accepted the transfer of %s %s.",
                        toBorrower.getName(), vehicle.getBrand(), vehicle.getModel());
                authClient.sendSms(new SmsRequest(fromBorrower.getMobileNumber(), smsMessage));
            }
        } catch (Exception e) {
            log.error("Failed to send transfer accepted to previous borrower {}: {}",
                    fromBorrower.getId(), e.getMessage());
        }
    }

    private void sendTransferAcceptedToOwner(VehicleBorrowing borrowing, BorrowingTransfer transfer,
                                              VehicleDetails vehicle, UserResponse fromBorrower,
                                              UserResponse toBorrower, UserResponse owner) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse =
                    authClient.getPreferencesByCustomerId1(owner.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getEmailOnServiceUpdates())) {
                String subject = String.format("Vehicle Transfer Completed - %s %s",
                        vehicle.getBrand(), vehicle.getModel());
                String body = buildTransferAcceptedOwnerEmailBody(borrowing, transfer, vehicle,
                        fromBorrower, toBorrower, owner);
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
                        "Vehicle Transfer Completed",
                        String.format("%s has taken over your %s %s from %s.",
                                toBorrower.getName(), vehicle.getBrand(), vehicle.getModel(), fromBorrower.getName()),
                        VEHICLE_BORROWINGS + borrowing.getId()
                ));
            }

            if (prefs != null && Boolean.TRUE.equals(prefs.getSmsOnServiceUpdates())) {
                String smsMessage = String.format("AutoAdda: %s has taken over your %s %s from %s.",
                        toBorrower.getName(), vehicle.getBrand(), vehicle.getModel(), fromBorrower.getName());
                authClient.sendSms(new SmsRequest(owner.getMobileNumber(), smsMessage));
            }
        } catch (Exception e) {
            log.error("Failed to send transfer accepted to owner {}: {}", owner.getId(), e.getMessage());
        }
    }

    private void sendTransferRejectedToRequester(VehicleBorrowing borrowing, BorrowingTransfer transfer,
                                                  VehicleDetails vehicle, UserResponse fromBorrower,
                                                  UserResponse toBorrower) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse =
                    authClient.getPreferencesByCustomerId1(fromBorrower.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getEmailOnServiceUpdates())) {
                String subject = String.format("Vehicle Transfer Declined - %s %s",
                        vehicle.getBrand(), vehicle.getModel());
                String body = buildTransferRejectedRequesterEmailBody(borrowing, transfer, vehicle,
                        fromBorrower, toBorrower);
                authClient.sendEmail(fromBorrower.getId(), EmailRequest.builder()
                        .to(fromBorrower.getEmail())
                        .subject(subject)
                        .body(body)
                        .build());
            }

            if (prefs == null || Boolean.TRUE.equals(prefs.getWebpushOnServiceUpdates())) {
                authClient.sendNotification(new SendNotificationRequest(
                        String.valueOf(fromBorrower.getId()),
                        CUSTOMER,
                        "Transfer Declined",
                        String.format("%s declined the transfer of %s %s.",
                                toBorrower.getName(), vehicle.getBrand(), vehicle.getModel()),
                        VEHICLE_BORROWINGS + borrowing.getId()
                ));
            }

            if (prefs != null && Boolean.TRUE.equals(prefs.getSmsOnServiceUpdates())) {
                String smsMessage = String.format("AutoAdda: %s declined the transfer of %s %s.",
                        toBorrower.getName(), vehicle.getBrand(), vehicle.getModel());
                authClient.sendSms(new SmsRequest(fromBorrower.getMobileNumber(), smsMessage));
            }
        } catch (Exception e) {
            log.error("Failed to send transfer rejected to requester {}: {}",
                    fromBorrower.getId(), e.getMessage());
        }
    }

    private void sendTransferRejectedToOwner(VehicleBorrowing borrowing, BorrowingTransfer transfer,
                                              VehicleDetails vehicle, UserResponse fromBorrower,
                                              UserResponse toBorrower, UserResponse owner) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse =
                    authClient.getPreferencesByCustomerId1(owner.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getEmailOnServiceUpdates())) {
                String subject = String.format("Vehicle Transfer Declined - %s %s",
                        vehicle.getBrand(), vehicle.getModel());
                String body = buildTransferRejectedOwnerEmailBody(borrowing, transfer, vehicle,
                        fromBorrower, toBorrower, owner);
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
                        "Vehicle Transfer Declined",
                        String.format("%s declined to take over your %s %s. %s still holds the vehicle.",
                                toBorrower.getName(), vehicle.getBrand(), vehicle.getModel(), fromBorrower.getName()),
                        VEHICLE_BORROWINGS + borrowing.getId()
                ));
            }

            if (prefs != null && Boolean.TRUE.equals(prefs.getSmsOnServiceUpdates())) {
                String smsMessage = String.format("AutoAdda: %s declined the transfer of your %s %s. %s still holds it.",
                        toBorrower.getName(), vehicle.getBrand(), vehicle.getModel(), fromBorrower.getName());
                authClient.sendSms(new SmsRequest(owner.getMobileNumber(), smsMessage));
            }
        } catch (Exception e) {
            log.error("Failed to send transfer rejected to owner {}: {}", owner.getId(), e.getMessage());
        }
    }

    private void sendTransferCancelledToRecipient(VehicleBorrowing borrowing, BorrowingTransfer transfer,
                                                   VehicleDetails vehicle, UserResponse fromBorrower,
                                                   UserResponse toBorrower) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse =
                    authClient.getPreferencesByCustomerId1(toBorrower.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getEmailOnServiceUpdates())) {
                String subject = String.format("Vehicle Transfer Cancelled - %s %s",
                        vehicle.getBrand(), vehicle.getModel());
                String body = buildTransferCancelledRecipientEmailBody(borrowing, transfer, vehicle,
                        fromBorrower, toBorrower);
                authClient.sendEmail(toBorrower.getId(), EmailRequest.builder()
                        .to(toBorrower.getEmail())
                        .subject(subject)
                        .body(body)
                        .build());
            }

            if (prefs == null || Boolean.TRUE.equals(prefs.getWebpushOnServiceUpdates())) {
                authClient.sendNotification(new SendNotificationRequest(
                        String.valueOf(toBorrower.getId()),
                        CUSTOMER,
                        "Transfer Cancelled",
                        String.format("%s cancelled the transfer of %s %s.",
                                fromBorrower.getName(), vehicle.getBrand(), vehicle.getModel()),
                        VEHICLE_BORROWINGS + borrowing.getId()
                ));
            }

            if (prefs != null && Boolean.TRUE.equals(prefs.getSmsOnServiceUpdates())) {
                String smsMessage = String.format("AutoAdda: %s cancelled the transfer of %s %s.",
                        fromBorrower.getName(), vehicle.getBrand(), vehicle.getModel());
                authClient.sendSms(new SmsRequest(toBorrower.getMobileNumber(), smsMessage));
            }
        } catch (Exception e) {
            log.error("Failed to send transfer cancelled to recipient {}: {}",
                    toBorrower.getId(), e.getMessage());
        }
    }

    private void sendTransferCancelledToOwner(VehicleBorrowing borrowing, BorrowingTransfer transfer,
                                               VehicleDetails vehicle, UserResponse fromBorrower,
                                               UserResponse toBorrower, UserResponse owner) {
        try {
            ResponseEntity<CommunicationPreference> prefsResponse =
                    authClient.getPreferencesByCustomerId1(owner.getId());
            CommunicationPreference prefs = prefsResponse.getBody();

            if (prefs == null || Boolean.TRUE.equals(prefs.getEmailOnServiceUpdates())) {
                String subject = String.format("Vehicle Transfer Cancelled - %s %s",
                        vehicle.getBrand(), vehicle.getModel());
                String body = buildTransferCancelledOwnerEmailBody(borrowing, transfer, vehicle,
                        fromBorrower, toBorrower, owner);
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
                        "Vehicle Transfer Cancelled",
                        String.format("%s cancelled the transfer of your %s %s to %s.",
                                fromBorrower.getName(), vehicle.getBrand(), vehicle.getModel(), toBorrower.getName()),
                        VEHICLE_BORROWINGS + borrowing.getId()
                ));
            }

            if (prefs != null && Boolean.TRUE.equals(prefs.getSmsOnServiceUpdates())) {
                String smsMessage = String.format("AutoAdda: %s cancelled the transfer of your %s %s to %s.",
                        fromBorrower.getName(), vehicle.getBrand(), vehicle.getModel(), toBorrower.getName());
                authClient.sendSms(new SmsRequest(owner.getMobileNumber(), smsMessage));
            }
        } catch (Exception e) {
            log.error("Failed to send transfer cancelled to owner {}: {}", owner.getId(), e.getMessage());
        }
    }

    private String buildReturnInitiatedEmailBody(VehicleBorrowing borrowing, VehicleDetails vehicle,
                                                  UserResponse borrower, UserResponse owner) {
        return String.format("""
                Dear %s,

                %s has initiated the return of your vehicle.

                RETURN DETAILS:
                - Borrowing ID: %d
                - Vehicle: %s %s (%s)
                - Borrower: %s
                - Return Notes: %s

                Please confirm the return once you have received your vehicle back in satisfactory condition.

                Best regards,
                AutoAdda Team""",
                owner.getName(),
                borrower.getName(),
                borrowing.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear() != null ? vehicle.getYear() : "N/A",
                borrower.getName(),
                borrowing.getReturnNotes() != null ? borrowing.getReturnNotes() : "No notes provided"
        );
    }

    private String buildBorrowingStartedEmailBody(VehicleBorrowing borrowing, VehicleDetails vehicle,
                                                   UserResponse borrower, UserResponse owner) {
        return String.format("""
                Dear %s,

                %s has picked up your vehicle.

                BORROWING DETAILS:
                - Borrowing ID: %d
                - Vehicle: %s %s (%s)
                - Registration: %s
                - Borrower: %s
                - Borrower Contact: %s
                - Pick-up Time: %s
                - Expected Return: %s

                Best regards,
                AutoAdda Team""",
                owner.getName(),
                borrower.getName(),
                borrowing.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear() != null ? vehicle.getYear() : "N/A",
                vehicle.getNumber() != null ? vehicle.getNumber() : "N/A",
                borrower.getName(),
                borrower.getMobileNumber(),
                borrowing.getBorrowingTime(),
                borrowing.getExpectedReturnTime()
        );
    }

    private String buildReturnCompletedOwnerEmailBody(VehicleBorrowing borrowing, VehicleDetails vehicle,
                                                      UserResponse owner) {
        return String.format("""
                Dear %s,

                Your vehicle has been returned successfully.

                BORROWING DETAILS:
                - Borrowing ID: %d
                - Vehicle: %s %s (%s)
                - Registration: %s
                - Actual Return Time: %s

                Best regards,
                AutoAdda Team""",
                owner.getName(),
                borrowing.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear() != null ? vehicle.getYear() : "N/A",
                vehicle.getNumber() != null ? vehicle.getNumber() : "N/A",
                borrowing.getActualReturnTime()
        );
    }

    private String buildCancellationOwnerEmailBody(VehicleBorrowing borrowing, VehicleDetails vehicle,
                                                   UserResponse borrower, UserResponse owner) {
        return String.format("""
                Dear %s,

                %s has cancelled their borrowing request for your vehicle.

                BORROWING DETAILS:
                - Borrowing ID: %d
                - Vehicle: %s %s (%s)
                - Borrower: %s

                Best regards,
                AutoAdda Team""",
                owner.getName(),
                borrower.getName(),
                borrowing.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear() != null ? vehicle.getYear() : "N/A",
                borrower.getName()
        );
    }

    private String buildOwnerApprovalConfirmationEmailBody(VehicleBorrowing borrowing, VehicleDetails vehicle,
                                                           UserResponse borrower, UserResponse owner) {
        return String.format("""
                Dear %s,

                You approved a borrowing request for your vehicle.

                BORROWING DETAILS:
                - Borrowing ID: %d
                - Vehicle: %s %s (%s)
                - Borrower: %s
                - Borrower Contact: %s
                - Expected Return: %s

                Best regards,
                AutoAdda Team""",
                owner.getName(),
                borrowing.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear() != null ? vehicle.getYear() : "N/A",
                borrower.getName(),
                borrower.getMobileNumber(),
                borrowing.getExpectedReturnTime()
        );
    }

    private String buildOwnerRejectionConfirmationEmailBody(VehicleBorrowing borrowing, VehicleDetails vehicle,
                                                            UserResponse borrower, UserResponse owner) {
        return String.format("""
                Dear %s,

                You declined a borrowing request for your vehicle.

                BORROWING DETAILS:
                - Borrowing ID: %d
                - Vehicle: %s %s (%s)
                - Borrower: %s
                - Rejection Reason: %s

                Best regards,
                AutoAdda Team""",
                owner.getName(),
                borrowing.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear() != null ? vehicle.getYear() : "N/A",
                borrower.getName(),
                borrowing.getRejectionReason() != null ? borrowing.getRejectionReason() : "Not provided"
        );
    }

    private String buildOwnerAssignmentConfirmationEmailBody(VehicleBorrowing borrowing, VehicleDetails vehicle,
                                                             UserResponse borrower, UserResponse owner) {
        return String.format("""
                Dear %s,

                You assigned your vehicle to a borrower.

                BORROWING DETAILS:
                - Borrowing ID: %d
                - Vehicle: %s %s (%s)
                - Borrower: %s
                - Borrower Contact: %s
                - Expected Return: %s

                Best regards,
                AutoAdda Team""",
                owner.getName(),
                borrowing.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear() != null ? vehicle.getYear() : "N/A",
                borrower.getName(),
                borrower.getMobileNumber(),
                borrowing.getExpectedReturnTime()
        );
    }

    private String buildTransferRequestRecipientEmailBody(VehicleBorrowing borrowing, BorrowingTransfer transfer,
                                                           VehicleDetails vehicle, UserResponse fromBorrower,
                                                           UserResponse toBorrower) {
        return String.format("""
                Dear %s,

                %s would like to hand over a borrowed vehicle to you.

                TRANSFER DETAILS:
                - Borrowing ID: %d
                - Transfer ID: %d
                - Vehicle: %s %s (%s)
                - Registration: %s
                - Current Holder: %s
                - Current Holder Contact: %s
                - Reason: %s
                - Expected Return (inherited): %s

                Please log in to AutoAdda to accept or decline the transfer.

                Best regards,
                AutoAdda Team""",
                toBorrower.getName(),
                fromBorrower.getName(),
                borrowing.getId(),
                transfer.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear() != null ? vehicle.getYear() : "N/A",
                vehicle.getNumber() != null ? vehicle.getNumber() : "N/A",
                fromBorrower.getName(),
                fromBorrower.getMobileNumber(),
                transfer.getReason() != null ? transfer.getReason() : "Not specified",
                borrowing.getExpectedReturnTime()
        );
    }

    private String buildTransferRequestOwnerEmailBody(VehicleBorrowing borrowing, BorrowingTransfer transfer,
                                                       VehicleDetails vehicle, UserResponse fromBorrower,
                                                       UserResponse toBorrower, UserResponse owner) {
        return String.format("""
                Dear %s,

                A transfer has been requested on your vehicle.

                TRANSFER DETAILS:
                - Borrowing ID: %d
                - Transfer ID: %d
                - Vehicle: %s %s (%s)
                - Current Holder: %s
                - Proposed New Holder: %s
                - Reason: %s
                - Expected Return (unchanged): %s

                You are being notified for your records. The transfer does not require your approval.

                Best regards,
                AutoAdda Team""",
                owner.getName(),
                borrowing.getId(),
                transfer.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear() != null ? vehicle.getYear() : "N/A",
                fromBorrower.getName(),
                toBorrower.getName(),
                transfer.getReason() != null ? transfer.getReason() : "Not specified",
                borrowing.getExpectedReturnTime()
        );
    }

    private String buildTransferAcceptedRequesterEmailBody(VehicleBorrowing borrowing, BorrowingTransfer transfer,
                                                            VehicleDetails vehicle, UserResponse fromBorrower,
                                                            UserResponse toBorrower) {
        return String.format("""
                Dear %s,

                %s has accepted the transfer of the vehicle you were borrowing.

                TRANSFER DETAILS:
                - Borrowing ID: %d
                - Transfer ID: %d
                - Vehicle: %s %s (%s)
                - New Holder: %s
                - New Holder Contact: %s
                - Transferred At: %s

                Best regards,
                AutoAdda Team""",
                fromBorrower.getName(),
                toBorrower.getName(),
                borrowing.getId(),
                transfer.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear() != null ? vehicle.getYear() : "N/A",
                toBorrower.getName(),
                toBorrower.getMobileNumber(),
                transfer.getResolvedAt()
        );
    }

    private String buildTransferAcceptedOwnerEmailBody(VehicleBorrowing borrowing, BorrowingTransfer transfer,
                                                        VehicleDetails vehicle, UserResponse fromBorrower,
                                                        UserResponse toBorrower, UserResponse owner) {
        return String.format("""
                Dear %s,

                Your vehicle has been handed over to a new holder.

                TRANSFER DETAILS:
                - Borrowing ID: %d
                - Transfer ID: %d
                - Vehicle: %s %s (%s)
                - Previous Holder: %s
                - New Holder: %s
                - New Holder Contact: %s
                - Expected Return (unchanged): %s

                Best regards,
                AutoAdda Team""",
                owner.getName(),
                borrowing.getId(),
                transfer.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear() != null ? vehicle.getYear() : "N/A",
                fromBorrower.getName(),
                toBorrower.getName(),
                toBorrower.getMobileNumber(),
                borrowing.getExpectedReturnTime()
        );
    }

    private String buildTransferRejectedRequesterEmailBody(VehicleBorrowing borrowing, BorrowingTransfer transfer,
                                                            VehicleDetails vehicle, UserResponse fromBorrower,
                                                            UserResponse toBorrower) {
        return String.format("""
                Dear %s,

                %s declined the transfer of %s %s. You remain the current holder.

                TRANSFER DETAILS:
                - Borrowing ID: %d
                - Transfer ID: %d
                - Reason: %s

                Best regards,
                AutoAdda Team""",
                fromBorrower.getName(),
                toBorrower.getName(),
                vehicle.getBrand(),
                vehicle.getModel(),
                borrowing.getId(),
                transfer.getId(),
                transfer.getReason() != null ? transfer.getReason() : "Not provided"
        );
    }

    private String buildTransferRejectedOwnerEmailBody(VehicleBorrowing borrowing, BorrowingTransfer transfer,
                                                        VehicleDetails vehicle, UserResponse fromBorrower,
                                                        UserResponse toBorrower, UserResponse owner) {
        return String.format("""
                Dear %s,

                A transfer attempt on your vehicle was declined.

                TRANSFER DETAILS:
                - Borrowing ID: %d
                - Transfer ID: %d
                - Vehicle: %s %s (%s)
                - Current Holder (unchanged): %s
                - Declined By: %s

                Best regards,
                AutoAdda Team""",
                owner.getName(),
                borrowing.getId(),
                transfer.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear() != null ? vehicle.getYear() : "N/A",
                fromBorrower.getName(),
                toBorrower.getName()
        );
    }

    private String buildTransferCancelledRecipientEmailBody(VehicleBorrowing borrowing, BorrowingTransfer transfer,
                                                             VehicleDetails vehicle, UserResponse fromBorrower,
                                                             UserResponse toBorrower) {
        return String.format("""
                Dear %s,

                %s cancelled the pending transfer of %s %s to you.

                TRANSFER DETAILS:
                - Borrowing ID: %d
                - Transfer ID: %d

                Best regards,
                AutoAdda Team""",
                toBorrower.getName(),
                fromBorrower.getName(),
                vehicle.getBrand(),
                vehicle.getModel(),
                borrowing.getId(),
                transfer.getId()
        );
    }

    private String buildTransferCancelledOwnerEmailBody(VehicleBorrowing borrowing, BorrowingTransfer transfer,
                                                         VehicleDetails vehicle, UserResponse fromBorrower,
                                                         UserResponse toBorrower, UserResponse owner) {
        return String.format("""
                Dear %s,

                A pending transfer on your vehicle was cancelled.

                TRANSFER DETAILS:
                - Borrowing ID: %d
                - Transfer ID: %d
                - Vehicle: %s %s (%s)
                - Cancelled By: %s
                - Intended Recipient: %s

                Best regards,
                AutoAdda Team""",
                owner.getName(),
                borrowing.getId(),
                transfer.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear() != null ? vehicle.getYear() : "N/A",
                fromBorrower.getName(),
                toBorrower.getName()
        );
    }
}
