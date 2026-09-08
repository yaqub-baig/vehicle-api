# Company Types, Showroom Catalogue & Vehicle Sale - Frontend Integration Guide

## Overview

This is the umbrella guide for the company-type feature set. It covers three connected areas:

1. **Company types** — a company is now one or more of `USED_CAR_DEALER`, `NEW_CAR_SHOWROOM`,
   `FLEET_OWNER`, each with its own extra profile data.
2. **Showroom catalogue + quote requests** — a `NEW_CAR_SHOWROOM` publishes brand/model/trim
   offerings; buyers browse and request a quote; the showroom accepts (sharing contact
   details) or rejects.
3. **Vehicle sale completion** — a `USED_CAR_DEALER` (or a private owner) closes a
   `/vehicle-listing` into a recorded `VehicleSale` + invoice, which marks the listing SOLD
   and transfers vehicle ownership.

It follows the same conventions as the other guides (`CAR_RENTAL_FRONTEND_GUIDE.md`,
`NUMBER_PLATE_FRONTEND_GUIDE.md`): reuse `src/config/api.js` for `API_BASE_URL` and
`getHeaders(userId)` (which sets `Content-Type: application/json` + `X-Auto-Adda-Id`).
Every response is the standard envelope `{ status, code, message, body }` — check `code`,
not the HTTP status.

Deep-dive companions:
- `COMPANY_TYPES_FRONTEND_GUIDE.md` — company create/update field reference
- `SHOWROOM_CATALOGUE_FRONTEND_GUIDE.md` — catalogue + quote lifecycle detail
- `VEHICLE_SALE_FRONTEND_GUIDE.md` — sale completion detail

### How the pieces connect

```
                            ┌──────────────────────────────┐
                            │  Company (types: [...])       │
                            │  owner = companyOwnerId       │
                            └───────┬───────────┬───────────┘
             NEW_CAR_SHOWROOM       │           │   USED_CAR_DEALER
                                    │           │
             ┌──────────────────────┘           └───────────────────────┐
             ▼                                                          ▼
   CatalogueOffering (brand/model/trim)                    VehicleListing (existing /vehicle-listing)
             │  browse (public, paginated)                             │  buyer finds a car
             ▼                                                          ▼
   CatalogueQuoteRequest  PENDING                          POST /vehicle-sales  (owner or dealer)
     ├─ showroom accept -> ACCEPTED (contacts shared, quotedPrice)     │
     ├─ showroom reject -> REJECTED                                     ▼
     └─ buyer cancel    -> CANCELLED                          VehicleSale + VehicleSaleInvoice
                                                              listing -> SOLD
                                                              vehicle.isForSale -> false
                                                              ownership: customerId -> buyer
```

### Auth summary

| Area | Header required? |
|---|---|
| `POST /company`, `PUT /company/{id}` | **No** (unchanged; legacy endpoints) |
| `GET /catalogue-offerings/{id}`, `GET /catalogue-offerings/browse`, `GET /companies/{id}/catalogue-offerings` | No (public reads) |
| All other catalogue-offering ops | Yes — must be the company owner **and** the company must be a `NEW_CAR_SHOWROOM` |
| All `/catalogue-quote-requests/*` | Yes |
| All `/vehicle-sales/*` | Yes |

---

## API Service

```javascript
// src/services/companyFeaturesService.js
import API_BASE_URL, { getHeaders } from '../config/api';

// ==========================================================================
// 1. COMPANY TYPES  (/company)
// ==========================================================================

// types: array of "USED_CAR_DEALER" | "NEW_CAR_SHOWROOM" | "FLEET_OWNER"
// showroomAddress / rtaApprovalNumber / authorizedBrands are REQUIRED when
// types includes "NEW_CAR_SHOWROOM".
export const createCompany = async (data) => {
  const response = await fetch(`${API_BASE_URL}/company`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      companyOwnerId: data.companyOwnerId,
      name: data.name,
      tradeLicenseNumber: data.tradeLicenseNumber,
      tradeLicenseExpiry: data.tradeLicenseExpiry || null,
      profileImageId: data.profileImageId || null,
      types: data.types || null,
      showroomAddress: data.showroomAddress || null,
      rtaApprovalNumber: data.rtaApprovalNumber || null,
      authorizedBrands: data.authorizedBrands || null
    })
  });
  return response.json();
};

// Partial update. Send `types` only to REPLACE the whole set (rejected if []).
export const updateCompany = async (companyId, data) => {
  const response = await fetch(`${API_BASE_URL}/company/${companyId}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  });
  return response.json();
};

export const getCompanyById = async (companyId) => {
  const response = await fetch(`${API_BASE_URL}/company/${companyId}`);
  return response.json();
};

