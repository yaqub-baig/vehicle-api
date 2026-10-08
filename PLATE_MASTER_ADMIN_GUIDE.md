# Plate Master Data (Source / Type / Code) - Admin Panel Integration Guide

## Overview

This guide covers the admin panel screens for managing number plate master data. It follows the
same conventions as `NUMBER_PLATE_FRONTEND_GUIDE.md` — reuse the existing `src/config/api.js`
(`API_BASE_URL`, `getHeaders(userId)`) rather than redefining it.

The master data is a three-level hierarchy:

| Level | Example | Belongs to | Unique by |
|---|---|---|---|
| **Plate source** | `DUBAI` / "Dubai" | — | `code` |
| **Plate type** | `PRIVATE` / "Private" | one plate source | `code` within its source |
| **Plate code** | `A`, `1`, `WHITE` | one plate type | `code` within its type |

- **Types are per source.** Dubai's `PRIVATE` and Abu Dhabi's `PRIVATE` are two different rows with
  different ids and different code lists. Always create a type under a specific source.
- **Codes are per type.** `WHITE` under Dubai/PRIVATE and `WHITE` under Sharjah/MOTORCYCLE are
  separate rows.
- **Some types have no codes** (e.g. Dubai/GOVERNMENT). That's valid: sellers then list without a
  plate code. As soon as a type has at least one active code, sellers **must** pick one.
- Plate codes have no separate display name — the `code` itself is what users see.

The initial data (7 sources, 61 types, 148 codes) is seeded by
`migrations/2026-10-08_plate_master_seed_and_listing_backfill.sql`. The admin panel maintains it
from there.

Number plate listings store the **ids** of the source/type/code, not their text. Renaming a
`code` or `name` is therefore safe — existing listings pick up the new label automatically.

> Note: these endpoints currently do **not** check for an admin role or require an
> `X-Auto-Adda-Id` header. Restrict access to these screens in the admin panel itself.
> Still send `Content-Type: application/json`.

---

## Response and error handling

Every endpoint returns the standard `GenericResponse` envelope:

```json
{ "status": "success", "code": "200", "message": "...", "body": { } }
```

**Errors are returned with HTTP 200**, not a 4xx status. Check `status` / `code` in the body,
not `response.ok`:

```json
{ "status": "failed", "code": "409", "message": "Duplicate entry or data constraint violation" }
```

| `code` | When |
|---|---|
| `400` | Validation failed, e.g. `"Validation failed: code: Plate type code is required"` |
| `404` | Id not found (`"Plate source not found"`, `"Plate type not found"`, `"Plate code not found"`), including a missing parent `plateSourceId` / `plateTypeId` |
| `409` | Duplicate `code` under the same parent. The message is the generic `"Duplicate entry or data constraint violation"`, so show your own text, e.g. "This code already exists for the selected source". |

---

## API Service

