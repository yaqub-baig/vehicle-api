# Carpool Direct Request — Frontend Integration Guide

## What changed and why

Previously, `POST /carpool-connections` required the caller to already have posted
their own `CarpoolListing` (an OFFER or a SEEKER) and reference it via
`requesterListingId`. That's real friction for someone who just wants to respond
to *one specific listing* they found while browsing — they shouldn't have to go
create their own public listing first.

`POST /carpool-connections` now accepts **either**:
- `requesterListingId` — the original flow, unchanged, OR
- `requesterListingDetails` — a new inline-details flow: send your route/budget/seat
  info directly in the connect request, no listing required up front.

Both produce the exact same kind of `CarpoolConnection` and go through the exact
same compatibility check, accept/reject flow, and contact-info reveal. The only
difference is *where the requester's trip details come from*.

---

## The old flow (still fully supported)

```
1. User posts a CarpoolListing (OFFER or SEEKER) — POST /carpool-listings
2. User browses/matches to find a compatible opposite-type listing
3. User connects FROM their own listing TO the target —
   POST /carpool-connections { requesterListingId, targetListingId, message }
4. Target accepts/rejects
5. On accept, both sides' contact info is revealed
```

Use this when the user genuinely wants to **be discoverable** too — e.g. a driver
who wants multiple riders to find and request their OFFER, not just connect to one
target they already picked.

## The new flow (direct request, no listing needed)

```
1. User finds ONE specific listing they want to respond to (via browse/matches,
   or a shared link, etc.) — they never post their own listing
2. User connects directly, sending their trip details inline —
   POST /carpool-connections { targetListingId, requesterListingDetails, message }
3. Server creates a hidden CarpoolListing behind the scenes (not shown in
   browse/matches/my-listings for anyone), using those details
4. From here on, it's identical to the old flow: target accepts/rejects, contact
   info revealed on accept
```

Use this when the user just wants to respond to **one listing they already found**
and doesn't care about being discoverable to others.

### Why not just drop the listing concept entirely?

Matching/compatibility, the notification text, and the connection response all
read the requester's route and budget off a real `CarpoolListing`. Rather than
rebuilding all of that for a "listing-less" request, the server quietly creates
one — so every existing rule (compatibility check, accept/reject, expiry,
contact reveal) just keeps working, unchanged. The only thing new to the frontend
is **where the trip details come from on the way in**, and that the resulting
listing won't show up anywhere except nested in the connection itself.

---

## Side-by-side comparison

| | Old flow (`requesterListingId`) | New flow (`requesterListingDetails`) |
|---|---|---|
| User must post their own listing first | Yes | No |
| User's trip is discoverable by other users (browse/matches) | Yes | No — hidden (`discoverable: false`) |
| Shows up in the user's own `GET /carpool-listings/my-listings` | Yes | No |
| Shows up nested in `GET /carpool-connections/...` responses | Yes | Yes |
| Request payload | `{ requesterListingId, targetListingId, message }` | `{ targetListingId, requesterListingDetails: {...}, message }` |
| Compatibility check, accept/reject, contact reveal, auto-expiry | Same | Same |
| Can send more than one request to the same target while one is pending | No (always was) | No |

---

## API contract

### `POST /carpool-connections`

Send **exactly one** of `requesterListingId` or `requesterListingDetails` — sending
both, or neither, returns a 400.

```ts
{
  requesterListingId?: number,          // old flow
  requesterListingDetails?: {            // new flow
    originAddress: { countryId, stateTerritoryId, territoryAreaId, streetId?, googleMapsUrl? },
    destinationAddress: { countryId, stateTerritoryId, territoryAreaId, streetId?, googleMapsUrl? },
    budget: number,
    availableSeats?: number,             // required only if you end up on the OFFER side (see below)
    carDescription?: string              // optional, OFFER side only
  },
  targetListingId: number,
  message?: string
}
```

**You don't choose your own `type` or `travelDateTime` in `requesterListingDetails`.**
They're derived automatically from the target listing:
- `type` = the opposite of the target's type (requesting a driver's OFFER makes
  you a SEEKER; offering to fulfill a rider's SEEKER makes you an OFFER).
- `travelDateTime` = copied from the target listing — you're requesting *that*
  specific trip, so there's nothing to negotiate here.

Because of that, if the target is a SEEKER listing (someone looking for a ride)
and you're responding as the driver, your implied type is OFFER — which means
`availableSeats` becomes required, same rule as creating a normal OFFER listing
directly.

---

## Worked example 1 — Rider perspective (responding to a driver's OFFER)

Aisha found an OFFER listing (id `42`, Dubai → Sharjah, 100 AED/seat) while
browsing. She doesn't want to post her own SEEKER listing — she just wants this
ride.

