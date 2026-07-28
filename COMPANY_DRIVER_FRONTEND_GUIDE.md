# Company Driver Management - Frontend Integration Guide

## Overview

This guide documents the Company Driver Management feature: it lets a company
owner maintain a roster of drivers under their company, making those drivers
available for selection as borrowers elsewhere in the app (e.g. quick-borrow
flows). Only the company owner can add, remove, or list drivers for their
company.

---

## Table of Contents

1. [Authentication](#authentication)
2. [API Endpoints](#api-endpoints)
3. [Request/Response Models](#requestresponse-models)
4. [User Flow](#user-flow)
5. [Code Examples](#code-examples)
6. [Error Handling](#error-handling)
7. [UI/UX Recommendations](#uiux-recommendations)

---

## Authentication

All endpoints require the caller's user ID in the request header:

```http
X-Auto-Adda-Id: <user_id>
```

The caller must be the **owner** of the company (`companyId`) being acted on.
Any other caller receives a `401` authorization error.

---

## API Endpoints

### Base URL
```
http://your-api-domain.com/companies/{companyId}/drivers
```

### 1. Add a Driver

**Add a driver to the company's roster**

```http
POST /companies/{companyId}/drivers
```

**Headers:**
```http
Content-Type: application/json
X-Auto-Adda-Id: <company_owner_user_id>
```

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| companyId | Long | ID of the company |

**Request Body:**
```json
{
  "driverUserId": 12345
}
```

| Field | Type | Required | Description |
|-------|------|----------|--------------|
| driverUserId | Integer | Yes | User ID of the person to add as a driver |

**Success Response — new driver (201):**
```json
{
  "status": "success",
  "code": "201",
  "message": "Driver added",
  "body": {
    "id": 1,
    "companyId": 10,
    "driverUserId": 12345,
    "driverName": "John Doe",
    "driverMobileNumber": "9876543210",
    "createdAt": "2026-07-28T10:15:30"
  }
}
```

**Success Response — re-adding a previously removed driver (200):**
```json
{
  "status": "success",
  "code": "200",
  "message": "Driver added",
  "body": { "...": "same shape as above" }
}
```

> Removing a driver is a soft delete. Adding the same `driverUserId` again
> reactivates the existing roster entry (`200`) instead of creating a new one
> (`201`).

---

### 2. Remove a Driver

**Remove a driver from the company's roster**

```http
DELETE /companies/{companyId}/drivers/{driverUserId}
```

**Headers:**
```http
X-Auto-Adda-Id: <company_owner_user_id>
```

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| companyId | Long | ID of the company |
| driverUserId | Integer | User ID of the driver to remove |

**Success Response (200):**
```json
{
  "status": "success",
  "code": "200",
  "message": "Driver removed",
  "body": null
}
```

---

### 3. List Drivers

**List all active drivers for a company**

```http
GET /companies/{companyId}/drivers
```

**Headers:**
```http
X-Auto-Adda-Id: <company_owner_user_id>
```

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| companyId | Long | ID of the company |

**Success Response (200):**
```json
{
  "status": "success",
  "code": "200",
  "message": "Drivers retrieved successfully",
  "body": [
    {
      "id": 1,
      "companyId": 10,
      "driverUserId": 12345,
      "driverName": "John Doe",
      "driverMobileNumber": "9876543210",
      "createdAt": "2026-07-28T10:15:30"
    }
  ]
}
```

Results are ordered newest-first and only include active (non-removed) drivers.

---

## Request/Response Models

### AddCompanyDriverRequest
```typescript
interface AddCompanyDriverRequest {
  driverUserId: number; // required
}
```

### CompanyDriverResponse
```typescript
interface CompanyDriverResponse {
  id: number;
  companyId: number;
  driverUserId: number;
  driverName: string | null;        // null if the user-service lookup failed
  driverMobileNumber: string | null; // null if the user-service lookup failed
  createdAt: string; // ISO 8601 timestamp
}
```

`driverName` and `driverMobileNumber` are resolved from the auth/user service
at read time. If that lookup fails, these fields come back `null` rather than
failing the whole request — handle `null` gracefully in the UI (e.g. show
"Unknown driver").

---

## User Flow

1. Company owner opens the "Manage Drivers" screen for their company.
2. Frontend calls `GET /companies/{companyId}/drivers` to show the current roster.
3. Owner adds a driver by user ID (or via a user search/lookup elsewhere in
   the app that resolves to a `driverUserId`) → `POST /companies/{companyId}/drivers`.
4. Owner removes a driver → `DELETE /companies/{companyId}/drivers/{driverUserId}`.
5. Refresh the list after any add/remove so the UI reflects the current roster.

---

## Code Examples

### Fetch drivers
```javascript
async function getCompanyDrivers(companyId, userId) {
  const res = await fetch(`/companies/${companyId}/drivers`, {
    headers: { 'X-Auto-Adda-Id': userId },
  });
  const data = await res.json();
  if (data.status !== 'success') {
    throw new Error(data.message);
  }
  return data.body;
}
```

### Add a driver
```javascript
async function addCompanyDriver(companyId, userId, driverUserId) {
  const res = await fetch(`/companies/${companyId}/drivers`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-Auto-Adda-Id': userId,
    },
    body: JSON.stringify({ driverUserId }),
  });
  const data = await res.json();
  if (data.status !== 'success') {
    throw new Error(data.message);
  }
  return data.body;
}
```

### Remove a driver
```javascript
async function removeCompanyDriver(companyId, userId, driverUserId) {
  const res = await fetch(`/companies/${companyId}/drivers/${driverUserId}`, {
    method: 'DELETE',
    headers: { 'X-Auto-Adda-Id': userId },
  });
  const data = await res.json();
  if (data.status !== 'success') {
    throw new Error(data.message);
  }
}
```

---

## Error Handling

> **Note:** All responses — including errors — are returned with HTTP status
> `200 OK`. Always check the `status` and `code` fields in the JSON body
> rather than the HTTP status code to determine success or failure.

| Scenario | `status` | `code` | `message` |
|----------|----------|--------|-----------|
| Company does not exist | failed | 404 | Company not found |
| Caller is not the company owner | failed | 401 | Unauthorized |
| `driverUserId` missing from request body | failed | 400 | Validation error naming the field |
| Driver user ID does not resolve to a real user | error | 404 | Driver not found |
| Driver is already active on the roster | error | 400 | This user is already a driver for this company |
| Removing a driver that isn't on the roster (or already removed) | error | 404 | Driver not found |

Recommended handling:
- Surface `message` directly to the user for `400`/`404` responses.
- On `401`, hide/disable the driver-management UI — the caller isn't the
  company owner.
- Treat network/`5xx`-style failures (rare, since most errors return `200`)
  as a generic "something went wrong, try again" state.

---

## UI/UX Recommendations

- Only show the "Manage Drivers" screen to users who own the company (check
  this client-side too, to avoid a round-trip just to get a `401`).
- When adding a driver, validate/resolve the `driverUserId` via your existing
  user search UI rather than asking owners to type raw numeric IDs.
- After a successful add/remove, optimistically update the local list, but
  reconcile with a fresh `GET` if the owner navigates back to the screen.
- Because `driverName`/`driverMobileNumber` can be `null`, render a fallback
  (e.g. "Unknown driver — ID {driverUserId}") instead of blank text.
