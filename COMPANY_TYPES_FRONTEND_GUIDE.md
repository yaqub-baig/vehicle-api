# Company Types - Frontend Integration Guide

## Overview

This guide covers the **company type** fields on the Company resource. It follows the same
conventions as `NUMBER_PLATE_FRONTEND_GUIDE.md` — reuse the existing `src/config/api.js`
(`API_BASE_URL`, `getHeaders(userId)`) rather than redefining it.

A company can be one or more of three types:

| Type | Meaning | Unlocks |
|---|---|---|
| `USED_CAR_DEALER` | Buys/sells second-hand cars; completes sales with ownership transfer | The vehicle-sale flow (`VEHICLE_SALE_FRONTEND_GUIDE.md`) |
| `NEW_CAR_SHOWROOM` | Authorized brand distributor; publishes a model/trim catalogue | The catalogue + quote-request flow (`SHOWROOM_CATALOGUE_FRONTEND_GUIDE.md`) |
| `FLEET_OWNER` | Just owns and manages its own vehicles (rental listings, borrowing, drivers) | Nothing extra — this is the default |

Rules:

- **A company always has at least one type.** If `types` is omitted on create, it defaults to
  `["FLEET_OWNER"]`. Companies that existed before this feature are backfilled to `FLEET_OWNER`.
- **`types` replaces, it doesn't merge.** On `PUT /company/{id}`, sending `types` overwrites the
  whole set; omitting `types` leaves it unchanged. Sending `types: []` is rejected.
- **A `NEW_CAR_SHOWROOM` must also provide** `showroomAddress`, `rtaApprovalNumber`, and a
  non-empty `authorizedBrands` list. These are optional for the other two types.
- `POST /company` still upgrades the owner's platform role to `COMPANY_OWNER` regardless of type.

> Note: `POST /company` and `PUT /company/{id}` currently do **not** require an
> `X-Auto-Adda-Id` header (unlike the newer endpoints). That is unchanged here. Still send
> `Content-Type: application/json`.

---

## API Service

```javascript
// src/services/companyService.js
import API_BASE_URL, { getHeaders } from '../config/api';

const COMPANY_URL = `${API_BASE_URL}/company`;

// types: array of "USED_CAR_DEALER" | "NEW_CAR_SHOWROOM" | "FLEET_OWNER"
export const createCompany = async (data) => {
  const response = await fetch(COMPANY_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      companyOwnerId: data.companyOwnerId,
      name: data.name,
      tradeLicenseNumber: data.tradeLicenseNumber,
      tradeLicenseExpiry: data.tradeLicenseExpiry || null,
      profileImageId: data.profileImageId || null,
      types: data.types || null,                        // null -> defaults to ["FLEET_OWNER"]
      showroomAddress: data.showroomAddress || null,    // required if types includes NEW_CAR_SHOWROOM
      rtaApprovalNumber: data.rtaApprovalNumber || null, // required if types includes NEW_CAR_SHOWROOM
      authorizedBrands: data.authorizedBrands || null    // required (non-empty) if types includes NEW_CAR_SHOWROOM
    })
  });
  return response.json();
};

// Any field omitted is left unchanged. Send `types` only when you want to replace the whole set.
export const updateCompany = async (companyId, data) => {
  const response = await fetch(`${COMPANY_URL}/${companyId}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  });
  return response.json();
};

export const getCompanyById = async (companyId) => {
  const response = await fetch(`${COMPANY_URL}/${companyId}`);
  return response.json();
};

// NOTE: {ownerId} here is a USER id, not a company id.
export const getCompanyByOwner = async (ownerId) => {
  const response = await fetch(`${COMPANY_URL}/${ownerId}/owner`);
  return response.json();
};

export const getAllCompanies = async () => {
  const response = await fetch(`${COMPANY_URL}/all`);
  return response.json();
};

export const deleteCompany = async (companyId) => {
  const response = await fetch(`${COMPANY_URL}/${companyId}`, { method: 'DELETE' });
  return response.json();
};
```

---

## Type & Field Constants

```javascript
// src/constants/company.js
export const CompanyType = {
  USED_CAR_DEALER: 'USED_CAR_DEALER',
  NEW_CAR_SHOWROOM: 'NEW_CAR_SHOWROOM',
  FLEET_OWNER: 'FLEET_OWNER'
};

