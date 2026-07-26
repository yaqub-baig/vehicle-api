# Car Pooling - Frontend Integration Guide

## Overview

This guide covers integrating the Car Pooling feature into the AutoAdda frontend (React/JS). It follows the same conventions as `VEHICLE_BORROWING_FRONTEND_GUIDE.md` — reuse the existing `src/config/api.js` (`API_BASE_URL`, `getHeaders(userId)`) rather than redefining it.

All endpoints require the `X-Auto-Adda-Id` header (the caller's AutoAdda user id) except the public read endpoints (`GET /carpool-listings/{id}`, `GET /carpool-listings/browse`).

---

## API Service

```javascript
// src/services/carpoolService.js
import API_BASE_URL, { getHeaders } from '../config/api';

const LISTINGS_URL = `${API_BASE_URL}/carpool-listings`;
const CONNECTIONS_URL = `${API_BASE_URL}/carpool-connections`;

// ============================================
// LISTINGS
// ============================================

// Create a listing (type: 'OFFER' | 'SEEKER')
export const createListing = async (userId, data) => {
  const response = await fetch(LISTINGS_URL, {
    method: 'POST',
    headers: getHeaders(userId),
    body: JSON.stringify({
      type: data.type,
      originCity: data.originCity,
      destinationCity: data.destinationCity,
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
export const browseListings = async (type, origin, destination) => {
  const params = new URLSearchParams({ type, origin, destination });
  const response = await fetch(`${LISTINGS_URL}/browse?${params}`);
  return response.json();
};

// ============================================
// CONNECTIONS
// ============================================

export const connect = async (userId, requesterListingId, targetListingId, message = null) => {
  const response = await fetch(CONNECTIONS_URL, {
    method: 'POST',
    headers: getHeaders(userId),
    body: JSON.stringify({ requesterListingId, targetListingId, message })
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

### Listings — `/carpool-listings`

| Method | Path | Auth header | Notes |
|---|---|---|---|
| POST | `/` | required | create OFFER or SEEKER listing |
| PUT | `/{id}` | required, must own listing | only while `ACTIVE` |
| PUT | `/{id}/cancel` | required, must own listing | only while `ACTIVE` |
| PUT | `/{id}/complete` | required, must own listing | only while `ACTIVE` |
| GET | `/{id}` | none | public read |
| GET | `/my-listings?type=` | required | `type` optional |
| GET | `/{id}/matches` | required, must own listing | compatible opposite-type listings |
| GET | `/browse?type=&origin=&destination=` | none | public browse |

### Connections — `/carpool-connections`

| Method | Path | Auth header | Notes |
|---|---|---|---|
| POST | `/` | required | requester must own `requesterListingId` |
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
  "originCity": "Lahore",
  "destinationCity": "Islamabad",
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
    "originCity": "Lahore",
    "destinationCity": "Islamabad",
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
| OFFER without seats | 400 | Available seats are required for an offer listing |
| Connecting to your own listing | 400 | Cannot connect to your own listing |
| Connecting same-type listings | 400 | Can only connect to the opposite listing type |
| Incompatible route/budget | 400 | These listings are not compatible for connecting |
| Duplicate connect request | 400 | A connection request already exists between these listings |
| Responding to a resolved request | 400 | This connection request has already been responded to |
| Non-participant reading a connection | 403 | You are not a participant in this connection request |
