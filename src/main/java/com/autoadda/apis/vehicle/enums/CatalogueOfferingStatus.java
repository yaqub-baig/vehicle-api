package com.autoadda.apis.vehicle.enums;

public enum CatalogueOfferingStatus {
    ACTIVE,     // visible in the public catalogue and open to quote requests
    INACTIVE,   // hidden from browse; existing quote requests still resolvable
    ARCHIVED    // retired offering, kept for history
}