// {ownerId} is a USER id, not a company id.
export const getCompanyByOwner = async (ownerId) => {
  const response = await fetch(`${API_BASE_URL}/company/${ownerId}/owner`);
  return response.json();
};

export const getAllCompanies = async () => {
  const response = await fetch(`${API_BASE_URL}/company/all`);
  return response.json();
};

// ==========================================================================
// 2a. CATALOGUE OFFERINGS  (/companies/{companyId}/catalogue-offerings, /catalogue-offerings)
// ==========================================================================

// Upload photos first via POST /mediafiles/upload, pass returned ids as imageIds.
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
  const response = await fetch(
    `${API_BASE_URL}/companies/${companyId}/catalogue-offerings/${offeringId}`,
    { method: 'PUT', headers: getHeaders(userId), body: JSON.stringify(data) }
  );
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

// Public, paginated. body is a PagedResponse { content, pageNumber, totalPages, ... }.
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

// ==========================================================================
// 2b. CATALOGUE QUOTE REQUESTS  (/catalogue-quote-requests)
// ==========================================================================

export const requestQuote = async (userId, offeringId, message = null) => {
  const response = await fetch(`${API_BASE_URL}/catalogue-quote-requests`, {
    method: 'POST',
    headers: getHeaders(userId),
    body: JSON.stringify({ offeringId, message })
  });
  return response.json();
};

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

export const getQuotesForCompany = async (userId, companyId) => {
  const response = await fetch(`${API_BASE_URL}/catalogue-quote-requests/company/${companyId}`, {
    headers: getHeaders(userId)
  });
  return response.json();
};

// ==========================================================================
// 3. VEHICLE SALE COMPLETION  (/vehicle-sales)
// ==========================================================================

// paymentMode: CASH | CREDIT | LOAN | BANK_TRANSFER | CHEQUE
export const completeSale = async (userId, data) => {
  const response = await fetch(`${API_BASE_URL}/vehicle-sales`, {
    method: 'POST',
    headers: getHeaders(userId),
    body: JSON.stringify({
      listingId: data.listingId,
      buyerUserId: data.buyerUserId,
      buyerName: data.buyerName || null,
      salePrice: data.salePrice,
      saleDate: data.saleDate || null,
      paymentMode: data.paymentMode,
      invoiceAmount: data.invoiceAmount || null,
      invoicePaymentMethod: data.invoicePaymentMethod || null
    })
  });
  return response.json();
};

export const getSaleById = async (userId, saleId) => {
  const response = await fetch(`${API_BASE_URL}/vehicle-sales/${saleId}`, { headers: getHeaders(userId) });
  return response.json();
};

export const getSaleByListing = async (userId, listingId) => {
  const response = await fetch(`${API_BASE_URL}/vehicle-sales/listing/${listingId}`, { headers: getHeaders(userId) });
  return response.json();
};

export const getSaleInvoice = async (userId, saleId) => {
  const response = await fetch(`${API_BASE_URL}/vehicle-sales/${saleId}/invoice`, { headers: getHeaders(userId) });
  return response.json();
};

export const getMySales = async (userId) => {
  const response = await fetch(`${API_BASE_URL}/vehicle-sales/my-sales`, { headers: getHeaders(userId) });
  return response.json();
};

export const getMyPurchases = async (userId) => {
  const response = await fetch(`${API_BASE_URL}/vehicle-sales/my-purchases`, { headers: getHeaders(userId) });
  return response.json();
};
```

---

## Status & Type Constants

```javascript
// src/constants/companyFeatures.js

export const CompanyType = {
  USED_CAR_DEALER: 'USED_CAR_DEALER',
  NEW_CAR_SHOWROOM: 'NEW_CAR_SHOWROOM',
  FLEET_OWNER: 'FLEET_OWNER'
};

export const CatalogueOfferingStatus = {
  ACTIVE: 'ACTIVE',       // in browse, open to quotes
  INACTIVE: 'INACTIVE',   // hidden from browse
  ARCHIVED: 'ARCHIVED'    // retired
};

export const CatalogueQuoteRequestStatus = {
  PENDING: 'PENDING',
  ACCEPTED: 'ACCEPTED',   // contact + quotedPrice now visible
  REJECTED: 'REJECTED',
  CANCELLED: 'CANCELLED'
};

export const PaymentMode = {
  CASH: 'CASH',
  CREDIT: 'CREDIT',
  LOAN: 'LOAN',
  BANK_TRANSFER: 'BANK_TRANSFER',
  CHEQUE: 'CHEQUE'
};

// Company form: require these three inputs once NEW_CAR_SHOWROOM is selected
export const SHOWROOM_REQUIRED_FIELDS = ['showroomAddress', 'rtaApprovalNumber', 'authorizedBrands'];