```javascript
// src/services/carpoolService.js
export const requestRideDirect = async (userId, targetListingId, details, message) => {
  const response = await fetch(`${CONNECTIONS_URL}`, {
    method: 'POST',
    headers: getHeaders(userId),
    body: JSON.stringify({
      targetListingId,
      requesterListingDetails: details,
      message
    })
  });
  return response.json();
};

// Calling it:
await requestRideDirect(/* userId */ 7, /* targetListingId */ 42, {
  originAddress: { countryId: 1, stateTerritoryId: 2, territoryAreaId: 15 },       // where she'll be picked up
  destinationAddress: { countryId: 1, stateTerritoryId: 4, territoryAreaId: 71 },  // where she's going
  budget: 120   // max she's willing to pay per seat
}, "Can I grab a seat? I'm flexible on pickup time.");
```

**Response** (`201`):
```json
{
  "status": "success",
  "code": "201",
  "message": "Connection request sent",
  "body": {
    "id": 88,
    "status": "PENDING",
    "message": "Can I grab a seat? I'm flexible on pickup time.",
    "requesterListing": {
      "id": 501,
      "type": "SEEKER",
      "originAddress": { "territoryArea": { "id": 15, "name": "Al Barsha" }, "...": "..." },
      "destinationAddress": { "territoryArea": { "id": 71, "name": "Al Nahda" }, "...": "..." },
      "budget": 120,
      "status": "ACTIVE"
    },
    "targetListing": { "id": 42, "type": "OFFER", "budget": 100, "...": "..." },
    "requesterContactEmail": null,
    "requesterContactPhone": null,
    "targetContactEmail": null,
    "targetContactPhone": null
  }
}
```

Note `requesterListing.id: 501` — a real listing was created for Aisha behind the
scenes. If she calls `GET /carpool-listings/my-listings`, **she will not see
listing 501 there** — it's intentionally hidden (`discoverable: false`). The only
place she'll see her trip details again is inside this connection, via
`GET /carpool-connections/my-connections` or `GET /carpool-connections/88`.

If instead she'd called `GET /carpool-listings/browse` for Dubai → Al Nahda, she
would **not** see listing 501 appear there either, and no other user would ever
match against it.

## Worked example 2 — Driver perspective (fulfilling a rider's SEEKER request)

Omar sees a SEEKER listing (id `77`) — someone needs a ride from Deira to the
airport tomorrow at 6pm, budget 80 AED. Omar has a car and seats free but hasn't
posted his own OFFER. He responds directly:

```javascript
await requestRideDirect(/* userId */ 19, /* targetListingId */ 77, {
  originAddress: { countryId: 1, stateTerritoryId: 2, territoryAreaId: 22 },   // Deira
  destinationAddress: { countryId: 1, stateTerritoryId: 2, territoryAreaId: 30 }, // Airport
  budget: 70,              // his asking price per seat (<= rider's 80 budget, so compatible)
  availableSeats: 2,       // REQUIRED - he's on the OFFER side here
  carDescription: "White Toyota Camry"
}, "I can take you, leaving right on time.");
```

Because the target (`77`) is a SEEKER, Omar's implied type is **OFFER** — so
`availableSeats` is mandatory here. Omitting it (or sending `0`) returns:
```json
{ "status": "failed", "code": "400", "message": "Available seats are required for an offer listing" }
```

Everything else — budget compatibility (`70 <= 80` ✓), territory-area matching,
notification to the rider, accept/reject, contact reveal — behaves exactly like
the explicit-listing flow.

---

## Duplicate-request rule (applies to both flows)

A user can have **at most one PENDING or ACCEPTED connection request against a
given target listing**, regardless of whether they used `requesterListingId` or
`requesterListingDetails`, and regardless of which (or how many) of their own
listings they have. Trying to send a second request to the same target while the
first is still open returns:
```json
{ "status": "failed", "code": "400", "message": "A connection request already exists between these listings" }
```
So don't build UI that lets a user "try again with different details" against the
same target while a request is pending — surface the existing pending request
instead (`GET /carpool-connections/my-connections?status=PENDING`).

---

## New/changed error responses

| Scenario | code | message |
|---|---|---|
| Both `requesterListingId` and `requesterListingDetails` given, or neither | 400 | Provide exactly one of requesterListingId or requesterListingDetails |
| Implied type is OFFER but `availableSeats` missing/zero | 400 | Available seats are required for an offer listing |
| `requesterListingDetails` references an unknown address id | 404 | Country not found / State/territory not found / Territory area not found / Street not found |
| Requesting your own target listing | 400 | Cannot connect to your own listing |

All other validation/compatibility errors are unchanged from the existing
`CARPOOL_FRONTEND_GUIDE.md` error table.

---

## Migration notes for existing frontend code

- Any code already calling `POST /carpool-connections` with `requesterListingId`
  needs **no changes** — fully backward compatible.
- If you want to add a "Request this ride" button directly on a listing card
  (browse/match results) that doesn't require the user to have posted anything,
  wire it to the new `requesterListingDetails` path instead of forcing a
  "post your own listing" step first.
- Don't assume every `requesterListing` in a connection response was something
  the user can find again via `my-listings` — if `discoverable` matters to your
  UI (e.g. you're building a listing-management screen), treat the connection's
  nested `requesterListing` as read-only trip info, not necessarily a listing the
  user manages independently.
