# Showroom Catalogue & Quote Requests - Frontend Integration Guide

## Overview

This guide covers the `NEW_CAR_SHOWROOM` catalogue: a showroom publishes **offerings**
(brand / model / trim / indicative price / photos), buyers browse them and **request a quote**.
It follows the same conventions as `NUMBER_PLATE_FRONTEND_GUIDE.md` — reuse
`src/config/api.js` (`API_BASE_URL`, `getHeaders(userId)`).

```
Showroom owner                     Buyer
--------------                     -----
POST   /companies/{id}/catalogue-offerings
PUT    .../catalogue-offerings/{oid}
PUT    .../catalogue-offerings/{oid}/status?value=INACTIVE
                                   GET  /catalogue-offerings/browse   (public, paginated)
                                   GET  /catalogue-offerings/{id}     (public)
                                   POST /catalogue-quote-requests     -> PENDING
PUT  /catalogue-quote-requests/{id}/accept  (quotedPrice, responseMessage)  -> ACCEPTED (contacts shared)
PUT  /catalogue-quote-requests/{id}/reject
                                   PUT  /catalogue-quote-requests/{id}/cancel  (while PENDING)
```

Key rules:

- A company must be a **`NEW_CAR_SHOWROOM`** (see `COMPANY_TYPES_FRONTEND_GUIDE.md`) and the
  caller must be its owner to create/update offerings or respond to quotes.
- Offering `status`: `ACTIVE` (browsable), `INACTIVE` (hidden), `ARCHIVED` (retired).
- Only `ACTIVE` offerings appear in `/browse` and accept new quote requests.
- **Accepting a quote does not deactivate the offering** — a showroom can field several buyers.
  It only sets that request to `ACCEPTED` and shares contact details on that request.
- `buyerContact*` / `showroomContact*` on a quote response are `null` until `status == ACCEPTED`.
- All offering/quote endpoints except the two public GETs require `X-Auto-Adda-Id`.

---

## API Service

```javascript
// src/services/catalogueService.js
import API_BASE_URL, { getHeaders } from '../config/api';

// ============================================
// CATALOGUE OFFERINGS
// ============================================

// Upload photos first via POST /mediafiles/upload, then pass the MediaFile ids as imageIds.
export const createOffering = async (userId, companyId, data) => {
  const response = await fetch(`${API_BASE_URL}/companies/${companyId}/catalogue-offerings`, {
    method: 'POST',
    headers: getHeaders(userId),
    body: JSON.stringify({
      brand: data.brand,
      model: data.model,
      trimName: data.trimName || null,
      modelYear: data.modelYear,
      indicativePrice: data.indicativePrice,
      description: data.description || null,
      imageIds: data.imageIds || []
    })
  });
  return response.json();
};

export const updateOffering = async (userId, companyId, offeringId, data) => {
  const response = await fetch(`${API_BASE_URL}/companies/${companyId}/catalogue-offerings/${offeringId}`, {
    method: 'PUT',
    headers: getHeaders(userId),
    body: JSON.stringify(data)
  });
  return response.json();
};

// value: 'ACTIVE' | 'INACTIVE' | 'ARCHIVED'
export const setOfferingStatus = async (userId, companyId, offeringId, value) => {
  const response = await fetch(
    `${API_BASE_URL}/companies/${companyId}/catalogue-offerings/${offeringId}/status?value=${value}`,
    { method: 'PUT', headers: getHeaders(userId) }
  );
  return response.json();
};

export const getOfferingsForCompany = async (companyId) => {
  const response = await fetch(`${API_BASE_URL}/companies/${companyId}/catalogue-offerings`);
  return response.json();
};

export const getOfferingById = async (offeringId) => {
  const response = await fetch(`${API_BASE_URL}/catalogue-offerings/${offeringId}`);
  return response.json();
};

// Public, paginated. All filters optional.
export const browseOfferings = async ({
  brand, model, minPrice, maxPrice, fromYear, toYear, companyId,
  page = 0, size = 10, sortBy = 'createdAt', sortDirection = 'DESC'
} = {}) => {
  const params = new URLSearchParams();
  if (brand) params.set('brand', brand);
  if (model) params.set('model', model);
  if (minPrice != null) params.set('minPrice', minPrice);
  if (maxPrice != null) params.set('maxPrice', maxPrice);
  if (fromYear != null) params.set('fromYear', fromYear);
  if (toYear != null) params.set('toYear', toYear);
  if (companyId != null) params.set('companyId', companyId);
  params.set('page', page);
  params.set('size', size);
  params.set('sortBy', sortBy);
  params.set('sortDirection', sortDirection);
  const response = await fetch(`${API_BASE_URL}/catalogue-offerings/browse?${params}`);
  return response.json();
};

// ============================================
// QUOTE REQUESTS
// ============================================

export const requestQuote = async (userId, offeringId, message = null) => {
  const response = await fetch(`${API_BASE_URL}/catalogue-quote-requests`, {
    method: 'POST',
    headers: getHeaders(userId),
    body: JSON.stringify({ offeringId, message })
  });
  return response.json();
};

// Showroom accepts: optional quotedPrice + responseMessage
export const acceptQuote = async (userId, quoteId, { quotedPrice, responseMessage } = {}) => {
  const response = await fetch(`${API_BASE_URL}/catalogue-quote-requests/${quoteId}/accept`, {
    method: 'PUT',
    headers: getHeaders(userId),
    body: JSON.stringify({ quotedPrice: quotedPrice ?? null, responseMessage: responseMessage ?? null })
  });
  return response.json();
};

export const rejectQuote = async (userId, quoteId, rejectionReason = null) => {
  const response = await fetch(`${API_BASE_URL}/catalogue-quote-requests/${quoteId}/reject`, {
    method: 'PUT',
    headers: getHeaders(userId),
    body: JSON.stringify({ rejectionReason })
  });
  return response.json();
};

export const cancelQuote = async (userId, quoteId) => {
  const response = await fetch(`${API_BASE_URL}/catalogue-quote-requests/${quoteId}/cancel`, {
    method: 'PUT',
    headers: getHeaders(userId)
  });
  return response.json();
};

export const getQuoteById = async (userId, quoteId) => {
  const response = await fetch(`${API_BASE_URL}/catalogue-quote-requests/${quoteId}`, {
    headers: getHeaders(userId)
  });
  return response.json();
};

export const getMyQuotes = async (userId, status = null) => {
  const query = status ? `?status=${status}` : '';
  const response = await fetch(`${API_BASE_URL}/catalogue-quote-requests/my${query}`, {
    headers: getHeaders(userId)
  });
  return response.json();
};

export const getQuotesForOffering = async (userId, offeringId) => {
  const response = await fetch(`${API_BASE_URL}/catalogue-quote-requests/offering/${offeringId}`, {
    headers: getHeaders(userId)
  });
  return response.json();
};

// Showroom inbox: every quote request across the company's catalogue
export const getQuotesForCompany = async (userId, companyId) => {
  const response = await fetch(`${API_BASE_URL}/catalogue-quote-requests/company/${companyId}`, {
    headers: getHeaders(userId)
  });
  return response.json();
};
```