// Catalogue browse sort options
export const CATALOGUE_SORT_FIELDS = ['createdAt', 'indicativePrice', 'modelYear', 'id'];
```

---

## Endpoint Reference

### Company — `/company`

| Method | Path | Auth | Notes |
|---|---|---|---|
| POST | `/company` | none | `types` optional → defaults to `["FLEET_OWNER"]`; showroom fields conditionally required |
| PUT | `/company/{id}` | none | partial update; `types` present = full replace, `[]` rejected |
| GET | `/company/{id}` | none | company id |
| GET | `/company/{id}/owner` | none | `{id}` = **user** id → that user's active company |
| GET | `/company/all` | none | active companies |
| DELETE | `/company/{id}` | none | soft delete |

### Catalogue offerings

| Method | Path | Auth | Notes |
|---|---|---|---|
| POST | `/companies/{companyId}/catalogue-offerings` | owner of a `NEW_CAR_SHOWROOM` | 201 |
| PUT | `/companies/{companyId}/catalogue-offerings/{offeringId}` | showroom owner | full field update |
| PUT | `/companies/{companyId}/catalogue-offerings/{offeringId}/status?value=` | showroom owner | `ACTIVE`/`INACTIVE`/`ARCHIVED` |
| GET | `/companies/{companyId}/catalogue-offerings` | none | that showroom's list |
| GET | `/catalogue-offerings/{id}` | none | one offering |
| GET | `/catalogue-offerings/browse?brand=&model=&minPrice=&maxPrice=&fromYear=&toYear=&companyId=&page=&size=&sortBy=&sortDirection=` | none | paginated `PagedResponse` |

### Catalogue quote requests — `/catalogue-quote-requests`

| Method | Path | Auth | Notes |
|---|---|---|---|
| POST | `/` | required | buyer; blocked on own showroom / duplicate PENDING or ACCEPTED |
| PUT | `/{id}/accept` | showroom owner | body `{ quotedPrice?, responseMessage? }`; reveals contacts |
| PUT | `/{id}/reject` | showroom owner | body `{ rejectionReason? }` |
| PUT | `/{id}/cancel` | buyer | while `PENDING` only |
| GET | `/{id}` | participant | contact fields only if `ACCEPTED` |
| GET | `/my?status=` | required | buyer's requests |
| GET | `/offering/{offeringId}` | showroom owner | requests on one offering |
| GET | `/company/{companyId}` | showroom owner | inbox across the catalogue |

### Vehicle sales — `/vehicle-sales`

| Method | Path | Auth | Notes |
|---|---|---|---|
| POST | `/` | vehicle owner or dealer-company owner | listing must be `LISTED`; one sale per listing (409) |
| GET | `/{id}` | participant | seller / buyer / dealer-company owner |
| GET | `/listing/{listingId}` | participant | sale for a listing |
| GET | `/{id}/invoice` | participant | the invoice |
| GET | `/my-sales` | required | caller was the seller |
| GET | `/my-purchases` | required | caller was the buyer |

---

## Worked examples

### Create a company that is both a used dealer and a fleet owner

`POST /company`
```json
{
  "companyOwnerId": 42,
  "name": "Al Futtaim Used Cars",
  "tradeLicenseNumber": "DED-90011",
  "types": ["USED_CAR_DEALER", "FLEET_OWNER"]
}
```
No showroom fields needed. Response `body.types` echoes both. The owner's platform role is
set to `COMPANY_OWNER`.

### Turn it into a showroom too (later)

`PUT /company/42`
```json
{
  "types": ["USED_CAR_DEALER", "FLEET_OWNER", "NEW_CAR_SHOWROOM"],
  "showroomAddress": "Sheikh Zayed Road, Dubai",
  "rtaApprovalNumber": "RTA-AC-2026-0210",
  "authorizedBrands": ["Toyota", "Lexus"]
}
```
`types` is a full replace — resend the ones you want to keep. Missing showroom fields → 400.

### Buyer quote flow

1. `GET /catalogue-offerings/browse?brand=Toyota&fromYear=2026&toYear=2026`
2. `POST /catalogue-quote-requests` `{ "offeringId": 31, "message": "OTR price?" }` → `PENDING`
3. Buyer polls `GET /catalogue-quote-requests/{id}` — `buyerContact*` / `showroomContact*` are
   `null`, `quotedPrice` is `null`.
4. Showroom: `PUT /catalogue-quote-requests/{id}/accept`
   `{ "quotedPrice": 135000, "responseMessage": "OTR incl. reg." }` → `ACCEPTED`; now the
   buyer's next `GET` returns `showroomContactEmail/Phone` and `quotedPrice`.

### Dealer completes a used-car sale

`POST /vehicle-sales`, header `X-Auto-Adda-Id: 42` (the dealer company owner; the listed
vehicle has `companyId = 42`'s company and that company is `USED_CAR_DEALER`)
```json
{ "listingId": 1, "buyerUserId": 200, "salePrice": 85000, "paymentMode": "BANK_TRANSFER" }
```
Response `body` has the `VehicleSale` + nested `invoice`. Afterwards `GET /vehicle-listing/1`
is `SOLD` and `GET /vehicles/10` shows `customerId: 200`, `previousOwnerId: 42's-user`,
`isForSale: false`.

