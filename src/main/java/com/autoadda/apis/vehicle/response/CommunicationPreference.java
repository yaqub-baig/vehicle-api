package com.autoadda.apis.vehicle.response;


import com.autoadda.apis.vehicle.util.Status;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunicationPreference {


    private Integer id;

    private Integer customerId; // Reference to the customer (could be VehicleOwner or User)

    private Boolean emailOnServiceUpdates;
    private Boolean smsOnServiceUpdates;

    private Boolean emailOnAppointments;
    private Boolean smsOnAppointments;

    private Boolean emailOnReminders;
    private Boolean smsOnReminders;

    private Boolean emailOnOffers;
    private Boolean smsOnOffers;

    private Boolean emailOnQuotationRequests;
    private Boolean smsOnQuotationRequests;

    // WebPush preferences (mirrors identity-api entity structure)
    private Boolean webpushOnServiceUpdates;
    private Boolean webpushOnAppointments;
    private Boolean webpushOnReminders;
    private Boolean webpushOnOffers;
    private Boolean webpushOnQuotationRequests;


    private Status status;

}
