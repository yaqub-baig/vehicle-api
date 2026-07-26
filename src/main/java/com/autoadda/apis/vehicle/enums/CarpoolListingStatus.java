package com.autoadda.apis.vehicle.enums;

public enum CarpoolListingStatus {
    ACTIVE,     // visible for matching/browsing
    COMPLETED,  // poster manually marked the trip done
    CANCELLED,  // poster withdrew it
    EXPIRED     // travelDateTime passed while still ACTIVE (scheduler-set)
}
