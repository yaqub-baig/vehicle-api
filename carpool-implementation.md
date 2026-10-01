# Car Pooling - Implementation Guide

## Overview

This document describes the Car Pooling feature in the AutoAdda Vehicle API. A car owner can post a route, travel time, and price ("offer"), and someone needing a lift can post the same shape with a budget ("seeker"). Each side can browse or see compatible posts from the other side and send a **connect request**. Once accepted, both parties' contact info is revealed via the existing email/notification pipeline so they can coordinate the ride outside the app.

### Use Cases
- **Owner offering a ride**: A car owner driving from City A to City B posts an offer with seats, price per seat, and travel time.
- **Passenger seeking a ride**: A user needing a lift posts a seeker listing with their route, travel time, and budget.
- **Matching**: Either side can view compatible listings on the opposite side (same route, budget-compatible, in the future).
- **Connecting**: Either side can send a connect request to a compatible listing; the recipient accepts or declines.
- **Contact exchange**: Once accepted, both parties' name, email, and phone are shared so they can coordinate pickup.

---

## Design Decisions

- **Single entity with a type discriminator** (`CarpoolListing.type` = `OFFER`/`SEEKER`) instead of two separate entities. An offer and a seeker post are two symmetric instances of the same concept (route + date + budget), not distinct business processes — splitting them would duplicate DAO/mapper/controller code and turn matching into a needless cross-table join.
- **`budget` is overloaded by type**: for an `OFFER` it's the asking price per seat; for a `SEEKER` it's the maximum the rider is willing to pay per seat. Matching requires `seekerBudget >= offerBudget`.
- **No FK to `VehicleDetails`**: an offer's car info (`carDescription`) is a freeform, optional string, not linked to an existing vehicle record.
- **Matching is by territory area** (equal `TerritoryArea` on both origin and destination, for both listings) plus budget compatibility. Origin/destination are a full structured address — `Country -> StateTerritory -> TerritoryArea -> Street` — each level its own admin-manageable lookup table, rather than a free-text city string. A `TerritoryArea`'s FK chain fixes its state/country, so matching on `territoryArea.id` alone is unambiguous (no city-name-collision risk like the old text match had).
- **A listing does not auto-close when a connection is accepted** — an `OFFER` can have multiple seats and legitimately accept multiple `SEEKER` connections. Posters manually mark listings `COMPLETED`/`CANCELLED`; a scheduler auto-`EXPIRED`s listings whose `travelDateTime` has passed.
- **Connect = reveal contact info**, not in-app chat. This reuses the existing `AuthClient` email/push/SMS notification pipeline (see `CarpoolNotificationService`), the same pattern as `BorrowingNotificationService`.
- **Connecting doesn't require posting a listing first.** `POST /carpool-connections` accepts either `requesterListingId` (the original flow) or `requesterListingDetails` (inline route/budget/seats). In the latter case, `CarpoolConnectionServiceImpl.createImplicitRequesterListing` creates a real, ordinary `CarpoolListing` on the caller's behalf — type derived as the opposite of the target's, `travelDateTime` copied from the target — so every downstream rule (compatibility, accept/reject, contact-reveal, expiry) runs unmodified against a real row. It's marked `discoverable = false` so it doesn't clutter `browse`/`matches`/`my-listings`; it only surfaces nested inside the connection response. A fresh listing is created on every such request — never reused or mutated — since budget/route are read live off the listing, and amending one already tied to another pending connection would silently change that connection's terms. See `CARPOOL_DIRECT_REQUEST_GUIDE.md` for the full before/after flow.

---

## Architecture Overview

```
┌──────────────────────────────────────────────────────────────────┐
│                        Car Pooling Flow                          │
├──────────────────────────────────────────────────────────────────┤
│                                                                    │
│  1. Owner posts an OFFER listing (route, seats, price/seat)      │
│     Rider posts a SEEKER listing (route, budget)                 │
│                                                                    │
│  2. Either side calls GET /carpool-listings/{id}/matches         │
│     └─> System finds opposite-type ACTIVE listings with the      │
│         same origin/destination territory area and compatible    │
│         budget                                                    │
│                                                                    │
│  3. Either side sends a connect request                          │
│     POST /carpool-connections                                    │
│     └─> Server re-validates compatibility, creates PENDING       │
│         CarpoolConnection, notifies the recipient                │
│                                                                    │
│  4. Recipient accepts or rejects                                 │
│     PUT /carpool-connections/{id}/accept | /reject                │
│     └─> On accept: both parties' contact info is shared via      │
│         email/push/SMS, and returned in the API response         │
│                                                                    │
│  5. A background sweep expires listings past their travel time   │
│     and any PENDING connections left dangling by an expired      │
│     listing                                                       │
│                                                                    │
└──────────────────────────────────────────────────────────────────┘
```

---

## Database Schema

All tables are created automatically by Hibernate (`spring.jpa.hibernate.ddl-auto=update`). Moving `CarpoolListing` from free-text city columns to the address-lookup hierarchy required a one-time backfill for existing rows — see `migrations/2026-09-28_carpool_structured_address_backfill.sql`.

### 0. Address lookup tables: Country -> StateTerritory -> TerritoryArea -> Street

**Files**: `entity/Country.java`, `entity/StateTerritory.java`, `entity/TerritoryArea.java`, `entity/Street.java`
**Tables**: `country`, `state_territory`, `territory_area`, `street`

Each is a standalone, admin-manageable reference table (`code`, `name`, `isActive` soft-delete flag, timestamps), with `state_territory` FK'd to `country`, `territory_area` FK'd to `state_territory`, and `street` FK'd to `territory_area`. Each has its own full CRUD endpoint set (`controller/CountryController.java`, `StateTerritoryController.java`, `TerritoryAreaController.java`, `StreetController.java`) so the panel can manage them and the client can populate cascading address dropdowns — see the frontend guide's Address Lookups section.