---

## Error Responses

All returned with HTTP 200 + `status: "error"`, or HTTP 400/401 where Spring maps the
exception (`InvalidInputException` → 400, `UnauthorizedAccessException` → 401,
`ResourceNotFoundException` → 400/404). Always branch on `body`/`code`, not the HTTP status.

| Area | Scenario | code | message |
|---|---|---|---|
| Company | Unknown type string | 400 | Invalid company type: `<value>` |
| Company | `types: []` on update | 400 | A company must have at least one type |
| Company | Showroom missing a field | 400 | Showroom address / RTA approval number / At least one authorized brand is required for a NEW_CAR_SHOWROOM |
| Company | Duplicate trade licence | 400 | Trade License Number already exists |
| Company | Owner already has a company | 409 | Owner already has a registered company |
| Offering | Not found | 404 | Catalogue offering not found |
| Offering | Caller not company owner | 401 | Only the company owner can perform this action |
| Offering | Company not a showroom | 401 | This company is not a NEW_CAR_SHOWROOM |
| Offering | Bad status value | 400 | Invalid catalogue offering status: `<value>` |
| Quote | Offering inactive | 400 | This offering is no longer active |
| Quote | On your own showroom | 400 | You cannot request a quote on your own showroom |
| Quote | Duplicate active request | 400 | You already have an active quote request for this offering |
| Quote | Non-showroom responding | 403 | Only the showroom can respond to this quote request |
| Quote | Already resolved | 400 | This quote request has already been responded to |
| Quote | Non-buyer cancelling | 403 | Only the buyer can cancel this quote request |
| Quote | Non-participant read | 403 | You are not a participant in this quote request |
| Sale | Invalid payment mode | 400 | Payment mode must be one of [CASH, CREDIT, LOAN, BANK_TRANSFER, CHEQUE] |
| Sale | Listing not found | 404 | Vehicle listing not found |
| Sale | Listing not open | 400 | This listing is not open for sale |
| Sale | Already sold | 409 | A sale has already been recorded for this listing |
| Sale | Caller not owner/dealer | 403 | Only the vehicle owner or its used-car-dealer company can complete this sale |
| Sale | Buyer not registered | 404 | Buyer not found |
| Sale | Non-participant read | 403 | You are not a participant in this sale |

---

## UI Notes

### Company profile

- Render `types` as a checkbox group (`CompanyType`). When `NEW_CAR_SHOWROOM` is checked,
  reveal + require `showroomAddress`, `rtaApprovalNumber`, and an "authorized brands" tag
  input (`SHOWROOM_REQUIRED_FIELDS`).
- Pre-fill the checkbox group from `company.types` and always submit the full array.
- Gate feature areas off `company.types`:
  `company.types.includes('NEW_CAR_SHOWROOM')` → show "Manage catalogue";
  `company.types.includes('USED_CAR_DEALER')` → show "Complete a sale" on listings.

### Showroom catalogue

- **Manage catalogue** (owner): list `getOfferingsForCompany`, add/edit forms, an
  ACTIVE/INACTIVE/ARCHIVED status toggle. Photos via the standard `POST /mediafiles/upload`.
- **Browse** (any user): `browseOfferings` with brand/model/price/year filters, rendered as a
  paginated grid (`body` is a `PagedResponse`).
- **Offering detail**: specs + photos + `companyName`; "Request a quote" for signed-in users
  who don't own the showroom. Disable if `getMyQuotes` already has a `PENDING`/`ACCEPTED`
  request on that offering.
- **Showroom inbox** (`getQuotesForCompany`): one list filterable by status; accept opens a
  small `quotedPrice` + `responseMessage` form. Accepting one request does **not** close the
  offering or other requests.
- **Buyer "My enquiries"** (`getMyQuotes`): once `ACCEPTED`, surface `showroomContact*` and
  `quotedPrice` prominently.

### Vehicle sale

- Add a **"Mark as sold"** action on the seller's / dealer's listing page → form for buyer
  (user picker), `salePrice`, `paymentMode`, optional `saleDate`.
- The buyer must have an AutoAdda account (`buyerUserId` is required) — if not, prompt them to
  sign up first; there is no walk-in path.
- After success, refresh the listing (now `SOLD`) and link to the invoice
  (`getSaleInvoice`).
- **My purchases / My sales** pages from `getMyPurchases` / `getMySales`; each row links to
  the invoice, rendered as a printable receipt.
