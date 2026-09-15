package com.autoadda.apis.vehicle.enums;

public enum CatalogueQuoteRequestStatus {
    PENDING,    // awaiting the showroom's response
    ACCEPTED,   // showroom responded with a quote; contact details shared
    REJECTED,   // showroom declined
    CANCELLED   // buyer withdrew
}