### 1. Entity: CarpoolListing

**File**: `src/main/java/com/autoadda/apis/vehicle/entity/CarpoolListing.java`
**Table**: `carpool_listing`

`originAddress`/`destinationAddress` are `Address` (`entity/Address.java`) `@Embeddable`s — each an FK to `Country`, `StateTerritory`, `TerritoryArea` (all required) and `Street` (optional), plus a `googleMapsUrl` — embedded twice via `@AssociationOverride`/`@AttributeOverride` so each trip end gets its own address without a join table.

| Column | Type | Notes |
|---|---|---|
| id | BIGINT (PK) | |
| user_id | INT | poster's AutoAdda user id |
| type | VARCHAR (enum) | `OFFER` \| `SEEKER` |
| origin_country_id / destination_country_id | BIGINT (FK -> country) | |
| origin_state_territory_id / destination_state_territory_id | BIGINT (FK -> state_territory) | |
| origin_territory_area_id / destination_territory_area_id | BIGINT (FK -> territory_area) | drives matching (see below) |
| origin_street_id / destination_street_id | BIGINT (FK -> street) | optional |
| origin_google_maps_url / destination_google_maps_url | VARCHAR(500) | optional pin link |
| travel_date_time | DATETIME | must be in the future on create/update |
| budget | DECIMAL(10,2) | asking price (OFFER) or max willingness (SEEKER) |
| available_seats | INT | OFFER-only, nullable |
| car_description | VARCHAR(500) | OFFER-only, freeform, nullable |
| status | VARCHAR (enum) | `ACTIVE` \| `COMPLETED` \| `CANCELLED` \| `EXPIRED` |
| created_at / updated_at | DATETIME | |
| is_deleted | BOOLEAN | soft delete |

### 2. Entity: CarpoolConnection

**File**: `src/main/java/com/autoadda/apis/vehicle/entity/CarpoolConnection.java`
**Table**: `carpool_connection`

| Column | Type | Notes |
|---|---|---|
| id | BIGINT (PK) | |
| requester_listing_id | BIGINT | the listing used to initiate the connect |
| requester_user_id | INT | denormalized from the requester listing |
| target_listing_id | BIGINT | the listing being connected to |
| target_user_id | INT | denormalized from the target listing |
| status | VARCHAR (enum) | `PENDING` \| `ACCEPTED` \| `REJECTED` \| `CANCELLED` \| `EXPIRED` |
| message | VARCHAR(500) | optional note from the requester |
| rejection_reason | VARCHAR(500) | optional, set on reject |
| responded_at | DATETIME | |
| created_at / updated_at | DATETIME | |
| is_deleted | BOOLEAN | soft delete |

**Connection state machine**: `PENDING → ACCEPTED` (target accepts) / `PENDING → REJECTED` (target rejects) / `PENDING → CANCELLED` (requester cancels) / `PENDING → EXPIRED` (scheduler, when an underlying listing is no longer active). All four are terminal.

---

## Key Components

| Layer | File |
|---|---|
| Entities | `entity/CarpoolListing.java`, `entity/CarpoolConnection.java`, `entity/Address.java` (embeddable), `entity/Country.java`, `entity/StateTerritory.java`, `entity/TerritoryArea.java`, `entity/Street.java` |
| Enums | `enums/CarpoolListingType.java`, `enums/CarpoolListingStatus.java`, `enums/CarpoolConnectionStatus.java` |
| DAO | `dao/CarpoolListingRepository.java`, `dao/CarpoolConnectionRepository.java`, `dao/CountryRepository.java`, `dao/StateTerritoryRepository.java`, `dao/TerritoryAreaRepository.java`, `dao/StreetRepository.java` |
| DTOs | `request/CarpoolListingRequest.java`, `request/AddressRequest.java`, `request/CountryRequest.java`, `request/StateTerritoryRequest.java`, `request/TerritoryAreaRequest.java`, `request/StreetRequest.java`, `request/CarpoolConnectionRequest.java`, `request/ImplicitCarpoolListingRequest.java`, `request/CarpoolConnectionRejectionRequest.java`, `response/CarpoolListingResponse.java`, `response/AddressResponse.java`, `response/CountryResponse.java`, `response/StateTerritoryResponse.java`, `response/TerritoryAreaResponse.java`, `response/StreetResponse.java`, `response/CarpoolConnectionResponse.java` |
| Mappers | `mapper/CarpoolListingMapper.java`, `mapper/CarpoolConnectionMapper.java`, `mapper/CountryMapper.java`, `mapper/StateTerritoryMapper.java`, `mapper/TerritoryAreaMapper.java`, `mapper/StreetMapper.java` |
| Services | `service/CarpoolListingService.java` + impl, `service/CarpoolConnectionService.java` + impl, `service/impl/CountryService.java`, `service/impl/StateTerritoryService.java`, `service/impl/TerritoryAreaService.java`, `service/impl/StreetService.java` |
| Notifications | `service/impl/CarpoolNotificationService.java` |
| Scheduler | `scheduler/CarpoolListingExpiryScheduler.java` (runs every `vehicle.carpool.expiry.interval-ms`, default 30 min) |
| Controllers | `controller/CarpoolListingController.java`, `controller/CarpoolConnectionController.java`, `controller/CountryController.java`, `controller/StateTerritoryController.java`, `controller/TerritoryAreaController.java`, `controller/StreetController.java` |

See `CARPOOL_FRONTEND_GUIDE.md` for the full REST API reference with request/response examples.
