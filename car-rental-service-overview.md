# Car Rental Service — Feature Overview

**Audience:** Project owner / stakeholders
**Scope:** The peer-to-peer car rental marketplace inside the AutoAdda Vehicle API — not to be confused with the separate Car Pooling or Vehicle Borrowing (trusted-circle) features.

---

## 1. What it is

A vehicle owner lists one of their own cars for rent at a daily (and optionally hourly) rate. Any other user can browse listings, request to rent one, and — once the owner accepts — pick it up, drive it, and return it. The system automatically prices the rental at return time based on how long it was out, how far it was driven, and whether it came back late.

It is a two-sided marketplace with two distinct perspectives:

| | Owner | Renter |
|---|---|---|
| Publishes a listing for their vehicle | ✅ | — |
| Browses/searches listings | — | ✅ |
| Sends a rental request | — | ✅ |
| Accepts / declines a request | ✅ | — |
| Picks up the vehicle | — | ✅ |
| Sets a negotiated discount | ✅ | — |
| Returns the vehicle | — | ✅ |
| Receives the final price breakdown | ✅ | ✅ |

A diagram of this end-to-end journey is available at [`car-rental-service-swimlane.png`](./car-rental-service-swimlane.png) (see the note in [§6](#6-relationship-to-the-swimlane-diagram) — it illustrates the concept, not the exact return mechanics described below).

---

## 2. The three moving parts

The feature is built from three linked concepts, each with its own lifecycle:

```
CarRentalListing  --(renter requests)-->  RentalRequest  --(owner accepts)-->  RentalCycle
   (the ad)                                (the ask)                          (the actual rental)
```

### 2.1 Listing — `CarRentalListing`

The owner's ad for one of their vehicles.

- Created against a vehicle the caller already owns (`VehicleDetails.customerId` must match the caller).
- A vehicle can only have **one active listing at a time**.
- Required: price/day, km limit per day, excess-km penalty rate, late-return penalty rate, location. Price/hour is optional (an owner can choose to rent daily-only).
- Car photos are not stored on the listing — every listing response includes the linked vehicle's photos (`vehicleImages` and `mainImage`), resolved from its `VehicleDetails` record.
- Statuses: **ACTIVE → PAUSED ⇄ ACTIVE**, or **→ REMOVED** (terminal). Pausing hides it from browse without losing its history; an owner can't reactivate a listing while another active listing already exists for the same vehicle.
- Browsing (`GET /car-rental-listings/browse`) is public, paginated, and filterable by brand, model, location, price range, rate type, and minimum daily km limit.

### 2.2 Request — `RentalRequest`

A renter's ask to rent a specific listing.

- Renter picks a rate type (hourly or daily) and can attach an optional message.
- Renter also declares whether the rental is **UNLIMITED** (open-ended — the original behavior) or **LIMITED** with a duration count in the same unit as the rate type (e.g. `durationValue: 3` means 3 days for a DAILY request, or 3 hours for HOURLY). LIMITED requires a positive duration; for UNLIMITED the field is ignored/cleared even if the client sends one.
- A renter can't request their own listing, can't have two pending requests on the same listing at once, and can't request a vehicle that's already out on an open rental.
- Statuses: **PENDING → ACCEPTED**, **PENDING → REJECTED** (owner, with an optional reason), or **PENDING → CANCELLED** (renter, only while still pending).
- **Accepting a request reveals both parties' name, email, and phone to each other** (via email/push/SMS, subject to each user's notification preferences) so they can coordinate pickup — and it's the moment a `RentalCycle` is created.

### 2.3 Cycle — `RentalCycle`

The actual rental, from acceptance to completion.

- Created the instant a request is accepted. It **snapshots** the listing's rate, km limit, and penalty rates at that moment — and also the request's UNLIMITED/LIMITED choice and duration — so if the owner edits the listing later, an in-progress or already-completed rental is unaffected.
- Statuses: **AWAITING_START → ACTIVE → COMPLETED**, or **AWAITING_START → CANCELLED** (either party, only before pickup — once the renter has started the cycle, neither side can cancel it).
- **Start (pickup):** renter records the starting odometer reading. What happens to the expected return time depends on the rental type snapshotted at accept time:
  - **UNLIMITED:** the renter may optionally give an expected return time at pickup. If omitted, no late-return penalty can ever apply to this rental — exactly as before this feature existed.
  - **LIMITED:** the expected return time is computed automatically as *pickup time + the duration declared at request time*. This is a binding commitment — anything the renter sends in this field at pickup is ignored in favor of the system-computed value.
- **Discount:** the owner can set (or change) a flat, off-platform-negotiated discount amount at any point before the rental is returned.
- **Return:** renter records the ending odometer reading — that's the *only* input required. The system then computes the final price automatically (see below) and marks the cycle COMPLETED. There is no separate owner-side confirmation step.

---

## 3. How the final price is calculated

Set at return time, in this order:

1. **Duration units** — hours or days (whichever the request's rate type was), rounded *up* to the next whole unit, minimum of 1.
2. **Base price** = rate (snapshotted at pickup) × duration units.
3. **Excess-km penalty** — the allowed distance is `km-limit-per-day × number of days the rental ran` (also rounded up, min. 1 day). Anything driven beyond that allowance is charged at the excess-km penalty rate.
4. **Late-return penalty** — only if an expected return time was set (always true for LIMITED; optional and renter-chosen for UNLIMITED — see §2.3). Hours late are rounded up, and each hour is charged at the late-return penalty rate.
5. **Final price** = base price − discount + excess-km penalty + late-return penalty, floored at zero (a rental can never come back owing a negative amount).

**Worked example:** ₹1,500/day, 200 km/day limit, ₹5/km excess penalty, ₹100/hr late penalty, ₹200 discount agreed. Renter keeps the car 2 days, drives 460 km, returns 3 hours late.
- Base: 2 × ₹1,500 = ₹3,000
- Excess km: 460 − (200×2) = 60 km × ₹5 = ₹300
- Late: 3 hrs × ₹100 = ₹300
- Final: ₹3,000 − ₹200 + ₹300 + ₹300 = **₹3,400**

Both parties receive this full breakdown by email and push notification when the rental completes.

---

## 4. Notifications sent at each step

| Trigger | Who's notified | Channels |
|---|---|---|
| Request created | Owner (new request) + Renter (confirmation) | Email, push, SMS (owner only) |
| Request accepted | Both parties, with each other's contact details | Email, push, SMS |
| Request rejected | Renter, with the owner's stated reason if given | Email, push |
| Request cancelled | Owner | Push |
| Vehicle picked up (cycle started) | Owner | Push |
| Rental returned (cycle completed) | Both parties, with the full price breakdown | Email, push |
| Rental cancelled | Both parties | Push |

All channels respect each user's notification preferences (email/push/SMS toggles), except a picked-up/cancelled push which defaults to on unless explicitly turned off.

---

## 5. Business rules worth knowing

- **One active listing per vehicle** — prevents an owner double-listing (or accidentally renting out) the same car twice.
- **One open rental per vehicle** — a vehicle already on an accepted request or active cycle can't be requested or accepted again until that cycle finishes or is cancelled.
- **Rate-card snapshotting** — protects both sides from an owner changing terms mid-rental.
- **LIMITED duration is binding, not a suggestion** — declared at request time (so the owner sees it before accepting), frozen onto the cycle at acceptance, and converted into a real expected-return timestamp at pickup automatically. Unlike an UNLIMITED rental's optional expected-return time, the renter can't skip or renegotiate it at pickup.
- **Cancellation window** — either party can back out cleanly only before pickup; once the renter has the keys, the rental must run its course to a return.
- **No manual price override at return** — the only lever available to the owner is the pre-return discount; excess-km and late-return penalties are always system-computed from the odometer and clock, not negotiable in-app.

---

## 6. Relationship to the swimlane diagram

`car-rental-service-swimlane.png` illustrates the core flow: List & Publish → Browse & Search → Send Request → Accept/Reject/Cancel → Start Rental → Return Vehicle → COMPLETED, including the optional pre-return discount and the single-step, system-priced return described in §2.3 and §3. The Send Request step also shows the renter's UNLIMITED/LIMITED(n) choice from §2.2, and the legend notes its binding effect on the return time at pickup, as described in §2.3.