---

## Status & Type Constants

```javascript
export const CatalogueOfferingStatus = {
  ACTIVE: 'ACTIVE',
  INACTIVE: 'INACTIVE',
  ARCHIVED: 'ARCHIVED'
};

export const CatalogueQuoteRequestStatus = {
  PENDING: 'PENDING',
  ACCEPTED: 'ACCEPTED',
  REJECTED: 'REJECTED',
  CANCELLED: 'CANCELLED'
};
```

---

## Endpoint Reference

### Offerings

| Method | Path | Auth header | Notes |
|---|---|---|---|
| POST | `/companies/{companyId}/catalogue-offerings` | required, owner of a `NEW_CAR_SHOWROOM` | 201 on success |
| PUT | `/companies/{companyId}/catalogue-offerings/{offeringId}` | required, showroom owner | full field update; `status` optional |
| PUT | `/companies/{companyId}/catalogue-offerings/{offeringId}/status?value=` | required, showroom owner | `ACTIVE` / `INACTIVE` / `ARCHIVED` |
| GET | `/companies/{companyId}/catalogue-offerings` | none | that showroom's offerings |
| GET | `/catalogue-offerings/{id}` | none | single offering |
| GET | `/catalogue-offerings/browse?brand=&model=&minPrice=&maxPrice=&fromYear=&toYear=&companyId=&page=&size=&sortBy=&sortDirection=` | none | paginated; `sortBy` ∈ `id,indicativePrice,modelYear,createdAt` |

### Quote requests — `/catalogue-quote-requests`

| Method | Path | Auth header | Notes |
|---|---|---|---|
| POST | `/` | required | buyer only; blocked on own showroom or a duplicate PENDING/ACCEPTED request |
| PUT | `/{id}/accept` | required, showroom owner | body `{ quotedPrice?, responseMessage? }`; reveals contacts; does not deactivate the offering |
| PUT | `/{id}/reject` | required, showroom owner | body `{ rejectionReason? }` |
| PUT | `/{id}/cancel` | required, buyer | only while `PENDING` |
| GET | `/{id}` | required, participant | contact fields only if `ACCEPTED` |
| GET | `/my?status=` | required | buyer's own requests; `status` optional |
| GET | `/offering/{offeringId}` | required, showroom owner | all requests on one offering |
| GET | `/company/{companyId}` | required, showroom owner | inbox across the whole catalogue |