```javascript
// src/services/plateMasterService.js
import API_BASE_URL from '../config/api';

const SOURCES_URL = `${API_BASE_URL}/plate-sources`;
const TYPES_URL = `${API_BASE_URL}/plate-types`;
const CODES_URL = `${API_BASE_URL}/plate-codes`;
const JSON_HEADERS = { 'Content-Type': 'application/json' };

// Errors come back as HTTP 200 with status "failed" - surface them as exceptions.
const handle = async (response) => {
  const data = await response.json();
  if (data.status !== 'success') {
    const error = new Error(data.message || 'Request failed');
    error.code = data.code; // "400" | "404" | "409"
    throw error;
  }
  return data.body;
};

// ============================================
// TREE (read-only, everything active in one call)
// ============================================

// [{ id, code, name, plateTypes: [{ id, code, name, plateCodes: [{ id, code }] }] }]
export const getPlateMasterTree = () => fetch(`${SOURCES_URL}/tree`).then(handle);

// ============================================
// PLATE SOURCES
// ============================================

export const listPlateSources = () => fetch(SOURCES_URL).then(handle);

export const getPlateSource = (id) => fetch(`${SOURCES_URL}/${id}`).then(handle);

// data: { code, name, displayOrder }
export const createPlateSource = (data) =>
  fetch(SOURCES_URL, { method: 'POST', headers: JSON_HEADERS, body: JSON.stringify(data) }).then(handle);

export const updatePlateSource = (id, data) =>
  fetch(`${SOURCES_URL}/${id}`, { method: 'PUT', headers: JSON_HEADERS, body: JSON.stringify(data) }).then(handle);

export const deletePlateSource = (id) => fetch(`${SOURCES_URL}/${id}`, { method: 'DELETE' }).then(handle);

// ============================================
// PLATE TYPES
// ============================================

// plateSourceId optional - omit to list types across all sources
export const listPlateTypes = (plateSourceId) =>
  fetch(plateSourceId ? `${TYPES_URL}?plateSourceId=${plateSourceId}` : TYPES_URL).then(handle);

export const getPlateType = (id) => fetch(`${TYPES_URL}/${id}`).then(handle);

// data: { code, name, plateSourceId, displayOrder }
export const createPlateType = (data) =>
  fetch(TYPES_URL, { method: 'POST', headers: JSON_HEADERS, body: JSON.stringify(data) }).then(handle);

export const updatePlateType = (id, data) =>
  fetch(`${TYPES_URL}/${id}`, { method: 'PUT', headers: JSON_HEADERS, body: JSON.stringify(data) }).then(handle);

export const deletePlateType = (id) => fetch(`${TYPES_URL}/${id}`, { method: 'DELETE' }).then(handle);

// ============================================
// PLATE CODES
// ============================================

// plateTypeId optional - omit to list codes across all types
export const listPlateCodes = (plateTypeId) =>
  fetch(plateTypeId ? `${CODES_URL}?plateTypeId=${plateTypeId}` : CODES_URL).then(handle);

export const getPlateCode = (id) => fetch(`${CODES_URL}/${id}`).then(handle);

// data: { code, plateTypeId, displayOrder }
export const createPlateCode = (data) =>
  fetch(CODES_URL, { method: 'POST', headers: JSON_HEADERS, body: JSON.stringify(data) }).then(handle);

export const updatePlateCode = (id, data) =>
  fetch(`${CODES_URL}/${id}`, { method: 'PUT', headers: JSON_HEADERS, body: JSON.stringify(data) }).then(handle);

export const deletePlateCode = (id) => fetch(`${CODES_URL}/${id}`, { method: 'DELETE' }).then(handle);
```

---

## Endpoint Reference

### Plate sources — `/plate-sources`

| Method | Path | Body | Notes |
|---|---|---|---|
| GET | `/tree` | — | all **active** sources → types → codes, nested |
| GET | `/` | — | active sources, sorted by `displayOrder` then `id` |
| GET | `/{id}` | — | returns the row even if inactive |
| POST | `/` | `{ code*, name*, displayOrder }` | 409 if `code` already exists |
| PUT | `/{id}` | `{ code*, name*, displayOrder }` | full replace (see below) |
| DELETE | `/{id}` | — | soft delete (`isActive = false`) |

### Plate types — `/plate-types`

| Method | Path | Body | Notes |
|---|---|---|---|
| GET | `/?plateSourceId=` | — | active types, optionally for one source |
| GET | `/{id}` | — | includes the nested `plateSource` |
| POST | `/` | `{ code*, name*, plateSourceId*, displayOrder }` | 409 if `code` exists under that source |
| PUT | `/{id}` | `{ code*, name*, plateSourceId*, displayOrder }` | full replace |
| DELETE | `/{id}` | — | soft delete |

### Plate codes — `/plate-codes`

| Method | Path | Body | Notes |
|---|---|---|---|
| GET | `/?plateTypeId=` | — | active codes, optionally for one type |
| GET | `/{id}` | — | includes the nested `plateType` → `plateSource` |
| POST | `/` | `{ code*, plateTypeId*, displayOrder }` | 409 if `code` exists under that type |
| PUT | `/{id}` | `{ code*, plateTypeId*, displayOrder }` | full replace |
| DELETE | `/{id}` | — | soft delete |

`*` = required. `code` columns hold up to 30 characters, `name` up to 150.

---

## Rules the admin UI should respect

**PUT is a full replace.** Send every field, including `displayOrder`. Omitting `displayOrder`
clears it to `null`, which sorts the row **first** in every list. Pre-fill the edit form from
`GET /{id}` and send the whole object back.

**`code` is case-insensitive for duplicates.** `dubai` and `DUBAI` clash. Use uppercase with
underscores (`PUBLIC_TRANSPORT`) to match the seeded data.

