# Vehicle Ownership Claim - Frontend Integration Guide

## Overview

This guide provides complete documentation for integrating the Vehicle Ownership Claim feature into your frontend application. The feature allows users to request ownership of vehicles by chassis number, with an approval workflow for current owners.

---

## Table of Contents

1. [Authentication](#authentication)
2. [API Endpoints](#api-endpoints)
3. [Request/Response Models](#requestresponse-models)
4. [User Flows](#user-flows)
5. [Code Examples](#code-examples)
6. [Error Handling](#error-handling)
7. [UI/UX Recommendations](#uiux-recommendations)

---

## Authentication

All endpoints require the user ID to be passed in the request header:

```http
X-Auto-Adda-Id: <user_id>
```

**Example:**
```http
X-Auto-Adda-Id: 12345
```

---

## API Endpoints

### Base URL
```
http://your-api-domain.com/ownership-claims
```

### 1. Create Ownership Claim

**Request ownership of a vehicle by chassis number**

```http
POST /ownership-claims/request
```

**Headers:**
```http
Content-Type: application/json
X-Auto-Adda-Id: <requester_user_id>
```

**Request Body:**
```json
{
  "chassisNo": "ABC123XYZ789",
  "reason": "I purchased this vehicle from the previous owner on 2024-01-15"
}
```

**Success Response (201):**
```json
{
  "status": "success",
  "code": "201",
  "message": "Ownership claim created successfully. Awaiting owner approval.",
  "body": {
    "id": 1,
    "chassisNo": "ABC123XYZ789",
    "vehicleId": 456,
    "requesterId": 12345,
    "requesterName": null,
    "currentOwnerId": 67890,
    "currentOwnerName": null,
    "reason": "I purchased this vehicle from the previous owner on 2024-01-15",
    "status": "PENDING",
    "vehicleBrand": null,
    "vehicleModel": null,
    "vehicleYear": null,
    "requestDate": "2024-01-20T10:30:00",
    "responseDate": null,
    "responseNotes": null,
    "createdAt": "2024-01-20T10:30:00",
    "updatedAt": "2024-01-20T10:30:00"
  }
}
```

**Error Responses:**

- **404 - Vehicle Not Found:**
```json
{
  "status": "error",
  "code": "404",
  "message": "Vehicle with chassis number ABC123XYZ789 not found",
  "body": null
}
```

- **400 - Already Owner:**
```json
{
  "status": "error",
  "code": "400",
  "message": "You already own this vehicle",
  "body": null
}
```

- **400 - Duplicate Claim:**
```json
{
  "status": "error",
  "code": "400",
  "message": "You already have a pending claim for this vehicle",
  "body": null
}
```

---

### 2. Get My Requests

**Get all ownership claims made by the current user**

```http
GET /ownership-claims/my-requests
```

**Headers:**
```http
X-Auto-Adda-Id: <user_id>
```

**Success Response (200):**
```json
{
  "status": "success",
  "code": "200",
  "message": "Claims retrieved successfully",
  "body": [
    {
      "id": 1,
      "chassisNo": "ABC123XYZ789",
      "vehicleId": 456,
      "requesterId": 12345,
      "currentOwnerId": 67890,
      "reason": "I purchased this vehicle",
      "status": "PENDING",
      "requestDate": "2024-01-20T10:30:00",
      "responseDate": null,
      "responseNotes": null,
      "createdAt": "2024-01-20T10:30:00",
      "updatedAt": "2024-01-20T10:30:00"
    },
    {
      "id": 2,
      "chassisNo": "XYZ789ABC123",
      "vehicleId": 789,
      "requesterId": 12345,
      "currentOwnerId": 11111,
      "reason": "Vehicle transfer",
      "status": "APPROVED",
      "requestDate": "2024-01-15T08:00:00",
      "responseDate": "2024-01-16T14:20:00",
      "responseNotes": "Transfer approved",
      "createdAt": "2024-01-15T08:00:00",
      "updatedAt": "2024-01-16T14:20:00"
    }
  ]
}
```

---

### 3. Get Pending Claims (For Owners)

**Get all pending ownership claims for vehicles owned by the current user**

```http
GET /ownership-claims/pending
```

**Headers:**
```http
X-Auto-Adda-Id: <owner_user_id>
```

**Success Response (200):**
```json
{
  "status": "success",
  "code": "200",
  "message": "Pending claims retrieved successfully",
  "body": [
    {
      "id": 3,
      "chassisNo": "DEF456GHI012",
      "vehicleId": 999,
      "requesterId": 22222,
      "currentOwnerId": 12345,
      "reason": "I bought this car from you last week",
      "status": "PENDING",
      "requestDate": "2024-01-21T09:15:00",
      "responseDate": null,
      "responseNotes": null,
      "createdAt": "2024-01-21T09:15:00",
      "updatedAt": "2024-01-21T09:15:00"
    }
  ]
}
```

---

### 4. Get Claim by ID

**Get details of a specific ownership claim**

```http
GET /ownership-claims/{claimId}
```

**Path Parameters:**
- `claimId` (Long) - The ID of the claim

**Success Response (200):**
```json
{
  "status": "success",
  "code": "200",
  "message": "Claim retrieved successfully",
  "body": {
    "id": 1,
    "chassisNo": "ABC123XYZ789",
    "vehicleId": 456,
    "requesterId": 12345,
    "currentOwnerId": 67890,
    "reason": "Vehicle purchase",
    "status": "PENDING",
    "requestDate": "2024-01-20T10:30:00",
    "responseDate": null,
    "responseNotes": null,
    "createdAt": "2024-01-20T10:30:00",
    "updatedAt": "2024-01-20T10:30:00"
  }
}
```

**Error Response (404):**
```json
{
  "status": "error",
  "code": "404",
  "message": "Ownership claim not found",
  "body": null
}
```

---

### 5. Approve Ownership Claim

**Approve a claim and transfer vehicle ownership (current owner only)**

```http
PUT /ownership-claims/{claimId}/approve
```

**Headers:**
```http
Content-Type: application/json
X-Auto-Adda-Id: <current_owner_user_id>
```

**Path Parameters:**
- `claimId` (Long) - The ID of the claim to approve

**Request Body (Optional):**
```json
{
  "notes": "Sale confirmed. Transfer approved."
}
```

**Success Response (200):**
```json
{
  "status": "success",
  "code": "200",
  "message": "Ownership claim approved. Vehicle ownership transferred successfully.",
  "body": {
    "id": 1,
    "chassisNo": "ABC123XYZ789",
    "vehicleId": 456,
    "requesterId": 12345,
    "currentOwnerId": 67890,
    "reason": "I purchased this vehicle",
    "status": "APPROVED",
    "requestDate": "2024-01-20T10:30:00",
    "responseDate": "2024-01-21T15:45:00",
    "responseNotes": "Sale confirmed. Transfer approved.",
    "createdAt": "2024-01-20T10:30:00",
    "updatedAt": "2024-01-21T15:45:00"
  }
}
```

**Error Responses:**

- **404 - Claim Not Found:**
```json
{
  "status": "error",
  "code": "404",
  "message": "Ownership claim not found",
  "body": null
}
```

- **403 - Unauthorized:**
```json
{
  "status": "error",
  "code": "403",
  "message": "You are not authorized to approve this claim",
  "body": null
}
```

- **400 - Already Processed:**
```json
{
  "status": "error",
  "code": "400",
  "message": "Claim has already been approved",
  "body": null
}
```

---

### 6. Reject Ownership Claim

**Reject a claim (current owner only)**

```http
PUT /ownership-claims/{claimId}/reject
```

**Headers:**
```http
Content-Type: application/json
X-Auto-Adda-Id: <current_owner_user_id>
```

**Path Parameters:**
- `claimId` (Long) - The ID of the claim to reject

**Request Body:**
```json
{
  "notes": "Vehicle is not for sale. Request denied."
}
```

**Success Response (200):**
```json
{
  "status": "success",
  "code": "200",
  "message": "Ownership claim rejected",
  "body": {
    "id": 1,
    "chassisNo": "ABC123XYZ789",
    "vehicleId": 456,
    "requesterId": 12345,
    "currentOwnerId": 67890,
    "reason": "I purchased this vehicle",
    "status": "REJECTED",
    "requestDate": "2024-01-20T10:30:00",
    "responseDate": "2024-01-21T11:20:00",
    "responseNotes": "Vehicle is not for sale. Request denied.",
    "createdAt": "2024-01-20T10:30:00",
    "updatedAt": "2024-01-21T11:20:00"
  }
}
```

---

### 7. Cancel Ownership Claim

**Cancel a pending claim (requester only)**

```http
PUT /ownership-claims/{claimId}/cancel
```

**Headers:**
```http
X-Auto-Adda-Id: <requester_user_id>
```

**Path Parameters:**
- `claimId` (Long) - The ID of the claim to cancel

**Success Response (200):**
```json
{
  "status": "success",
  "code": "200",
  "message": "Ownership claim cancelled successfully",
  "body": null
}
```

**Error Responses:**

- **403 - Unauthorized:**
```json
{
  "status": "error",
  "code": "403",
  "message": "You are not authorized to cancel this claim",
  "body": null
}
```

- **400 - Cannot Cancel:**
```json
{
  "status": "error",
  "code": "400",
  "message": "Only pending claims can be cancelled",
  "body": null
}
```

---

### 8. Get Vehicle Claim History

**Get all ownership claims (past and present) for a specific vehicle**

```http
GET /ownership-claims/vehicle/{vehicleId}/history
```

**Path Parameters:**
- `vehicleId` (Integer) - The ID of the vehicle

**Success Response (200):**
```json
{
  "status": "success",
  "code": "200",
  "message": "Claim history retrieved successfully",
  "body": [
    {
      "id": 5,
      "chassisNo": "ABC123XYZ789",
      "vehicleId": 456,
      "requesterId": 12345,
      "currentOwnerId": 67890,
      "reason": "Vehicle purchase",
      "status": "APPROVED",
      "requestDate": "2024-01-20T10:30:00",
      "responseDate": "2024-01-21T15:45:00",
      "responseNotes": "Sale approved",
      "createdAt": "2024-01-20T10:30:00",
      "updatedAt": "2024-01-21T15:45:00"
    },
    {
      "id": 3,
      "chassisNo": "ABC123XYZ789",
      "vehicleId": 456,
      "requesterId": 99999,
      "currentOwnerId": 67890,
      "reason": "Transfer request",
      "status": "REJECTED",
      "requestDate": "2024-01-10T08:00:00",
      "responseDate": "2024-01-11T10:00:00",
      "responseNotes": "Declined",
      "createdAt": "2024-01-10T08:00:00",
      "updatedAt": "2024-01-11T10:00:00"
    }
  ]
}
```

---

## Request/Response Models

### ClaimStatus Enum

```javascript
/**
 * Claim status values
 * @typedef {('PENDING'|'APPROVED'|'REJECTED'|'CANCELLED')} ClaimStatus
 */
const ClaimStatus = {
  PENDING: "PENDING",      // Awaiting owner approval
  APPROVED: "APPROVED",    // Owner approved, ownership transferred
  REJECTED: "REJECTED",    // Owner rejected the claim
  CANCELLED: "CANCELLED"   // Requester cancelled the claim
};
```

### OwnershipClaimRequest

```javascript
/**
 * Request object for creating ownership claim
 * @typedef {Object} OwnershipClaimRequest
 * @property {string} chassisNo - Required: Vehicle chassis number
 * @property {string} [reason] - Optional: Reason for ownership request
 */
```

### ClaimActionRequest

```javascript
/**
 * Request object for approve/reject actions
 * @typedef {Object} ClaimActionRequest
 * @property {string} [notes] - Optional: Owner's notes when approving/rejecting
 */
```

### OwnershipClaimResponse

```javascript
/**
 * Response object for ownership claim
 * @typedef {Object} OwnershipClaimResponse
 * @property {number} id
 * @property {string} chassisNo
 * @property {number} vehicleId
 * @property {number} requesterId - User Information
 * @property {string|null} requesterName
 * @property {number} currentOwnerId
 * @property {string|null} currentOwnerName
 * @property {string} reason - Claim Details
 * @property {ClaimStatus} status
 * @property {string|null} vehicleBrand - Vehicle Info (optional)
 * @property {string|null} vehicleModel
 * @property {string|null} vehicleYear
 * @property {string} requestDate - ISO 8601 format
 * @property {string|null} responseDate
 * @property {string|null} responseNotes
 * @property {string} createdAt
 * @property {string} updatedAt
 */
```

### GenericResponse

```javascript
/**
 * Generic API response wrapper
 * @typedef {Object} GenericResponse
 * @property {('success'|'error')} status
 * @property {string} code - HTTP status code as string
 * @property {string} message
 * @property {*} body - Response body (can be any type or null)
 */
```

---

## User Flows

### Flow 1: Request Ownership (Buyer Perspective)

```
1. User searches for vehicle by chassis number
2. User clicks "Request Ownership"
3. Frontend calls: POST /ownership-claims/request
   - Headers: X-Auto-Adda-Id: <buyer_id>
   - Body: { chassisNo: "...", reason: "..." }
4. Backend validates:
   - Vehicle exists
   - User is not already owner
   - No pending claim exists
5. Backend creates claim with status PENDING
6. Frontend shows success: "Ownership request sent. Waiting for approval."
7. User can view their requests: GET /ownership-claims/my-requests
```

**UI Flow:**
```
┌─────────────────────────────┐
│  Search Vehicle by Chassis  │
│  [ABC123XYZ789]  [Search]   │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│   Vehicle Found             │
│   Brand: Toyota             │
│   Model: Camry              │
│   Owner: John Doe           │
│   [Request Ownership]       │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│   Request Ownership         │
│   Reason:                   │
│   [Text area for reason]    │
│   [Cancel]  [Submit]        │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│   ✓ Request Sent!           │
│   Your ownership request    │
│   has been sent to the      │
│   current owner.            │
└─────────────────────────────┘
```

---

### Flow 2: Approve/Reject Claim (Owner Perspective)

```
1. Owner receives notification of pending claim
2. Owner navigates to "Pending Claims" section
3. Frontend calls: GET /ownership-claims/pending
   - Headers: X-Auto-Adda-Id: <owner_id>
4. Frontend displays list of pending claims
5. Owner reviews claim details
6. Owner clicks "Approve" or "Reject"
7a. If Approve:
    - Frontend calls: PUT /ownership-claims/{id}/approve
    - Body: { notes: "..." }
    - Backend transfers ownership
    - Frontend shows: "Ownership transferred successfully"
8a. If Reject:
    - Frontend calls: PUT /ownership-claims/{id}/reject
    - Body: { notes: "..." }
    - Backend updates claim status
    - Frontend shows: "Claim rejected"
```

**UI Flow:**
```
┌─────────────────────────────┐
│   Pending Ownership Claims  │
│                             │
│   ┌───────────────────────┐ │
│   │ Claim #1              │ │
│   │ From: Jane Smith      │ │
│   │ Vehicle: Toyota Camry │ │
│   │ Chassis: ABC123XYZ789 │ │
│   │ Reason: I purchased...│ │
│   │ Date: 2024-01-20      │ │
│   │ [View] [Approve] [X]  │ │
│   └───────────────────────┘ │
└─────────────────────────────┘
               │
               ▼ (Approve clicked)
┌─────────────────────────────┐
│   Approve Claim             │
│   Vehicle: Toyota Camry     │
│   Requester: Jane Smith     │
│                             │
│   Notes (optional):         │
│   [Text area]               │
│                             │
│   [Cancel]  [Confirm]       │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│   ✓ Ownership Transferred   │
│   The vehicle has been      │
│   transferred to Jane Smith │
└─────────────────────────────┘
```

---

### Flow 3: Cancel Request (Requester Perspective)

```
1. User views their requests: GET /ownership-claims/my-requests
2. User finds a PENDING claim they want to cancel
3. User clicks "Cancel Request"
4. Frontend calls: PUT /ownership-claims/{id}/cancel
   - Headers: X-Auto-Adda-Id: <requester_id>
5. Backend validates requester and updates status to CANCELLED
6. Frontend shows: "Request cancelled successfully"
```

---

## Code Examples

### React/JavaScript Example

```javascript
// services/ownershipClaimService.js
import axios from 'axios';

const API_BASE_URL = 'http://your-api-domain.com';

/**
 * Create ownership claim
 * @param {number} userId - The user ID
 * @param {string} chassisNo - The chassis number
 * @param {string} reason - Reason for claim
 * @returns {Promise<GenericResponse>}
 */
export const createOwnershipClaim = async (userId, chassisNo, reason) => {
  const response = await axios.post(
    `${API_BASE_URL}/ownership-claims/request`,
    { chassisNo, reason },
    {
      headers: {
        'X-Auto-Adda-Id': userId.toString(),
        'Content-Type': 'application/json'
      }
    }
  );
  return response.data;
};

/**
 * Get my requests
 * @param {number} userId - The user ID
 * @returns {Promise<GenericResponse>}
 */
export const getMyRequests = async (userId) => {
  const response = await axios.get(
    `${API_BASE_URL}/ownership-claims/my-requests`,
    {
      headers: {
        'X-Auto-Adda-Id': userId.toString()
      }
    }
  );
  return response.data;
};

/**
 * Get pending claims (for owners)
 * @param {number} userId - The user ID
 * @returns {Promise<GenericResponse>}
 */
export const getPendingClaims = async (userId) => {
  const response = await axios.get(
    `${API_BASE_URL}/ownership-claims/pending`,
    {
      headers: {
        'X-Auto-Adda-Id': userId.toString()
      }
    }
  );
  return response.data;
};

/**
 * Approve claim
 * @param {number} userId - The user ID
 * @param {number} claimId - The claim ID
 * @param {string} [notes] - Optional notes
 * @returns {Promise<GenericResponse>}
 */
export const approveClaim = async (userId, claimId, notes) => {
  const response = await axios.put(
    `${API_BASE_URL}/ownership-claims/${claimId}/approve`,
    { notes },
    {
      headers: {
        'X-Auto-Adda-Id': userId.toString(),
        'Content-Type': 'application/json'
      }
    }
  );
  return response.data;
};

/**
 * Reject claim
 * @param {number} userId - The user ID
 * @param {number} claimId - The claim ID
 * @param {string} notes - Rejection notes
 * @returns {Promise<GenericResponse>}
 */
export const rejectClaim = async (userId, claimId, notes) => {
  const response = await axios.put(
    `${API_BASE_URL}/ownership-claims/${claimId}/reject`,
    { notes },
    {
      headers: {
        'X-Auto-Adda-Id': userId.toString(),
        'Content-Type': 'application/json'
      }
    }
  );
  return response.data;
};

/**
 * Cancel claim
 * @param {number} userId - The user ID
 * @param {number} claimId - The claim ID
 * @returns {Promise<GenericResponse>}
 */
export const cancelClaim = async (userId, claimId) => {
  const response = await axios.put(
    `${API_BASE_URL}/ownership-claims/${claimId}/cancel`,
    {},
    {
      headers: {
        'X-Auto-Adda-Id': userId.toString()
      }
    }
  );
  return response.data;
};

/**
 * Get claim history for vehicle
 * @param {number} vehicleId - The vehicle ID
 * @returns {Promise<GenericResponse>}
 */
export const getVehicleClaimHistory = async (vehicleId) => {
  const response = await axios.get(
    `${API_BASE_URL}/ownership-claims/vehicle/${vehicleId}/history`
  );
  return response.data;
};
```

### React Component Example

```javascript
// components/RequestOwnership.jsx
import React, { useState } from 'react';
import { createOwnershipClaim } from '../services/ownershipClaimService';

/**
 * RequestOwnership Component
 * @param {Object} props
 * @param {number} props.userId - The user ID
 * @param {string} props.chassisNo - The chassis number
 * @param {Function} props.onSuccess - Success callback
 */
export const RequestOwnership = ({ userId, chassisNo, onSuccess }) => {
  const [reason, setReason] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const response = await createOwnershipClaim(userId, chassisNo, reason);

      if (response.status === 'success') {
        alert('Ownership request sent successfully!');
        onSuccess();
      } else {
        setError(response.message);
      }
    } catch (err) {
      if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else {
        setError('An error occurred while sending the request');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="request-ownership-form">
      <h2>Request Vehicle Ownership</h2>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Chassis Number:</label>
          <input type="text" value={chassisNo} disabled />
        </div>

        <div className="form-group">
          <label>Reason for Request:</label>
          <textarea
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            placeholder="Please provide a reason for this ownership request..."
            rows={4}
            required
          />
        </div>

        {error && <div className="error-message">{error}</div>}

        <div className="form-actions">
          <button type="submit" disabled={loading}>
            {loading ? 'Sending...' : 'Submit Request'}
          </button>
        </div>
      </form>
    </div>
  );
};
```

### Pending Claims Component Example

```javascript
// components/PendingClaims.jsx
import React, { useEffect, useState } from 'react';
import { getPendingClaims, approveClaim, rejectClaim } from '../services/ownershipClaimService';

/**
 * PendingClaims Component
 * @param {Object} props
 * @param {number} props.userId - The user ID
 */
export const PendingClaims = ({ userId }) => {
  const [claims, setClaims] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadClaims();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [userId]);

  const loadClaims = async () => {
    try {
      const response = await getPendingClaims(userId);
      if (response.status === 'success') {
        setClaims(response.body || []);
      }
    } catch (err) {
      console.error('Error loading claims:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = async (claimId) => {
    const notes = prompt('Enter notes for approval (optional):');

    try {
      const response = await approveClaim(userId, claimId, notes || undefined);
      if (response.status === 'success') {
        alert('Claim approved! Ownership transferred.');
        loadClaims(); // Reload claims
      }
    } catch (err) {
      alert(err.response?.data?.message || 'Error approving claim');
    }
  };

  const handleReject = async (claimId) => {
    const notes = prompt('Enter reason for rejection:');
    if (!notes) return;

    try {
      const response = await rejectClaim(userId, claimId, notes);
      if (response.status === 'success') {
        alert('Claim rejected');
        loadClaims(); // Reload claims
      }
    } catch (err) {
      alert(err.response?.data?.message || 'Error rejecting claim');
    }
  };

  if (loading) return <div>Loading...</div>;

  return (
    <div className="pending-claims">
      <h2>Pending Ownership Claims</h2>

      {claims.length === 0 ? (
        <p>No pending claims</p>
      ) : (
        <div className="claims-list">
          {claims.map((claim) => (
            <div key={claim.id} className="claim-card">
              <div className="claim-header">
                <h3>Claim #{claim.id}</h3>
                <span className="claim-status">{claim.status}</span>
              </div>

              <div className="claim-details">
                <p><strong>Chassis:</strong> {claim.chassisNo}</p>
                <p><strong>Requester ID:</strong> {claim.requesterId}</p>
                <p><strong>Reason:</strong> {claim.reason}</p>
                <p><strong>Request Date:</strong> {new Date(claim.requestDate).toLocaleDateString()}</p>
              </div>

              <div className="claim-actions">
                <button
                  className="btn-approve"
                  onClick={() => handleApprove(claim.id)}
                >
                  Approve
                </button>
                <button
                  className="btn-reject"
                  onClick={() => handleReject(claim.id)}
                >
                  Reject
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
```

---

## Error Handling

### HTTP Status Codes

| Code | Meaning | When It Occurs |
|------|---------|----------------|
| 200 | Success | Request processed successfully |
| 201 | Created | Claim created successfully |
| 400 | Bad Request | Invalid data, duplicate claim, or business rule violation |
| 403 | Forbidden | User not authorized to perform action |
| 404 | Not Found | Claim or vehicle not found |
| 500 | Server Error | Internal server error |

### Error Response Structure

All errors follow this format:
```json
{
  "status": "error",
  "code": "400",
  "message": "Human-readable error message",
  "body": null
}
```

### Common Error Scenarios

**1. Vehicle Not Found**
```json
// When: Chassis number doesn't exist
{
  "status": "error",
  "code": "404",
  "message": "Vehicle with chassis number ABC123XYZ789 not found"
}
```

**2. Already Owner**
```json
// When: User tries to claim vehicle they already own
{
  "status": "error",
  "code": "400",
  "message": "You already own this vehicle"
}
```

**3. Duplicate Claim**
```json
// When: User has pending claim for same vehicle
{
  "status": "error",
  "code": "400",
  "message": "You already have a pending claim for this vehicle"
}
```

**4. Unauthorized Action**
```json
// When: Non-owner tries to approve/reject
{
  "status": "error",
  "code": "403",
  "message": "You are not authorized to approve this claim"
}
```

**5. Already Processed**
```json
// When: Trying to approve/reject non-pending claim
{
  "status": "error",
  "code": "400",
  "message": "Claim has already been approved"
}
```

### Frontend Error Handling Pattern

```javascript
try {
  const response = await createOwnershipClaim(userId, chassisNo, reason);

  if (response.status === 'success') {
    // Handle success
    showSuccessMessage(response.message);
  } else {
    // Handle error response
    showErrorMessage(response.message);
  }
} catch (error) {
  if (axios.isAxiosError(error)) {
    if (error.response) {
      // Server responded with error
      const errorData = error.response.data;
      showErrorMessage(errorData.message || 'An error occurred');
    } else if (error.request) {
      // No response received
      showErrorMessage('Network error. Please check your connection.');
    } else {
      // Request setup error
      showErrorMessage('An unexpected error occurred');
    }
  }
}
```

---

## UI/UX Recommendations

### 1. Request Ownership Page

**Key Elements:**
- Vehicle information display (brand, model, year, chassis)
- Current owner information (if available)
- Reason text area (required)
- Clear submit button
- Success/error messages

**Best Practices:**
- Pre-fill chassis number if coming from vehicle details page
- Show vehicle photo/details for confirmation
- Validate chassis number format before submission
- Show loading state during API call
- Display clear success message with next steps

---

### 2. My Requests Page

**Key Elements:**
- List of all claims made by user
- Filter by status (All, Pending, Approved, Rejected, Cancelled)
- Each claim shows:
  - Chassis number
  - Vehicle details
  - Status badge with color coding
  - Request date
  - Response date (if processed)
  - Response notes (if any)
- Cancel button for pending claims

**Status Color Coding:**
- PENDING: Yellow/Orange
- APPROVED: Green
- REJECTED: Red
- CANCELLED: Gray

---

### 3. Pending Claims Page (For Owners)

**Key Elements:**
- List of pending claims for owner's vehicles
- Each claim shows:
  - Requester information
  - Vehicle details
  - Reason for request
  - Request date
  - Approve/Reject buttons
- Modal/dialog for approve/reject with notes field

**Best Practices:**
- Show notification badge for pending claims count
- Send push notifications when new claim arrives
- Require confirmation before approve/reject
- Show vehicle details prominently
- Allow owners to view requester profile (if available)

---

### 4. Vehicle Details Page Enhancement

**Add Claim History Section:**
- Show all ownership claims for vehicle
- Display transfer history
- Show dates of ownership changes
- Visual timeline of ownership

---

### 5. Notifications

**Trigger notifications for:**
- New claim received (owner)
- Claim approved (requester)
- Claim rejected (requester)
- Claim cancelled (owner)

**Notification Methods:**
- In-app notifications
- Push notifications
- Email notifications
- SMS (optional)

---

## Security Considerations

1. **User ID Validation:**
   - Always validate `X-Auto-Adda-Id` header on backend
   - Never trust client-side user ID
   - Implement proper authentication middleware

2. **Authorization Checks:**
   - Approve/Reject: Verify user is current owner
   - Cancel: Verify user is requester
   - Prevent unauthorized access

3. **Rate Limiting:**
   - Limit claim creation to prevent spam
   - Implement cooldown period between claims

4. **Input Validation:**
   - Validate chassis number format
   - Sanitize reason and notes text
   - Prevent XSS attacks

---

## Testing Checklist

### Happy Path Testing
- [ ] Create ownership claim successfully
- [ ] View my requests
- [ ] View pending claims
- [ ] Approve claim successfully
- [ ] Reject claim successfully
- [ ] Cancel claim successfully
- [ ] View vehicle claim history

### Error Scenarios
- [ ] Create claim for non-existent chassis
- [ ] Create claim for own vehicle
- [ ] Create duplicate pending claim
- [ ] Approve claim as non-owner
- [ ] Reject already processed claim
- [ ] Cancel already processed claim

### Edge Cases
- [ ] Empty reason field
- [ ] Very long reason text
- [ ] Special characters in chassis number
- [ ] Invalid user ID in header
- [ ] Network timeout handling
- [ ] Concurrent claim approvals

---

## FAQ

**Q: Can multiple users claim the same vehicle simultaneously?**
A: Yes, multiple users can create pending claims, but only the current owner can approve them.

**Q: What happens when a claim is approved?**
A: The vehicle's `customerId` is updated to the requester's ID, and the previous owner's ID is stored in `previousOwnerId`.

**Q: Can a user cancel an approved claim?**
A: No, only pending claims can be cancelled.

**Q: Are claim IDs globally unique?**
A: Yes, claim IDs are auto-generated and unique across the system.

**Q: What if the chassis number doesn't exist?**
A: The API returns a 404 error with message "Vehicle with chassis number X not found".

**Q: Can owners see who requested ownership?**
A: Yes, the claim response includes `requesterId`. You can fetch user details from your identity service.

---

## Support

For technical issues or questions:
- Backend API Documentation: [Your API Docs URL]
- Support Email: support@autoadda.com
- Developer Portal: [Your Developer Portal URL]

---

**Document Version:** 1.0
**Last Updated:** 2025-01-02
**Maintained By:** AutoAdda Development Team
