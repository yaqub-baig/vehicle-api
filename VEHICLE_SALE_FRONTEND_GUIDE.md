# Vehicle Sale Completion - Frontend Integration Guide

## Overview

This guide covers **completing a used-car sale**: turning an active `/vehicle-listing` into a
recorded transaction. It follows the same conventions as `NUMBER_PLATE_FRONTEND_GUIDE.md` —
reuse `src/config/api.js` (`API_BASE_URL`, `getHeaders(userId)`).

Creating and browsing sale listings is unchanged (see the existing `/vehicle-listing`
endpoints). This adds the closing step:

```
Seller / dealer                                     Result
---------------                                     ------
POST /vehicle-sales   { listingId, buyerUserId,     - VehicleSale row created
                        salePrice, paymentMode }     - VehicleSaleInvoice generated
                                                     - listing.status -> SOLD
                                                     - vehicle.isForSale -> false
                                                     - vehicle ownership: customerId -> buyer,
                                                       previousOwnerId -> old owner
```

Rules:

- **Who can call it**: the current vehicle owner (`vehicle.customerId`), OR the owner of the
  vehicle's company when that company is a `USED_CAR_DEALER` (see `COMPANY_TYPES_FRONTEND_GUIDE.md`).
- **The buyer must be a registered platform user** — `buyerUserId` is required because ownership
  transfers to that account. `buyerName` is an optional display override.
- The listing must be `LISTED` (open). A listing can only be sold once — a second call returns 409.
- `paymentMode` ∈ `CASH | CREDIT | LOAN | BANK_TRANSFER | CHEQUE`.
- All `/vehicle-sales` endpoints require the `X-Auto-Adda-Id` header.

---

## API Service

```javascript
// src/services/vehicleSaleService.js
import API_BASE_URL, { getHeaders } from '../config/api';

const SALES_URL = `${API_BASE_URL}/vehicle-sales`;

export const completeSale = async (userId, data) => {
  const response = await fetch(SALES_URL, {
    method: 'POST',
    headers: getHeaders(userId),
    body: JSON.stringify({
      listingId: data.listingId,
      buyerUserId: data.buyerUserId,
      buyerName: data.buyerName || null,       // optional display override
      salePrice: data.salePrice,
      saleDate: data.saleDate || null,         // ISO date; null -> today
      paymentMode: data.paymentMode,           // CASH | CREDIT | LOAN | BANK_TRANSFER | CHEQUE
      invoiceAmount: data.invoiceAmount || null,        // null -> salePrice
      invoicePaymentMethod: data.invoicePaymentMethod || null  // null -> paymentMode
    })
  });
  return response.json();
};

export const getSaleById = async (userId, saleId) => {
  const response = await fetch(`${SALES_URL}/${saleId}`, { headers: getHeaders(userId) });
  return response.json();
};

export const getSaleByListing = async (userId, listingId) => {
  const response = await fetch(`${SALES_URL}/listing/${listingId}`, { headers: getHeaders(userId) });
  return response.json();
};

export const getInvoice = async (userId, saleId) => {
  const response = await fetch(`${SALES_URL}/${saleId}/invoice`, { headers: getHeaders(userId) });
  return response.json();
};

export const getMySales = async (userId) => {
  const response = await fetch(`${SALES_URL}/my-sales`, { headers: getHeaders(userId) });
  return response.json();
};

export const getMyPurchases = async (userId) => {
  const response = await fetch(`${SALES_URL}/my-purchases`, { headers: getHeaders(userId) });
  return response.json();
};
```

---

## Constants

```javascript
export const PaymentMode = {
  CASH: 'CASH',
  CREDIT: 'CREDIT',
  LOAN: 'LOAN',
  BANK_TRANSFER: 'BANK_TRANSFER',
  CHEQUE: 'CHEQUE'
};
```

---

## Endpoint Reference — `/vehicle-sales`

| Method | Path | Auth header | Notes |
|---|---|---|---|
| POST | `/` | required, vehicle owner or dealer-company owner | listing must be `LISTED`; one sale per listing |
| GET | `/{id}` | required, participant | seller, buyer, or dealer-company owner |
| GET | `/listing/{listingId}` | required, participant | the sale recorded for a listing |
| GET | `/{id}/invoice` | required, participant | the generated invoice |
| GET | `/my-sales` | required | sales where the caller was the seller (previous owner) |
| GET | `/my-purchases` | required | sales where the caller was the buyer (new owner) |

---

## Example: complete a sale

**Request** — `POST /vehicle-sales`, header `X-Auto-Adda-Id: 100`
```json
{
  "listingId": 1,
  "buyerUserId": 200,
  "salePrice": 85000,
  "saleDate": "2026-09-08",
  "paymentMode": "BANK_TRANSFER"
}
```

**Response**
```json
{
  "status": "success",
  "code": "200",
  "message": "Sale completed; ownership transferred to the buyer",
  "body": {
    "id": 5,
    "vehicleId": 10,
    "listingId": 1,
    "previousOwnerId": 100,
    "newOwnerId": 200,
    "buyerName": "Omar F",
    "salePrice": 85000.0,
    "saleDate": "2026-09-08",
    "paymentMode": "BANK_TRANSFER",
    "status": "ACTIVE",
    "invoice": {
      "id": 5,
      "invoiceDate": "2026-09-08",
      "totalAmount": 85000.0,
      "paymentMethod": "BANK_TRANSFER",
      "vehicleSaleId": 5
    }
  }
}
```

After this call:
- `GET /vehicle-listing/1` shows `status: "SOLD"`.
- `GET /vehicles/10` shows `customerId: 200`, `previousOwnerId: 100`, `isForSale: false`.

---

## Error Responses

Errors come back with HTTP 200 and `status: "error"` (or 400 for bean-validation failures).
Check `code` / `message`.

| Scenario | code | message |
|---|---|---|
| Invalid payment mode | 400 | Payment mode must be one of [CASH, CREDIT, LOAN, BANK_TRANSFER, CHEQUE] |
| Listing not found | 404 | Vehicle listing not found |
| Listing not open | 400 | This listing is not open for sale |
| Sale already recorded | 409 | A sale has already been recorded for this listing |
| Caller not owner / dealer | 403 | Only the vehicle owner or its used-car-dealer company can complete this sale |
| Buyer not a registered user | 404 | Buyer not found |
| Non-participant reading a sale | 403 | You are not a participant in this sale |

---

## UI Notes

- **"Mark as sold" on the seller's listing page**: open a form for buyer (user picker),
  `salePrice`, `paymentMode`, optional `saleDate`. On success, refresh the listing (now `SOLD`)
  and show a link to the invoice.
- **Buyer picker**: the buyer must have an AutoAdda account. If they don't, prompt the seller to
  have them sign up first — there is no "walk-in buyer" path that transfers ownership.
- **My purchases / my sales**: use `getMyPurchases` / `getMySales` for the user's transaction
  history; each row links to `getInvoice`.
- **Invoice view**: render `getInvoice` as a printable receipt.
