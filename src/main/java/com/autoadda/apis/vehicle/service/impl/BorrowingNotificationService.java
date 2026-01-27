package com.autoadda.apis.vehicle.service.impl;

import com.autoadda.apis.vehicle.clients.AuthClient;
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

            log.info("Owner assignment notifications sent for borrowing ID: {}", borrowing.getId());
        } catch (Exception e) {
            log.error("Failed to send owner assignment notifications for borrowing {}: {}",
                    borrowing.getId(), e.getMessage(), e);
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
}
