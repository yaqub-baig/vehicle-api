# Number Plate For Sale - Frontend Integration Guide

## Overview

This guide covers integrating the Number Plate For Sale feature into the AutoAdda frontend (React/JS). It follows the same conventions as `CARPOOL_FRONTEND_GUIDE.md` — reuse the existing `src/config/api.js` (`API_BASE_URL`, `getHeaders(userId)`) rather than redefining it.

All endpoints require the `X-Auto-Adda-Id` header (the caller's AutoAdda user id) except the public read endpoints (`GET /number-plate-listings/{id}`, `GET /number-plate-listings/browse`).

A seller uploads a plate listing (plate number, emirate, category, expected price, description, optional photos). Any other user can browse listings and express interest. The seller decides per-interest whether to accept or reject it; **accepting one interest does not close the listing or affect any other interest on it** — the seller closes the listing manually (mark sold / cancel) whenever they're done, independent of how many interests they've accepted. Buyer/seller contact details are only ever included in an interest response once that specific interest is `ACCEPTED`.

---

## API Service

```javascript
// src/services/numberPlateService.js
import API_BASE_URL, { getHeaders } from '../config/api';

const LISTINGS_URL = `${API_BASE_URL}/number-plate-listings`;
const INTERESTS_URL = `${API_BASE_URL}/number-plate-interests`;

// ============================================
// LISTINGS
// ============================================

// Step 1: upload plate photo(s) first (reuses the existing media endpoint), then
// pass the returned MediaFile id(s) as imageIds when creating/updating a listing.
export const uploadPlateImages = async (userId, files) => {
  const formData = new FormData();
  files.forEach((file) => formData.append('files', file));
  const response = await fetch(`${API_BASE_URL}/mediafiles/upload`, {
    method: 'POST',
    headers: { 'X-Auto-Adda-Id': userId }, // no Content-Type - browser sets multipart boundary
    body: formData
  });
  return response.json(); // body: [{ id, fileName, ... }, ...]
};

export const createListing = async (userId, data) => {
  const response = await fetch(LISTINGS_URL, {
    method: 'POST',
    headers: getHeaders(userId),
    body: JSON.stringify({
      plateNumber: data.plateNumber,
      emirate: data.emirate,
      category: data.category,
      expectedPrice: data.expectedPrice,
      description: data.description || null,
      imageIds: data.imageIds || []   // optional - [] or omit if no photos
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

export const markListingSold = async (userId, listingId) => {
  const response = await fetch(`${LISTINGS_URL}/${listingId}/sold`, {
    method: 'PUT',
    headers: getHeaders(userId)
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

// Public browse, no auth header required. All filters optional.
export const browseListings = async ({ emirate, category, minPrice, maxPrice } = {}) => {
  const params = new URLSearchParams();
  if (emirate) params.set('emirate', emirate);
  if (category) params.set('category', category);
  if (minPrice != null) params.set('minPrice', minPrice);
  if (maxPrice != null) params.set('maxPrice', maxPrice);
  const query = params.toString() ? `?${params}` : '';
  const response = await fetch(`${LISTINGS_URL}/browse${query}`);
  return response.json();
};

// ============================================
// INTERESTS
// ============================================

export const expressInterest = async (userId, listingId, message = null) => {
  const response = await fetch(INTERESTS_URL, {
    method: 'POST',
    headers: getHeaders(userId),
    body: JSON.stringify({ listingId, message })
  });
  return response.json();
};

export const acceptInterest = async (userId, interestId) => {
  const response = await fetch(`${INTERESTS_URL}/${interestId}/accept`, {
    method: 'PUT',
    headers: getHeaders(userId)
  });
  return response.json();
};

export const rejectInterest = async (userId, interestId, rejectionReason = null) => {
  const response = await fetch(`${INTERESTS_URL}/${interestId}/reject`, {
    method: 'PUT',
    headers: getHeaders(userId),
    body: JSON.stringify({ rejectionReason })
  });
  return response.json();
};

export const cancelInterest = async (userId, interestId) => {
  const response = await fetch(`${INTERESTS_URL}/${interestId}/cancel`, {
    method: 'PUT',
    headers: getHeaders(userId)
  });
  return response.json();
};

export const getInterestById = async (userId, interestId) => {
  const response = await fetch(`${INTERESTS_URL}/${interestId}`, {
    headers: getHeaders(userId)
  });
  return response.json();
};

export const getMyInterests = async (userId, status = null) => {
  const query = status ? `?status=${status}` : '';
  const response = await fetch(`${INTERESTS_URL}/my-interests${query}`, {
    headers: getHeaders(userId)
  });
  return response.json();
};

// Seller: all interest expressed on one of their listings
export const getInterestsForListing = async (userId, listingId) => {
  const response = await fetch(`${INTERESTS_URL}/listing/${listingId}`, {
    headers: getHeaders(userId)
  });
  return response.json();
};
```

---

## Endpoint Reference

### Listings — `/number-plate-listings`

| Method | Path | Auth header | Notes |
|---|---|---|---|
| POST | `/` | required | `imageIds` optional |
| PUT | `/{id}` | required, must own listing | only while `ACTIVE` |
| PUT | `/{id}/sold` | required, must own listing | only while `ACTIVE`; manual action, never automatic |
| PUT | `/{id}/cancel` | required, must own listing | only while `ACTIVE` |
| GET | `/{id}` | none | public read |
| GET | `/my-listings` | required | seller's own listings |
| GET | `/browse?emirate=&category=&minPrice=&maxPrice=` | none | public browse, all filters optional |

### Interests — `/number-plate-interests`

| Method | Path | Auth header | Notes |
|---|---|---|---|
| POST | `/` | required | buyer only; blocked on own listing or duplicate pending interest |
| PUT | `/{id}/accept` | required, must be seller | reveals contact info; does **not** close the listing or touch other interests |
| PUT | `/{id}/reject` | required, must be seller | optional `rejectionReason` body |
| PUT | `/{id}/cancel` | required, must be buyer | only while `PENDING` |
| GET | `/{id}` | required, must be a participant | contact fields only if `ACCEPTED` |
| GET | `/my-interests?status=` | required | as buyer or seller; `status` optional |
| GET | `/listing/{listingId}` | required, must own listing | all interest received on one listing |

---

## Example: Create a listing

**Request** — `POST /number-plate-listings`, header `X-Auto-Adda-Id: 101`
```json
{
  "plateNumber": "A 12345",
  "emirate": "Dubai",
  "category": "Private",
  "expectedPrice": 25000,
  "description": "Single digit... just kidding, clean 5-digit plate, no accidents.",
  "imageIds": [88]
}
```

**Response**
```json
{
  "status": "success",
  "code": "201",
  "message": "Number plate listing created successfully",
  "body": {
    "id": 1,
    "userId": 101,
    "sellerName": "Ali Raza",
    "plateNumber": "A 12345",
    "emirate": "Dubai",
    "category": "Private",
    "expectedPrice": 25000,
    "description": "Single digit... just kidding, clean 5-digit plate, no accidents.",
    "images": [{ "id": 88, "fileName": "...", "filePath": "..." }],
    "status": "ACTIVE",
    "createdAt": "2026-08-22T10:00:00",
    "updatedAt": "2026-08-22T10:00:00"
  }
}
```

## Example: Interest response before vs. after acceptance

This is the one subtle behavior the UI needs to handle: `buyerContactEmail/Phone` and `sellerContactEmail/Phone` are `null` until the interest is `ACCEPTED`.

**Before acceptance** (`status: "PENDING"`):
```json
{
  "id": 5,
  "listingId": 1,
  "buyerUserId": 202,
  "sellerUserId": 101,
  "status": "PENDING",
  "message": "Is the price negotiable?",
  "listing": { "id": 1, "plateNumber": "A 12345", "...": "..." },
  "buyerContactEmail": null,
  "buyerContactPhone": null,
  "sellerContactEmail": null,
  "sellerContactPhone": null
}
```

**After acceptance** (`status: "ACCEPTED"`):
```json
{
  "id": 5,
  "listingId": 1,
  "buyerUserId": 202,
  "sellerUserId": 101,
  "status": "ACCEPTED",
  "respondedAt": "2026-08-22T11:00:00",
  "listing": { "id": 1, "plateNumber": "A 12345", "status": "ACTIVE", "...": "..." },
  "buyerContactEmail": "buyer@example.com",
  "buyerContactPhone": "0501234567",
  "sellerContactEmail": "ali.raza@example.com",
  "sellerContactPhone": "0507654321"
}
```

Note the listing's own `status` in the embedded snapshot stays `ACTIVE` here — accepting an interest is independent of closing the listing. Show a "Mark as sold" action on the seller's listing detail page once they're done, rather than assuming it happens automatically on accept.

---

## Error Responses

All errors are returned with HTTP 200 and `status: "error"` (or `"failed"` for framework-level validation errors), matching the rest of the API. Check `status` and `code`, not the HTTP status code.

| Scenario | code | message |
|---|---|---|
| Listing not found | 404 | Number plate listing not found |
| Editing someone else's listing | 403 | You are not authorized to edit this listing |
| Editing a non-active listing | 400 | Only active listings can be edited |
| Expressing interest on your own listing | 400 | You cannot express interest in your own listing |
| Expressing interest on a non-active listing | 400 | This listing is no longer active |
| Duplicate pending/accepted interest | 400 | You already have an active interest in this listing |
| Responding to a resolved interest | 400 | This interest has already been responded to |
| Non-participant reading an interest | 403 | You are not a participant in this interest |
| Non-owner viewing a listing's interests | 403 | You can only view interest on your own listing |

---

## UI Notes

- **Browse page**: call `browseListings()` with whichever filters the user picks; all are optional, so an empty call returns every `ACTIVE` listing.
- **Listing detail page** (any user): show plate details + photos + `sellerName`. Never show seller contact info here — only via an `ACCEPTED` interest.
- **"I'm interested" button**: only show it to users who are not the listing owner (`listing.userId !== currentUserId`) and hide/disable it if `getMyInterests` already shows a `PENDING`/`ACCEPTED` interest from this user on that listing.
- **Seller's listing management page**: show `getInterestsForListing` so the seller can review and accept/reject each interest independently — there is no "one winner" constraint enforced by the backend, so the UI can let the seller accept more than one if they want to compare before manually marking the listing sold.
- **My interests page** (buyer): use `getMyInterests` to show sent interests and their status; once `ACCEPTED`, surface the seller's contact info prominently since that's the whole point of the acceptance.