// Fields the form must require once NEW_CAR_SHOWROOM is selected
export const SHOWROOM_REQUIRED_FIELDS = ['showroomAddress', 'rtaApprovalNumber', 'authorizedBrands'];
```

---

## Endpoint Reference — `/company`

| Method | Path | Auth header | Notes |
|---|---|---|---|
| POST | `/company` | none | `types` optional (defaults to `["FLEET_OWNER"]`); showroom fields conditionally required |
| PUT | `/company/{id}` | none | partial update; `types` replaces the set if present, rejected if `[]` |
| GET | `/company/{id}` | none | `{id}` = company id |
| GET | `/company/{id}/owner` | none | `{id}` = **user** id; returns that user's active company |
| GET | `/company/all` | none | active companies only |
| DELETE | `/company/{id}` | none | soft delete (`isActive = false`) |

---

## Example: create a new-car showroom

**Request** — `POST /company`
```json
{
  "companyOwnerId": 42,
  "name": "Gargash Motors",
  "tradeLicenseNumber": "DED-556677",
  "tradeLicenseExpiry": "2027-03-31",
  "types": ["NEW_CAR_SHOWROOM"],
  "showroomAddress": "Sheikh Zayed Road, Dubai",
  "rtaApprovalNumber": "RTA-AC-2026-0091",
  "authorizedBrands": ["Mercedes-Benz"]
}
```

**Response**
```json
{
  "status": null,
  "code": "200",
  "message": "success",
  "body": {
    "id": 12,
    "name": "Gargash Motors",
    "companyOwner": { "id": 42, "name": "...", "email": "...", "mobileNumber": "..." },
    "tradeLicenseNumber": "DED-556677",
    "tradeLicenseExpiry": "2027-03-31",
    "profileImage": null,
    "types": ["NEW_CAR_SHOWROOM"],
    "showroomAddress": "Sheikh Zayed Road, Dubai",
    "rtaApprovalNumber": "RTA-AC-2026-0091",
    "authorizedBrands": ["Mercedes-Benz"]
  }
}
```

Omitting `showroomAddress` from the request above returns:
```json
{ "code": "400", "message": "Showroom address is required for a NEW_CAR_SHOWROOM" }
```

## Example: add a second type to an existing company

**Request** — `PUT /company/12`
```json
{ "types": ["NEW_CAR_SHOWROOM", "USED_CAR_DEALER"] }
```
Both types are now set. (Note you must resend `NEW_CAR_SHOWROOM` — `types` is a full replace,
and the showroom fields already on the company keep it valid.)

---

## Error Responses

Errors come back with HTTP 200 (or 400 for `InvalidInputException` / `ResourceNotFoundException`,
which Spring maps via `@ResponseStatus`). Check `code` / `message`.

| Scenario | code | message |
|---|---|---|
| Unknown type string | 400 | Invalid company type: `<value>` |
| `types: []` on update | 400 | A company must have at least one type |
| Showroom without address | 400 | Showroom address is required for a NEW_CAR_SHOWROOM |
| Showroom without RTA number | 400 | RTA approval number is required for a NEW_CAR_SHOWROOM |
| Showroom without brands | 400 | At least one authorized brand is required for a NEW_CAR_SHOWROOM |
| Duplicate trade licence | 400 | Trade License Number already exists |
| Owner already has a company | 409 | Owner already has a registered company |

---

## UI Notes

- **Company create/edit form**: render `types` as a multi-select (checkboxes). When
  `NEW_CAR_SHOWROOM` is checked, reveal and require the showroom address, RTA approval number,
  and an "authorized brands" tag input.
- **Editing types**: pre-fill the multi-select from `company.types` and always submit the full
  array — the backend replaces, not merges.
- **Deriving capabilities**: use `company.types.includes('USED_CAR_DEALER')` /
  `.includes('NEW_CAR_SHOWROOM')` to decide whether to show the "Complete a sale" and
  "Manage catalogue" sections.
