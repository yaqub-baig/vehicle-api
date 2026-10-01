# Car Pooling - Frontend Integration Guide

## Overview

This guide covers integrating the Car Pooling feature into the AutoAdda frontend (React/JS). It follows the same conventions as `VEHICLE_BORROWING_FRONTEND_GUIDE.md` — reuse the existing `src/config/api.js` (`API_BASE_URL`, `getHeaders(userId)`) rather than redefining it.

All endpoints require the `X-Auto-Adda-Id` header (the caller's AutoAdda user id) except the public read endpoints (`GET /carpool-listings/{id}`, `GET /carpool-listings/browse`) and the address lookup endpoints (`/countries`, `/state-territories`, `/territory-areas`, `/streets`).

A carpool listing's origin/destination is now a structured address (`country -> state/territory -> territory area -> street`), not a free-text city string — see [Address Lookups](#address-lookups) below for how to populate cascading dropdowns and resolve the ids a listing needs.

`POST /carpool-connections` can now be called **without** posting your own listing first — see `CARPOOL_DIRECT_REQUEST_GUIDE.md` for the old vs. new flow, when to use which, and worked examples from both the rider's and driver's perspective.

---

## API Service

```javascript
// src/services/carpoolService.js
import API_BASE_URL, { getHeaders } from '../config/api';

const LISTINGS_URL = `${API_BASE_URL}/carpool-listings`;
const CONNECTIONS_URL = `${API_BASE_URL}/carpool-connections`;
const COUNTRIES_URL = `${API_BASE_URL}/countries`;
const STATE_TERRITORIES_URL = `${API_BASE_URL}/state-territories`;
const TERRITORY_AREAS_URL = `${API_BASE_URL}/territory-areas`;
const STREETS_URL = `${API_BASE_URL}/streets`;

// ============================================
// ADDRESS LOOKUPS (cascading dropdowns)
// ============================================

export const getCountries = async () => (await fetch(COUNTRIES_URL)).json();

export const getStateTerritories = async (countryId) => {
  const params = new URLSearchParams({ countryId });
  return (await fetch(`${STATE_TERRITORIES_URL}?${params}`)).json();
};

export const getTerritoryAreas = async (stateTerritoryId) => {
  const params = new URLSearchParams({ stateTerritoryId });
  return (await fetch(`${TERRITORY_AREAS_URL}?${params}`)).json();
};

export const getStreets = async (territoryAreaId) => {
  const params = new URLSearchParams({ territoryAreaId });
  return (await fetch(`${STREETS_URL}?${params}`)).json();
};

// ============================================
// LISTINGS
// ============================================

// Create a listing (type: 'OFFER' | 'SEEKER')
// originAddress/destinationAddress: { countryId, stateTerritoryId, territoryAreaId, streetId?, googleMapsUrl? }
export const createListing = async (userId, data) => {
  const response = await fetch(LISTINGS_URL, {
    method: 'POST',
    headers: getHeaders(userId),
    body: JSON.stringify({
      type: data.type,
      originAddress: data.originAddress,
      destinationAddress: data.destinationAddress,
      travelDateTime: data.travelDateTime,
      budget: data.budget,
      availableSeats: data.availableSeats || null,  // OFFER only
      carDescription: data.carDescription || null    // OFFER only
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

export const cancelListing = async (userId, listingId) => {
  const response = await fetch(`${LISTINGS_URL}/${listingId}/cancel`, {
    method: 'PUT',
    headers: getHeaders(userId)
  });
  return response.json();
};

export const completeListing = async (userId, listingId) => {
  const response = await fetch(`${LISTINGS_URL}/${listingId}/complete`, {
    method: 'PUT',
    headers: getHeaders(userId)
  });
  return response.json();
};

export const getListingById = async (listingId) => {
  const response = await fetch(`${LISTINGS_URL}/${listingId}`);
  return response.json();
};

export const getMyListings = async (userId, type = null) => {
  const query = type ? `?type=${type}` : '';
  const response = await fetch(`${LISTINGS_URL}/my-listings${query}`, {
    headers: getHeaders(userId)
  });
  return response.json();
};

// Compatible opposite-type listings for one of my own listings
export const getMatches = async (userId, listingId) => {
  const response = await fetch(`${LISTINGS_URL}/${listingId}/matches`, {
    headers: getHeaders(userId)
  });
  return response.json();
};

// Public browse, no auth header required
export const browseListings = async (type, originTerritoryAreaId, destinationTerritoryAreaId) => {
  const params = new URLSearchParams({ type, originTerritoryAreaId, destinationTerritoryAreaId });
  const response = await fetch(`${LISTINGS_URL}/browse?${params}`);
  return response.json();
};

// ============================================
// CONNECTIONS
// ============================================

// Connect using a listing you've already posted
export const connectFromListing = async (userId, requesterListingId, targetListingId, message = null) => {
  const response = await fetch(CONNECTIONS_URL, {
    method: 'POST',
    headers: getHeaders(userId),
    body: JSON.stringify({ requesterListingId, targetListingId, message })
  });
  return response.json();
};

// Connect without posting your own listing first - the server creates a hidden
// listing behind the scenes. requesterListingDetails: { originAddress, destinationAddress,
// budget, availableSeats?, carDescription? } - see CARPOOL_DIRECT_REQUEST_GUIDE.md
export const connectDirect = async (userId, targetListingId, requesterListingDetails, message = null) => {
  const response = await fetch(CONNECTIONS_URL, {
    method: 'POST',
    headers: getHeaders(userId),
    body: JSON.stringify({ targetListingId, requesterListingDetails, message })
  });
  return response.json();
};

export const acceptConnection = async (userId, connectionId) => {
  const response = await fetch(`${CONNECTIONS_URL}/${connectionId}/accept`, {
    method: 'PUT',
    headers: getHeaders(userId)
  });
  return response.json();
};

export const rejectConnection = async (userId, connectionId, rejectionReason = null) => {
  const response = await fetch(`${CONNECTIONS_URL}/${connectionId}/reject`, {
    method: 'PUT',
    headers: getHeaders(userId),
    body: JSON.stringify({ rejectionReason })
  });
  return response.json();
};

export const cancelConnection = async (userId, connectionId) => {
  const response = await fetch(`${CONNECTIONS_URL}/${connectionId}/cancel`, {
    method: 'PUT',
    headers: getHeaders(userId)
  });
  return response.json();
};

export const getConnectionById = async (userId, connectionId) => {
  const response = await fetch(`${CONNECTIONS_URL}/${connectionId}`, {
    headers: getHeaders(userId)
  });
  return response.json();
};

export const getMyConnections = async (userId, status = null) => {
  const query = status ? `?status=${status}` : '';
  const response = await fetch(`${CONNECTIONS_URL}/my-connections${query}`, {
    headers: getHeaders(userId)
  });
  return response.json();
};
```

---

## Endpoint Reference

### Address Lookups

Used to populate cascading dropdowns when a user is entering a pickup/drop address, and to resolve the ids a listing's `originAddress`/`destinationAddress` needs. All open, no auth header, all support the standard CRUD verbs (`POST`, `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}` — soft delete) for admin-panel management; only the cascading `GET` list is shown here.

| Method | Path | Notes |
|---|---|---|
| GET | `/countries` | all active countries |
| GET | `/state-territories?countryId=` | states/territories under a country |
| GET | `/territory-areas?stateTerritoryId=` | areas under a state/territory |
| GET | `/streets?territoryAreaId=` | streets under a territory area |

### Listings — `/carpool-listings`

| Method | Path | Auth header | Notes |
|---|---|---|---|
| POST | `/` | required | create OFFER or SEEKER listing |
| PUT | `/{id}` | required, must own listing | only while `ACTIVE` |
| PUT | `/{id}/cancel` | required, must own listing | only while `ACTIVE` |
| PUT | `/{id}/complete` | required, must own listing | only while `ACTIVE` |
| GET | `/{id}` | none | public read |
| GET | `/my-listings?type=` | required | `type` optional |
| GET | `/{id}/matches` | required, must own listing | compatible opposite-type listings (same territory area both ends) |
| GET | `/browse?type=&originTerritoryAreaId=&destinationTerritoryAreaId=` | none | public browse |

### Connections — `/carpool-connections`

| Method | Path | Auth header | Notes |
|---|---|---|---|
| POST | `/` | required | `requesterListingId` **or** `requesterListingDetails`, not both — see `CARPOOL_DIRECT_REQUEST_GUIDE.md` |
| PUT | `/{id}/accept` | required, must be target | reveals contact info |
| PUT | `/{id}/reject` | required, must be target | optional `rejectionReason` body |
| PUT | `/{id}/cancel` | required, must be requester | only while `PENDING` |
| GET | `/{id}` | required, must be a participant | contact fields only if `ACCEPTED` |
| GET | `/my-connections?status=` | required | `status` optional |

---

## Example: Create an offer listing

**Request** — `POST /carpool-listings`, header `X-Auto-Adda-Id: 101`
```json
{
  "type": "OFFER",
  "originAddress": {
    "countryId": 1,
    "stateTerritoryId": 2,
    "territoryAreaId": 15,
    "streetId": 102,
    "googleMapsUrl": "https://maps.google.com/?q=25.1122,55.2003"
  },
  "destinationAddress": {
    "countryId": 1,
    "stateTerritoryId": 2,
    "territoryAreaId": 22,
    "googleMapsUrl": "https://maps.google.com/?q=25.0772,55.1409"
  },
  "travelDateTime": "2026-08-01T08:00:00",
  "budget": 1500,
  "availableSeats": 3,
  "carDescription": "Silver Honda City"
}
```

**Response**
```json
{
  "status": "success",
  "code": "201",
  "message": "Carpool listing created successfully",
  "body": {
    "id": 1,
    "userId": 101,
    "posterName": "Ali Raza",
    "type": "OFFER",
    "originAddress": {
      "country": { "id": 1, "code": "UAE", "name": "United Arab Emirates", "isActive": true },
      "stateTerritory": { "id": 2, "code": "DUBAI", "name": "Dubai", "isActive": true, "country": { "id": 1, "code": "UAE", "name": "United Arab Emirates", "isActive": true } },
      "territoryArea": { "id": 15, "code": "AL_BARSHA", "name": "Al Barsha", "isActive": true, "stateTerritory": { "...": "..." } },
      "street": { "id": 102, "code": "12A", "name": null, "isActive": true, "territoryArea": { "...": "..." } },
      "googleMapsUrl": "https://maps.google.com/?q=25.1122,55.2003"
    },
    "destinationAddress": {
      "country": { "id": 1, "code": "UAE", "name": "United Arab Emirates", "isActive": true },
      "stateTerritory": { "id": 2, "code": "DUBAI", "name": "Dubai", "isActive": true, "country": { "...": "..." } },
      "territoryArea": { "id": 22, "code": "DEIRA", "name": "Deira", "isActive": true, "stateTerritory": { "...": "..." } },
      "street": null,
      "googleMapsUrl": "https://maps.google.com/?q=25.0772,55.1409"
    },
    "travelDateTime": "2026-08-01T08:00:00",
    "budget": 1500,
    "availableSeats": 3,
    "carDescription": "Silver Honda City",
    "status": "ACTIVE",
    "createdAt": "2026-07-25T10:00:00",
    "updatedAt": "2026-07-25T10:00:00"
  }
}
```

## Example: Connection response before vs. after acceptance

This is the one subtle behavior the UI needs to handle: `requesterContactEmail/Phone` and `targetContactEmail/Phone` are `null` until the connection is `ACCEPTED`.

**Before acceptance** (`status: "PENDING"`):
```json
{
  "id": 5,
  "status": "PENDING",
  "requesterListing": { "id": 2, "type": "SEEKER", "...": "..." },
  "targetListing": { "id": 1, "type": "OFFER", "...": "..." },
  "requesterContactEmail": null,
  "requesterContactPhone": null,
  "targetContactEmail": null,
  "targetContactPhone": null
}
```

**After acceptance** (`status: "ACCEPTED"`):
```json
{
  "id": 5,
  "status": "ACCEPTED",
  "respondedAt": "2026-07-25T11:00:00",
  "requesterListing": { "id": 2, "type": "SEEKER", "...": "..." },
  "targetListing": { "id": 1, "type": "OFFER", "...": "..." },
  "requesterContactEmail": "rider@example.com",
  "requesterContactPhone": "03001234567",
  "targetContactEmail": "ali.raza@example.com",
  "targetContactPhone": "03007654321"
}
```

---

## Error Responses

All errors are returned with HTTP 200 and `status: "error"` (or `"failed"` for framework-level validation errors), matching the rest of the API. Check `status` and `code`, not the HTTP status code.

| Scenario | code | message |
|---|---|---|
| Listing not found | 404 | Carpool listing not found |
| Editing someone else's listing | 403 | You are not authorized to edit this listing |
| Editing a non-active listing | 400 | Only active listings can be edited |
| Travel time in the past | 400 | Travel date/time must be in the future |
| Unknown country/state/area/street id in an address | 404 | Country not found / State/territory not found / Territory area not found / Street not found |
| OFFER without seats | 400 | Available seats are required for an offer listing |
| Connecting to your own listing | 400 | Cannot connect to your own listing |
| Connecting same-type listings | 400 | Can only connect to the opposite listing type |
| Incompatible route/budget | 400 | These listings are not compatible for connecting |
| Duplicate connect request (one PENDING/ACCEPTED request per user per target, regardless of which listing) | 400 | A connection request already exists between these listings |
| Both or neither of requesterListingId/requesterListingDetails given | 400 | Provide exactly one of requesterListingId or requesterListingDetails |
| Responding to a resolved request | 400 | This connection request has already been responded to |
| Non-participant reading a connection | 403 | You are not a participant in this connection request |