---

## Example: browse response

`GET /catalogue-offerings/browse?brand=BMW&page=0&size=2`
```json
{
  "status": "success",
  "code": "200",
  "message": "Catalogue offerings retrieved successfully",
  "body": {
    "content": [
      {
        "id": 31,
        "companyId": 12,
        "companyName": "AGMC",
        "brand": "BMW",
        "model": "X5",
        "trimName": "xDrive40i M Sport",
        "modelYear": 2026,
        "indicativePrice": 450000.00,
        "description": "In stock, Carbon Black.",
        "images": [{ "id": 90, "filePath": "..." }],
        "status": "ACTIVE",
        "createdAt": "2026-09-08T09:00:00",
        "updatedAt": "2026-09-08T09:00:00"
      }
    ],
    "pageNumber": 0, "pageSize": 2, "totalElements": 1, "totalPages": 1,
    "first": true, "last": true, "empty": false, "numberOfElements": 1
  }
}
```

## Example: quote request before vs. after acceptance

**Before** (`GET /catalogue-quote-requests/100` as the buyer, `status: "PENDING"`):
```json
{
  "id": 100,
  "offeringId": 31,
  "companyId": 12,
  "buyerUserId": 202,
  "status": "PENDING",
  "message": "Best price on this trim?",
  "responseMessage": null,
  "quotedPrice": null,
  "offering": { "id": 31, "brand": "BMW", "model": "X5", "...": "..." },
  "buyerName": "Sara K",
  "buyerContactEmail": null,
  "buyerContactPhone": null,
  "showroomContactEmail": null,
  "showroomContactPhone": null
}
```

**After** the showroom `PUT /catalogue-quote-requests/100/accept` with
`{ "quotedPrice": 440000, "responseMessage": "AED 440,000 all-in, ready for delivery" }`:
```json
{
  "id": 100,
  "status": "ACCEPTED",
  "responseMessage": "AED 440,000 all-in, ready for delivery",
  "quotedPrice": 440000.00,
  "respondedAt": "2026-09-08T11:30:00",
  "buyerContactEmail": "sara@example.com",
  "buyerContactPhone": "0501112222",
  "showroomContactEmail": "sales@agmc.example.com",
  "showroomContactPhone": "0504445555"
}
```

---

## Error Responses

Errors come back with HTTP 200 and `status: "error"` (or 400/401 where Spring maps the
exception). Check `code` / `message`.

| Scenario | code | message |
|---|---|---|
| Offering not found | 404 | Catalogue offering not found |
| Caller is not the company owner | 401 | Only the company owner can perform this action |
| Company is not a showroom | 401 | This company is not a NEW_CAR_SHOWROOM |
| Invalid offering status value | 400 | Invalid catalogue offering status: `<value>` |
| Quote on an inactive offering | 400 | This offering is no longer active |
| Quote on your own showroom | 400 | You cannot request a quote on your own showroom |
| Duplicate active quote request | 400 | You already have an active quote request for this offering |
| Non-showroom responding to a quote | 403 | Only the showroom can respond to this quote request |
| Responding to a resolved quote | 400 | This quote request has already been responded to |
| Non-buyer cancelling | 403 | Only the buyer can cancel this quote request |
| Non-participant reading a quote | 403 | You are not a participant in this quote request |

---

## UI Notes

- **Catalogue management** (showroom owner): list `getOfferingsForCompany`, with add/edit
  forms and an ACTIVE/INACTIVE/ARCHIVED toggle. Photo upload is the same
  `POST /mediafiles/upload` flow used elsewhere.
- **Browse page** (any user): `browseOfferings` with brand/model/price/year filters; render as
  a paginated grid. Each card links to the offering detail page.
- **Offering detail** (any user): specs + photos + `companyName`; a "Request a quote" button
  for signed-in users who don't own the showroom. Disable it if `getMyQuotes` already shows a
  `PENDING`/`ACCEPTED` request on this offering.
- **Showroom inbox**: `getQuotesForCompany` — one list, filter by status; accept opens a small
  form for `quotedPrice` + `responseMessage`.
- **Buyer's "My enquiries"**: `getMyQuotes`; once `ACCEPTED`, surface `showroomContact*` and the
  `quotedPrice` prominently.