**Don't move a type or code to a different parent.** The API accepts a changed `plateSourceId` /
`plateTypeId` on PUT, but existing listings keep their old source/type id, so they end up with
a type that no longer belongs to their source. Lock the parent dropdown on the edit form; to
"move" one, create a new row under the right parent and delete the old one.

**Delete is a soft delete and does not cascade.**
- Deleting a **source** hides it and everything under it from `/tree`, but its types and codes
  stay active and still appear in `/plate-types` and `/plate-codes` list calls. Delete the
  children too if they should disappear from the admin lists.
- Deleting a **type** hides it (and its codes) from `/tree`.
- Existing listings keep pointing at deleted rows and still display them. New listings and
  listing edits that pick a deleted source/type/code are rejected with 404.
- Deleting the **last code** of a type turns it into a no-code type: sellers can then list it
  without a code, and listings that already have a code are unaffected.
- Adding the **first code** to a no-code type makes a code mandatory for new listings and for
  any edit of an existing listing of that type.

Show a confirmation that spells this out before deleting.

**There is no restore and no way to list deleted rows.** A deleted row can't be reactivated
through the API, and its `code` stays taken, so re-creating
the same code under the same parent returns 409. If an admin deletes something by mistake, it
has to be reactivated directly in the database (`UPDATE plate_type SET is_active = 1 WHERE id = ...`).

---

## Suggested admin screens

1. **Plate sources** — table of sources (code, name, display order) with add / edit / delete.
   Clicking a row opens its types.
2. **Plate types** for a source — `GET /plate-types?plateSourceId={id}`. Add/edit form with the
   source fixed (not editable). Show a "No codes" badge for types whose code list is empty.
3. **Plate codes** for a type — `GET /plate-codes?plateTypeId={id}`. Codes are short, so a
   chip/tag list with inline add and remove works well.

Alternatively, build one tree view from `GET /plate-sources/tree` and call the individual
create/update/delete endpoints from it, re-fetching the tree after each change.

---

## Examples

### Create a plate type under Dubai

**Request** — `POST /plate-types`
```json
{ "code": "VINTAGE", "name": "Vintage", "plateSourceId": 2, "displayOrder": 17 }
```

**Response**
```json
{
  "status": "success",
  "code": "201",
  "message": "Plate type created successfully",
  "body": {
    "id": 62,
    "code": "VINTAGE",
    "name": "Vintage",
    "displayOrder": 17,
    "isActive": true,
    "plateSource": { "id": 2, "code": "DUBAI", "name": "Dubai", "displayOrder": 2, "isActive": true }
  }
}
```

### Add a code to that type

**Request** — `POST /plate-codes`
```json
{ "code": "V", "plateTypeId": 62, "displayOrder": 1 }
```

**Response**
```json
{
  "status": "success",
  "code": "201",
  "message": "Plate code created successfully",
  "body": {
    "id": 149,
    "code": "V",
    "displayOrder": 1,
    "isActive": true,
    "plateType": {
      "id": 62,
      "code": "VINTAGE",
      "name": "Vintage",
      "displayOrder": 17,
      "isActive": true,
      "plateSource": { "id": 2, "code": "DUBAI", "name": "Dubai", "displayOrder": 2, "isActive": true }
    }
  }
}
```

### Duplicate code

**Request** — `POST /plate-codes` with `{ "code": "v", "plateTypeId": 62 }` again

**Response** (HTTP 200)
```json
{ "status": "failed", "code": "409", "message": "Duplicate entry or data constraint violation" }
```

### Tree

**Request** — `GET /plate-sources/tree`

**Response** (trimmed)
```json
{
  "status": "success",
  "code": "200",
  "message": "Plate master data retrieved successfully",
  "body": [
    {
      "id": 2,
      "code": "DUBAI",
      "name": "Dubai",
      "plateTypes": [
        { "id": 14, "code": "PRIVATE", "name": "Private", "plateCodes": [{ "id": 35, "code": "WHITE" }, { "id": 36, "code": "A" }] },
        { "id": 17, "code": "GOVERNMENT", "name": "Government", "plateCodes": [] }
      ]
    }
  ]
}
```

Ids in these examples are illustrative; real ids depend on insert order in your database.
