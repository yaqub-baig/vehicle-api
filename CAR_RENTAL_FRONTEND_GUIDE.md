# Car Rental Service - Frontend Integration Guide

## Overview

This guide covers integrating the Car Rental Service into the AutoAdda frontend (React/JS). It follows the same conventions as `CARPOOL_FRONTEND_GUIDE.md` and `VEHICLE_BORROWING_FRONTEND_GUIDE.md` — reuse the existing `src/config/api.js` (`API_BASE_URL`, `getHeaders(userId)`) rather than redefining it.

The feature has three linked resources, each with its own endpoint group:

```
CarRentalListing  --(renter requests)-->  RentalRequest  --(owner accepts)-->  RentalCycle
   /car-rental-listings                      /rental-requests                    /rental-cycles
   (the ad)                                  (the ask)                           (the actual rental)
```

All endpoints require the `X-Auto-Adda-Id` header (the caller's AutoAdda user id) except the public read endpoints: `GET /car-rental-listings/{id}`, `GET /car-rental-listings/browse`.

**The one subtle behavior the UI needs to handle:** when a renter sends a request, they choose `rentalType: 'UNLIMITED'` or `'LIMITED'`. For `LIMITED`, `durationValue` is required and the owner should see it before accepting. For `UNLIMITED`, don't send `durationValue` at all (or send `null` — the server clears it either way). This choice also changes what happens at pickup: for `LIMITED`, `expectedReturnAt` in the start-rental response is computed by the server and is **not** something the renter enters; for `UNLIMITED`, the renter may optionally provide `expectedReturnAt` when starting the rental, exactly as before this field existed. See the examples below.

---

## API Service

```javascript
// src/services/carRentalService.js
import API_BASE_URL, { getHeaders } from '../config/api';

const LISTINGS_URL = `${API_BASE_URL}/car-rental-listings`;
const REQUESTS_URL = `${API_BASE_URL}/rental-requests`;
const CYCLES_URL = `${API_BASE_URL}/rental-cycles`;

// ============================================
// LISTINGS (Owner creates/manages, anyone browses)
// ============================================

// Car photos are NOT set on the listing. Each listing response carries the linked
// vehicle's photos, resolved from VehicleDetails:
//   response.body.vehicleImages : MediaFile[]   (all photos of the car)
//   response.body.mainImage     : MediaFile     (the vehicle's primary photo, or null)
// Display URL for a photo = `${FILE_BASE_URL}/uploads/${image.filePath}`.
// To change the photos, edit the vehicle via the vehicle endpoints, not the listing.

// Create a rental listing for one of the owner's vehicles
export const createListing = async (userId, data) => {
  const response = await fetch(LISTINGS_URL, {
    method: 'POST',
    headers: getHeaders(userId),
    body: JSON.stringify({
      vehicleId: data.vehicleId,
      pricePerHour: data.pricePerHour || null,   // optional — omit for daily-only listings
      pricePerDay: data.pricePerDay,
      kmLimitPerDay: data.kmLimitPerDay,
      excessKmPenaltyRate: data.excessKmPenaltyRate,
      lateReturnPenaltyRate: data.lateReturnPenaltyRate,
      location: data.location,                    // required — where the car is picked up
      description: data.description || null
    })
  });
  return response.json();
};

export const updateListing = async (userId, listingId, data) => {
  const response = await fetch(`${LISTINGS_URL}/${listingId}`, {
    method: 'PUT',
    headers: getHeaders(userId),
    body: JSON.stringify(data)
  });
  return response.json();
};

// Hides the listing from browse without losing its history
export const pauseListing = async (userId, listingId) => {
  const response = await fetch(`${LISTINGS_URL}/${listingId}/pause`, {
    method: 'PUT',
    headers: getHeaders(userId)
  });
  return response.json();
};

export const reactivateListing = async (userId, listingId) => {
  const response = await fetch(`${LISTINGS_URL}/${listingId}/reactivate`, {
    method: 'PUT',
    headers: getHeaders(userId)
  });
  return response.json();
};

// Terminal — cannot be reactivated afterward
export const removeListing = async (userId, listingId) => {
  const response = await fetch(`${LISTINGS_URL}/${listingId}/remove`, {
    method: 'PUT',
    headers: getHeaders(userId)
  });
  return response.json();
};

export const getListingById = async (listingId) => {
  const response = await fetch(`${LISTINGS_URL}/${listingId}`);
  return response.json();
};

export const getMyListings = async (userId) => {
  const response = await fetch(`${LISTINGS_URL}/my-listings`, {
    headers: getHeaders(userId)
  });
  return response.json();
};

// Public browse — no auth header needed. All params optional.
export const browseListings = async (criteria = {}) => {
  const params = new URLSearchParams();
  ['brand', 'model', 'location', 'fromPrice', 'toPrice', 'rateType', 'minKmLimitPerDay',
   'page', 'size', 'sortBy', 'sortDirection'].forEach((key) => {
    if (criteria[key] !== undefined && criteria[key] !== null && criteria[key] !== '') {
      params.append(key, criteria[key]);
    }
  });
  const response = await fetch(`${LISTINGS_URL}/browse?${params.toString()}`);
  return response.json();
};

// ============================================
// REQUESTS (Renter sends, Owner accepts/rejects)
// ============================================

// Renter requests to rent a listing.
// rentalType: 'UNLIMITED' | 'LIMITED'. durationValue is required (positive integer)
// only when rentalType === 'LIMITED' — a count of hours (HOURLY) or days (DAILY),
// matching requestedRateType. Leave it undefined/null for UNLIMITED.
export const createRequest = async (userId, data) => {
  const response = await fetch(REQUESTS_URL, {
    method: 'POST',
    headers: getHeaders(userId),
    body: JSON.stringify({
      listingId: data.listingId,
      requestedRateType: data.requestedRateType,        // 'HOURLY' | 'DAILY'
      rentalType: data.rentalType,                       // 'UNLIMITED' | 'LIMITED'
      durationValue: data.rentalType === 'LIMITED' ? data.durationValue : null,
      message: data.message || null
    })
  });
  return response.json();
};

// Owner accepts — reveals both parties' contact info and creates the RentalCycle
export const acceptRequest = async (userId, requestId) => {
  const response = await fetch(`${REQUESTS_URL}/${requestId}/accept`, {
    method: 'PUT',
    headers: getHeaders(userId)
  });
  return response.json();
};

export const rejectRequest = async (userId, requestId, rejectionReason) => {
  const response = await fetch(`${REQUESTS_URL}/${requestId}/reject`, {
    method: 'PUT',
    headers: getHeaders(userId),
    body: JSON.stringify({ rejectionReason: rejectionReason || null })
  });
  return response.json();
};

// Renter withdraws — only while still PENDING
export const cancelRequest = async (userId, requestId) => {
  const response = await fetch(`${REQUESTS_URL}/${requestId}/cancel`, {
    method: 'PUT',
    headers: getHeaders(userId)
  });
  return response.json();
};

export const getRequestById = async (userId, requestId) => {
  const response = await fetch(`${REQUESTS_URL}/${requestId}`, {
    headers: getHeaders(userId)
  });
  return response.json();
};

// status is optional: 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'CANCELLED'
export const getMyRequests = async (userId, status) => {
  const query = status ? `?status=${status}` : '';
  const response = await fetch(`${REQUESTS_URL}/my-requests${query}`, {
    headers: getHeaders(userId)
  });
  return response.json();
};

// Owner's pending requests across all their listings — for a notifications/inbox view
export const getPendingRequestsForOwner = async (userId) => {
  const response = await fetch(`${REQUESTS_URL}/pending`, {
    headers: getHeaders(userId)
  });
  return response.json();
};

// All requests (any status) on one of the owner's listings
export const getRequestsForListing = async (userId, listingId) => {
  const response = await fetch(`${REQUESTS_URL}/listing/${listingId}`, {
    headers: getHeaders(userId)
  });
  return response.json();
};

// ============================================
// CYCLES (the actual rental, once a request is accepted)
// ============================================

// Owner negotiates a flat discount off-platform, sets it any time before return
export const setDiscount = async (userId, cycleId, discountAmount) => {
  const response = await fetch(`${CYCLES_URL}/${cycleId}/discount`, {
    method: 'PUT',
    headers: getHeaders(userId),
    body: JSON.stringify({ discountAmount })
  });
  return response.json();
};

// Renter picks up the vehicle. expectedReturnAt is only meaningful for an
// UNLIMITED cycle — the UI may omit the field entirely for a LIMITED cycle,
// since the server ignores it and computes the return time itself.
export const startCycle = async (userId, cycleId, data) => {
  const response = await fetch(`${CYCLES_URL}/${cycleId}/start`, {
    method: 'PUT',
    headers: getHeaders(userId),
    body: JSON.stringify({
      startOdometer: data.startOdometer,
      expectedReturnAt: data.expectedReturnAt || null
    })
  });
  return response.json();
};

// Renter returns the vehicle — the only input is the end odometer reading.
// The response includes the full price breakdown (see Response Structure below).
export const returnCycle = async (userId, cycleId, endOdometer) => {
  const response = await fetch(`${CYCLES_URL}/${cycleId}/return`, {
    method: 'PUT',
    headers: getHeaders(userId),
    body: JSON.stringify({ endOdometer })
  });
  return response.json();
};

// Either party — only while AWAITING_START (before pickup)
export const cancelCycle = async (userId, cycleId) => {
  const response = await fetch(`${CYCLES_URL}/${cycleId}/cancel`, {
    method: 'PUT',
    headers: getHeaders(userId)
  });
  return response.json();
};

export const getCycleById = async (userId, cycleId) => {
  const response = await fetch(`${CYCLES_URL}/${cycleId}`, {
    headers: getHeaders(userId)
  });
  return response.json();
};

// Look up the cycle created when a specific request was accepted
export const getCycleByRequestId = async (userId, requestId) => {
  const response = await fetch(`${CYCLES_URL}/request/${requestId}`, {
    headers: getHeaders(userId)
  });
  return response.json();
};

export const getMyRentals = async (userId) => {
  const response = await fetch(`${CYCLES_URL}/my-rentals`, {
    headers: getHeaders(userId)
  });
  return response.json();
};

export const getMyVehicleRentals = async (userId) => {
  const response = await fetch(`${CYCLES_URL}/my-vehicles`, {
    headers: getHeaders(userId)
  });
  return response.json();
};
```

---

## Status & Type Constants

```javascript
// src/constants/carRentalStatus.js

export const RENTAL_LISTING_STATUS = { ACTIVE: 'ACTIVE', PAUSED: 'PAUSED', REMOVED: 'REMOVED' };
export const RENTAL_LISTING_STATUS_LABELS = {
  ACTIVE: 'Listed',
  PAUSED: 'Paused',
  REMOVED: 'Removed'
};
export const RENTAL_LISTING_STATUS_COLORS = {
  ACTIVE: '#4CAF50',
  PAUSED: '#FFA500',
  REMOVED: '#9E9E9E'
};

export const RENTAL_REQUEST_STATUS = { PENDING: 'PENDING', ACCEPTED: 'ACCEPTED', REJECTED: 'REJECTED', CANCELLED: 'CANCELLED' };
export const RENTAL_REQUEST_STATUS_LABELS = {
  PENDING: 'Awaiting Owner Response',
  ACCEPTED: 'Accepted',
  REJECTED: 'Declined',
  CANCELLED: 'Withdrawn'
};
export const RENTAL_REQUEST_STATUS_COLORS = {
  PENDING: '#FFA500',
  ACCEPTED: '#4CAF50',
  REJECTED: '#F44336',
  CANCELLED: '#9E9E9E'
};

export const RENTAL_CYCLE_STATUS = { AWAITING_START: 'AWAITING_START', ACTIVE: 'ACTIVE', COMPLETED: 'COMPLETED', CANCELLED: 'CANCELLED' };
export const RENTAL_CYCLE_STATUS_LABELS = {
  AWAITING_START: 'Ready for Pickup',
  ACTIVE: 'Currently Rented',
  COMPLETED: 'Completed',
  CANCELLED: 'Cancelled'
};
export const RENTAL_CYCLE_STATUS_COLORS = {
  AWAITING_START: '#2196F3',
  ACTIVE: '#4CAF50',
  COMPLETED: '#607D8B',
  CANCELLED: '#F44336'
};

export const RENTAL_RATE_TYPE = { HOURLY: 'HOURLY', DAILY: 'DAILY' };

export const RENTAL_TYPE = { UNLIMITED: 'UNLIMITED', LIMITED: 'LIMITED' };
export const RENTAL_TYPE_LABELS = {
  UNLIMITED: 'Open-ended',
  LIMITED: 'Fixed duration'
};
```

---

## Example: Send a LIMITED vs. UNLIMITED request

This is the one subtle behavior the UI needs to handle — the request body shape depends on `rentalType`, and the owner's request-review UI should surface the duration when present.

**LIMITED** — `POST /rental-requests`, header `X-Auto-Adda-Id: 202`
```json
{
  "listingId": 5,
  "requestedRateType": "DAILY",
  "rentalType": "LIMITED",
  "durationValue": 3,
  "message": "Need it for a weekend trip"
}
```

**UNLIMITED** — same endpoint, `durationValue` omitted:
```json
{
  "listingId": 5,
  "requestedRateType": "DAILY",
  "rentalType": "UNLIMITED",
  "message": "Not sure yet how long I'll need it"
}
```

**Response** (both cases return the same shape; `durationValue` is `null` for UNLIMITED):
```json
{
  "status": "success",
  "code": "201",
  "message": "Rental request sent to the owner",
  "body": {
    "id": 12,
    "listingId": 5,
    "vehicleId": 8,
    "renterId": 202,
    "ownerId": 101,
    "requestedRateType": "DAILY",
    "rentalType": "LIMITED",
    "durationValue": 3,
    "message": "Need it for a weekend trip",
    "status": "PENDING",
    "rejectionReason": null,
    "respondedAt": null,
    "createdAt": "2026-08-27T10:00:00",
    "updatedAt": "2026-08-27T10:00:00",
    "listing": { "id": 5, "pricePerDay": 1500, "...": "..." },
    "renterContactEmail": null,
    "renterContactPhone": null,
    "ownerContactEmail": null,
    "ownerContactPhone": null
  }
}
```

If `LIMITED` is sent without a positive `durationValue`, the server responds `400` — "Duration is required for a limited rental".

---

## Example: Accept a request (contact reveal + cycle creation)

Mirrors the carpool feature's connection-acceptance pattern: contact fields are `null` until accepted.

**Request** — `PUT /rental-requests/12/accept`, header `X-Auto-Adda-Id: 101` (must be the listing's owner)

**Response**
```json
{
  "status": "success",
  "code": "200",
  "message": "Request accepted. Contact details have been shared with both parties.",
  "body": {
    "id": 12,
    "status": "ACCEPTED",
    "respondedAt": "2026-08-27T10:05:00",
    "renterContactEmail": "renter@example.com",
    "renterContactPhone": "03001234567",
    "ownerContactEmail": "owner@example.com",
    "ownerContactPhone": "03007654321",
    "...": "..."
  }
}
```

A `RentalCycle` now exists for this request — fetch it with `getCycleByRequestId(userId, 12)` to drive the pickup UI.

---

## Example: Start (pickup) — LIMITED vs. UNLIMITED expectedReturnAt

**LIMITED cycle** — `PUT /rental-cycles/40/start`, body `{ "startOdometer": 15000 }` (no `expectedReturnAt` needed):
```json
{
  "status": "success",
  "code": "200",
  "message": "Rental started. Enjoy your trip!",
  "body": {
    "id": 40,
    "rentalType": "LIMITED",
    "durationValue": 3,
    "rateType": "DAILY",
    "startOdometer": 15000,
    "startedAt": "2026-08-27T10:30:00",
    "expectedReturnAt": "2026-08-30T10:30:00",
    "status": "ACTIVE"
  }
}
```
Note `expectedReturnAt` is `startedAt + 3 days` — computed by the server. If the client sends `expectedReturnAt` in the request body for a LIMITED cycle, it's silently ignored.

**UNLIMITED cycle** — same endpoint, renter may optionally set their own `expectedReturnAt`:
```json
{
  "status": "success",
  "code": "200",
  "body": {
    "id": 41,
    "rentalType": "UNLIMITED",
    "durationValue": null,
    "startOdometer": 22000,
    "startedAt": "2026-08-27T10:30:00",
    "expectedReturnAt": null,
    "status": "ACTIVE"
  }
}
```
If `expectedReturnAt` is left `null` for an UNLIMITED cycle, no late-return penalty can ever apply — show this clearly in the pickup form (e.g. "Skip this and there's no late fee for this rental").

---

## Example: Return — final price breakdown

**Request** — `PUT /rental-cycles/40/return`, body `{ "endOdometer": 15460 }`

**Response**
```json
{
  "status": "success",
  "code": "200",
  "message": "Rental completed successfully",
  "body": {
    "id": 40,
    "status": "COMPLETED",
    "startOdometer": 15000,
    "endOdometer": 15460,
    "startedAt": "2026-08-27T10:30:00",
    "expectedReturnAt": "2026-08-30T10:30:00",
    "actualReturnAt": "2026-08-30T13:30:00",
    "rateAtStart": 1500,
    "kmLimitPerDayAtStart": 200,
    "excessKmPenaltyRateAtStart": 5,
    "lateReturnPenaltyRateAtStart": 100,
    "discountAmount": 200,
    "basePrice": 4500,
    "excessKmPenalty": 300,
    "lateReturnPenalty": 300,
    "finalPrice": 4900
  }
}
```
Render the breakdown as: `basePrice − discountAmount + excessKmPenalty + lateReturnPenalty = finalPrice`. There is no separate owner confirmation step — this response is final the moment the renter submits the odometer reading.

---

## Endpoint Reference

### Listings — `/car-rental-listings`

| Method | Path | Auth header | Notes |
|---|---|---|---|
| POST | `/` | required | owner only, for their own vehicle |
| PUT | `/{id}` | required, must own listing | |
| PUT | `/{id}/pause` | required, must own listing | hides from browse |
| PUT | `/{id}/reactivate` | required, must own listing | fails if another ACTIVE listing exists for the vehicle |
| PUT | `/{id}/remove` | required, must own listing | terminal |
| GET | `/{id}` | none | public read |
| GET | `/my-listings` | required | owner's own listings |
| GET | `/browse?brand=&model=&location=&fromPrice=&toPrice=&rateType=&minKmLimitPerDay=&page=&size=&sortBy=&sortDirection=` | none | public, paginated; `location` is a case-insensitive exact match |

### Requests — `/rental-requests`

| Method | Path | Auth header | Notes |
|---|---|---|---|
| POST | `/` | required | renter only, not on own listing |
| PUT | `/{id}/accept` | required, must be listing owner | reveals contact info, creates the cycle |
| PUT | `/{id}/reject` | required, must be listing owner | optional `rejectionReason` body |
| PUT | `/{id}/cancel` | required, must be renter | only while `PENDING` |
| GET | `/{id}` | required, must be a participant | |
| GET | `/my-requests?status=` | required | `status` optional |
| GET | `/pending` | required | owner's pending requests, all listings |
| GET | `/listing/{listingId}` | required, must own listing | all requests on one listing |

### Cycles — `/rental-cycles`

| Method | Path | Auth header | Notes |
|---|---|---|---|
| PUT | `/{id}/discount` | required, must be owner | any time before return |
| PUT | `/{id}/start` | required, must be renter | only from `AWAITING_START` |
| PUT | `/{id}/return` | required, must be renter | only from `ACTIVE`; computes final price |
| PUT | `/{id}/cancel` | required, renter or owner | only from `AWAITING_START` |
| GET | `/{id}` | required, must be a participant | |
| GET | `/request/{requestId}` | required, must be a participant | |
| GET | `/my-rentals` | required | as renter |
| GET | `/my-vehicles` | required | as owner |

---

## Error Responses

All errors are returned with HTTP 200 and `status: "error"` (or `"failed"` for framework-level validation errors), matching the rest of the API. Check `status` and `code`, not the HTTP status code.

| Scenario | code | message |
|---|---|---|
| Listing not found | 404 | Rental listing not found |
| Creating a listing for a vehicle you don't own | 403 | You can only create a rental listing for your own vehicle |
| Vehicle already has an active listing | 400 | This vehicle already has an active rental listing |
| Price per day missing/non-positive | 400 | Price per day must be positive |
| Location missing/blank | failed | Location is required |
| Requesting your own listing | 400 | Cannot rent your own vehicle |
| Duplicate pending request on the same listing | 400 | You already have a pending request on this listing |
| Vehicle already out on an open rental | 400 | This vehicle is currently rented out |
| `LIMITED` request without a positive duration | 400 | Duration is required for a limited rental |
| Responding to an already-resolved request | 400 | This request has already been responded to |
| Non-owner accepting/rejecting a request | 403 | Only the owner can respond to this request |
| Non-renter cancelling a request | 403 | Only the renter can cancel this request |
| Cancelling a request that's not `PENDING` | 400 | Only pending requests can be cancelled |
| Cycle not found | 404 | Rental cycle not found |
| Non-participant reading a cycle | 403 | You are not a participant in this rental |
| Non-renter starting/returning a cycle | 403 | Only the renter can start/return this rental |
| Starting a cycle that isn't `AWAITING_START` | 400 | Only a rental awaiting pickup can be started |
| Returning a cycle that isn't `ACTIVE` | 400 | Only an active rental can be returned |
| End odometer less than start odometer | 400 | End odometer cannot be less than the start odometer |
| Cancelling a cycle that's already started | 400 | Only a rental that hasn't started can be cancelled |
| Negative discount amount | 400 | Discount amount cannot be negative |
| Setting a discount on a finished rental | 400 | Cannot change the discount on a finished rental |
