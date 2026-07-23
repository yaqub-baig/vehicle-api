# Vehicle Borrowing - Frontend Integration Guide

## Overview

This guide covers the integration of the Vehicle Borrowing feature into the AutoAdda frontend application using React (JSX/JS).

## Base Configuration

```javascript
// src/config/api.js
const API_BASE_URL = 'http://localhost:8000/api/v1';

export const getHeaders = (userId) => ({
  'Content-Type': 'application/json',
  'X-Auto-Adda-Id': String(userId)
});

export default API_BASE_URL;
```

---

## API Service

```javascript
// src/services/vehicleBorrowingService.js
import API_BASE_URL, { getHeaders } from '../config/api';

const BORROWING_URL = `${API_BASE_URL}/vehicle-borrowings`;

// ============================================
// BORROWER ACTIONS
// ============================================

// Create a borrowing request (Borrower)
export const createBorrowingRequest = async (userId, data) => {
  const response = await fetch(BORROWING_URL, {
    method: 'POST',
    headers: getHeaders(userId),
    body: JSON.stringify({
      vehicleId: data.vehicleId,
      expectedReturnTime: data.expectedReturnTime,
      borrowingImageId: data.borrowingImageId || null,
      borrowingPurpose: data.borrowingPurpose || null
    })
  });
  return response.json();
};

// Start borrowing - pick up vehicle (Borrower)
export const startBorrowing = async (userId, borrowingId) => {
  const response = await fetch(`${BORROWING_URL}/${borrowingId}/start`, {
    method: 'PUT',
    headers: getHeaders(userId)
  });
  return response.json();
};

// Initiate return (Borrower)
export const initiateReturn = async (userId, borrowingId, data = {}) => {
  const response = await fetch(`${BORROWING_URL}/${borrowingId}/return`, {
    method: 'PUT',
    headers: getHeaders(userId),
    body: JSON.stringify({
      returnImageId: data.returnImageId || null,
      returnNotes: data.returnNotes || null,
      keyReturned: data.keyReturned ?? null
    })
  });
  return response.json();
};

// Cancel borrowing request (Borrower)
export const cancelBorrowingRequest = async (userId, borrowingId) => {
  const response = await fetch(`${BORROWING_URL}/${borrowingId}/cancel`, {
    method: 'PUT',
    headers: getHeaders(userId)
  });
  return response.json();
};

// Get my borrowings (Borrower)
export const getMyBorrowings = async (userId) => {
  const response = await fetch(`${BORROWING_URL}/my-borrowings`, {
    method: 'GET',
    headers: getHeaders(userId)
  });
  return response.json();
};

// ============================================
// OWNER ACTIONS
// ============================================

// Approve borrowing request (Owner)
export const approveBorrowingRequest = async (userId, borrowingId) => {
  const response = await fetch(`${BORROWING_URL}/${borrowingId}/approve`, {
    method: 'PUT',
    headers: getHeaders(userId)
  });
  return response.json();
};

// Reject borrowing request (Owner)
export const rejectBorrowingRequest = async (userId, borrowingId, reason = null) => {
  const response = await fetch(`${BORROWING_URL}/${borrowingId}/reject`, {
    method: 'PUT',
    headers: getHeaders(userId),
    body: JSON.stringify({
      rejectionReason: reason
    })
  });
  return response.json();
};

// Owner assigns vehicle directly to user (skips approval)
export const assignVehicleToBorrower = async (userId, data) => {
  const response = await fetch(`${BORROWING_URL}/assign`, {
    method: 'POST',
    headers: getHeaders(userId),
    body: JSON.stringify({
      vehicleId: data.vehicleId,
      borrowerId: data.borrowerId,
      expectedReturnTime: data.expectedReturnTime,
      borrowingImageId: data.borrowingImageId || null,
      borrowingPurpose: data.borrowingPurpose || null
    })
  });
  return response.json();
};

// Get pending requests for my vehicles (Owner)
export const getPendingBorrowings = async (userId) => {
  const response = await fetch(`${BORROWING_URL}/pending`, {
    method: 'GET',
    headers: getHeaders(userId)
  });
  return response.json();
};

// Get all borrowings of my vehicles (Owner)
export const getMyVehicleBorrowings = async (userId) => {
  const response = await fetch(`${BORROWING_URL}/my-vehicles`, {
    method: 'GET',
    headers: getHeaders(userId)
  });
  return response.json();
};

// Search user by AutoAdda ID (for owner to find borrower)
export const searchUserById = async (autoAddaId) => {
  const response = await fetch(`${BORROWING_URL}/search-user?userId=${autoAddaId}`, {
    method: 'GET',
    headers: { 'Content-Type': 'application/json' }
  });
  return response.json();
};

// ============================================
// QUICK ACTIONS (Fast Path — Minimal Input)
// ============================================
// These bypass the multi-step approve/start and return/confirm-return
// handshakes entirely. Use them for trusted owner<->borrower relationships
// where the formal approval dance is unnecessary friction. They are
// opt-in, additive endpoints — none of the existing slow-path endpoints
// above changed behavior.

// Quick-borrow: goes straight to ACTIVE in a single call.
// - Called by the vehicle OWNER: pass data.borrowerId to assign to that user
//   (no approval wait, no separate /start call).
// - Called by anyone else: borrowerId is ignored server-side and forced to
//   the caller — this is a self-request that is auto-approved, but ONLY if
//   the owner has already added the caller to their trusted borrower list
//   (see TRUSTED BORROWER ACTIONS below). Otherwise the response is a 403:
//   "You are not a trusted borrower for this owner's vehicles. Ask the
//   owner to add you as a trusted borrower first."
// expectedReturnTime is optional; the server defaults it to now + 24h.
export const quickBorrow = async (userId, data) => {
  const response = await fetch(`${BORROWING_URL}/quick-borrow`, {
    method: 'POST',
    headers: getHeaders(userId),
    body: JSON.stringify({
      vehicleId: data.vehicleId,
      borrowerId: data.borrowerId || null,
      expectedReturnTime: data.expectedReturnTime || null,
      borrowingImageIds: data.borrowingImageIds || null,
      borrowingPurpose: data.borrowingPurpose || null
    })
  });
  return response.json();
};

// Quick-return: closes out the borrowing in one call — no second-party
// confirmation needed. Works from ACTIVE, and also "rescues" a borrowing
// already stuck in RETURN_INITIATED waiting on the slow-path owner
// confirmation.
// - Called by the OWNER: always allowed (they own the vehicle and already
//   have full authority over it — no trust check).
// - Called by the BORROWER: only allowed if the owner has that borrower on
//   their trusted borrower list (see TRUSTED BORROWER ACTIONS below).
//   Otherwise 403: "You are not a trusted borrower for this owner's
//   vehicles. Ask the owner to add you as a trusted borrower, or use the
//   standard return + confirm-return flow instead." A borrower who isn't
//   trusted can still fall back to initiateReturn() + wait for the owner's
//   confirmReturn().
export const quickReturn = async (userId, borrowingId, data = {}) => {
  const response = await fetch(`${BORROWING_URL}/${borrowingId}/quick-return`, {
    method: 'PUT',
    headers: getHeaders(userId),
    body: JSON.stringify({
      returnImageIds: data.returnImageIds || null,
      returnNotes: data.returnNotes || null,
      keyReturned: data.keyReturned ?? null
    })
  });
  return response.json();
};

// ============================================
// TRUSTED BORROWER ACTIONS (Owner manages who can self-quick-borrow)
// ============================================
// A trusted borrower can quick-borrow ANY of the owner's vehicles without
// the owner approving each request. This is per-OWNER, not per-vehicle —
// adding someone once covers their whole fleet. Owner-initiated quick-borrow
// (owner picks the borrowerId themselves) never needs this list; it's only
// checked for the self-request branch of quick-borrow.
const TRUSTED_BORROWERS_URL = `${API_BASE_URL}/trusted-borrowers`;

// Add a user to the caller's (owner's) trusted borrower list
export const addTrustedBorrower = async (ownerId, borrowerId) => {
  const response = await fetch(TRUSTED_BORROWERS_URL, {
    method: 'POST',
    headers: getHeaders(ownerId),
    body: JSON.stringify({ borrowerId })
  });
  return response.json();
};

// Remove a user from the caller's (owner's) trusted borrower list
export const removeTrustedBorrower = async (ownerId, borrowerId) => {
  const response = await fetch(`${TRUSTED_BORROWERS_URL}/${borrowerId}`, {
    method: 'DELETE',
    headers: getHeaders(ownerId)
  });
  return response.json();
};

// List the caller's (owner's) trusted borrowers
export const getTrustedBorrowers = async (ownerId) => {
  const response = await fetch(TRUSTED_BORROWERS_URL, {
    method: 'GET',
    headers: getHeaders(ownerId)
  });
  return response.json();
};

// ============================================
// SHARED ACTIONS
// ============================================

// Confirm return (Both Owner and Borrower)
// When called by the owner, pass keyReturned to record the owner's own
// observation of whether the key was physically handed back — independent
// of whatever the borrower claimed at /return.
export const confirmReturn = async (userId, borrowingId, keyReturned = null) => {
  const response = await fetch(`${BORROWING_URL}/${borrowingId}/confirm-return`, {
    method: 'PUT',
    headers: getHeaders(userId),
    body: JSON.stringify({
      keyReturned
    })
  });
  return response.json();
};

// Reject a pending return (Owner only)
// Reverts the borrowing to ACTIVE — the borrower keeps the vehicle and
// must coordinate/re-initiate the return later. keyReturned here is the
// owner's own observation, recorded even though the return is rejected.
export const rejectReturn = async (userId, borrowingId, reason = null, keyReturned = null) => {
  const response = await fetch(`${BORROWING_URL}/${borrowingId}/reject-return`, {
    method: 'PUT',
    headers: getHeaders(userId),
    body: JSON.stringify({
      rejectionReason: reason,
      keyReturned
    })
  });
  return response.json();
};

// Get borrowing by ID
export const getBorrowingById = async (borrowingId) => {
  const response = await fetch(`${BORROWING_URL}/${borrowingId}`, {
    method: 'GET',
    headers: { 'Content-Type': 'application/json' }
  });
  return response.json();
};

// Get borrowings by vehicle
export const getBorrowingsByVehicle = async (vehicleId) => {
  const response = await fetch(`${BORROWING_URL}/vehicle/${vehicleId}`, {
    method: 'GET',
    headers: { 'Content-Type': 'application/json' }
  });
  return response.json();
};

// Check vehicle availability
export const checkVehicleAvailability = async (vehicleId) => {
  const response = await fetch(`${BORROWING_URL}/vehicle/${vehicleId}/available`, {
    method: 'GET',
    headers: { 'Content-Type': 'application/json' }
  });
  return response.json();
};

// Get active borrowing for vehicle
export const getActiveBorrowingForVehicle = async (vehicleId) => {
  const response = await fetch(`${BORROWING_URL}/vehicle/${vehicleId}/active`, {
    method: 'GET',
    headers: { 'Content-Type': 'application/json' }
  });
  return response.json();
};

// ============================================
// TRANSFER ACTIONS (Borrower → Borrower Handover)
// ============================================

// Current borrower proposes handover to another user
// Transfers are notify-only for the owner (no owner approval required).
// Unlimited chain hops are allowed; expectedReturnTime is inherited.
export const initiateTransfer = async (userId, borrowingId, data) => {
  const response = await fetch(`${BORROWING_URL}/${borrowingId}/transfer`, {
    method: 'POST',
    headers: getHeaders(userId),
    body: JSON.stringify({
      toBorrowerId: data.toBorrowerId,
      reason: data.reason || null,
      notes: data.notes || null,
      transferImageId: data.transferImageId || null
    })
  });
  return response.json();
};

// Recipient accepts a pending transfer — rotates borrowerId to recipient
export const acceptTransfer = async (userId, borrowingId, transferId) => {
  const response = await fetch(
    `${BORROWING_URL}/${borrowingId}/transfers/${transferId}/accept`,
    {
      method: 'PUT',
      headers: getHeaders(userId)
    }
  );
  return response.json();
};

// Recipient rejects a pending transfer — borrowing stays with previous borrower
export const rejectTransfer = async (userId, borrowingId, transferId, reason = null) => {
  const response = await fetch(
    `${BORROWING_URL}/${borrowingId}/transfers/${transferId}/reject`,
    {
      method: 'PUT',
      headers: getHeaders(userId),
      body: JSON.stringify({ reason })
    }
  );
  return response.json();
};

// Requesting borrower cancels a pending transfer
export const cancelTransfer = async (userId, borrowingId, transferId) => {
  const response = await fetch(
    `${BORROWING_URL}/${borrowingId}/transfers/${transferId}/cancel`,
    {
      method: 'PUT',
      headers: getHeaders(userId)
    }
  );
  return response.json();
};

// Full transfer history for a borrowing (chain of custody)
export const getTransfersForBorrowing = async (borrowingId) => {
  const response = await fetch(`${BORROWING_URL}/${borrowingId}/transfers`, {
    method: 'GET',
    headers: { 'Content-Type': 'application/json' }
  });
  return response.json();
};
```

---

## Borrowing Status Constants

```javascript
// src/constants/borrowingStatus.js
export const BORROWING_STATUS = {
  PENDING: 'PENDING',
  APPROVED: 'APPROVED',
  ACTIVE: 'ACTIVE',
  TRANSFER_REQUESTED: 'TRANSFER_REQUESTED',
  RETURN_INITIATED: 'RETURN_INITIATED',
  RETURNED: 'RETURNED',
  REJECTED: 'REJECTED',
  CANCELLED: 'CANCELLED'
};

export const STATUS_LABELS = {
  PENDING: 'Pending Approval',
  APPROVED: 'Approved - Ready for Pickup',
  ACTIVE: 'Currently Borrowed',
  TRANSFER_REQUESTED: 'Transfer Pending',
  RETURN_INITIATED: 'Return in Progress',
  RETURNED: 'Returned',
  REJECTED: 'Rejected',
  CANCELLED: 'Cancelled'
};

export const STATUS_COLORS = {
  PENDING: '#FFA500',      // Orange
  APPROVED: '#4CAF50',     // Green
  ACTIVE: '#2196F3',       // Blue
  TRANSFER_REQUESTED: '#FF9800', // Amber
  RETURN_INITIATED: '#9C27B0', // Purple
  RETURNED: '#607D8B',     // Grey
  REJECTED: '#F44336',     // Red
  CANCELLED: '#9E9E9E'     // Grey
};

// Transfer sub-status (on BorrowingTransferResponse)
export const TRANSFER_STATUS = {
  PENDING: 'PENDING',
  ACCEPTED: 'ACCEPTED',
  REJECTED: 'REJECTED',
  CANCELLED: 'CANCELLED'
};

export const TRANSFER_STATUS_LABELS = {
  PENDING: 'Awaiting Recipient',
  ACCEPTED: 'Accepted',
  REJECTED: 'Declined',
  CANCELLED: 'Cancelled'
};
```

---

## React Components

### 1. Borrowing Request Form (Borrower)

```jsx
// src/components/borrowing/BorrowingRequestForm.jsx
import React, { useState } from 'react';
import { createBorrowingRequest } from '../../services/vehicleBorrowingService';

const BorrowingRequestForm = ({ vehicleId, userId, onSuccess, onCancel }) => {
  const [formData, setFormData] = useState({
    expectedReturnTime: '',
    borrowingPurpose: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const response = await createBorrowingRequest(userId, {
        vehicleId,
        expectedReturnTime: formData.expectedReturnTime,
        borrowingPurpose: formData.borrowingPurpose
      });

      if (response.status === 'success') {
        onSuccess(response.body);
      } else {
        setError(response.message);
      }
    } catch (err) {
      setError('Failed to create borrowing request');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="borrowing-request-form">
      <h3>Request to Borrow Vehicle</h3>

      {error && <div className="error-message">{error}</div>}

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="expectedReturnTime">Expected Return Date & Time *</label>
          <input
            type="datetime-local"
            id="expectedReturnTime"
            name="expectedReturnTime"
            value={formData.expectedReturnTime}
            onChange={handleChange}
            required
            min={new Date().toISOString().slice(0, 16)}
          />
        </div>

        <div className="form-group">
          <label htmlFor="borrowingPurpose">Purpose (Optional)</label>
          <textarea
            id="borrowingPurpose"
            name="borrowingPurpose"
            value={formData.borrowingPurpose}
            onChange={handleChange}
            placeholder="Why do you need to borrow this vehicle?"
            rows={3}
          />
        </div>

        <div className="form-actions">
          <button type="button" onClick={onCancel} disabled={loading}>
            Cancel
          </button>
          <button type="submit" disabled={loading}>
            {loading ? 'Submitting...' : 'Submit Request'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default BorrowingRequestForm;
```

### 2. Owner Assign Vehicle Form (Owner-Initiated)

```jsx
// src/components/borrowing/OwnerAssignVehicleForm.jsx
import React, { useState } from 'react';
import { searchUserById, assignVehicleToBorrower } from '../../services/vehicleBorrowingService';

const OwnerAssignVehicleForm = ({ vehicleId, ownerId, onSuccess, onCancel }) => {
  const [searchId, setSearchId] = useState('');
  const [foundUser, setFoundUser] = useState(null);
  const [formData, setFormData] = useState({
    expectedReturnTime: '',
    borrowingPurpose: ''
  });
  const [loading, setLoading] = useState(false);
  const [searching, setSearching] = useState(false);
  const [error, setError] = useState(null);

  const handleSearchUser = async () => {
    if (!searchId.trim()) {
      setError('Please enter an AutoAdda ID');
      return;
    }

    setSearching(true);
    setError(null);
    setFoundUser(null);

    try {
      const response = await searchUserById(searchId);
      if (response.status === 'success') {
        setFoundUser(response.body);
      } else {
        setError(response.message || 'User not found');
      }
    } catch (err) {
      setError('Failed to search user');
    } finally {
      setSearching(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleAssign = async (e) => {
    e.preventDefault();

    if (!foundUser) {
      setError('Please search and select a user first');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const response = await assignVehicleToBorrower(ownerId, {
        vehicleId,
        borrowerId: foundUser.id,
        expectedReturnTime: formData.expectedReturnTime,
        borrowingPurpose: formData.borrowingPurpose
      });

      if (response.status === 'success') {
        onSuccess(response.body);
      } else {
        setError(response.message);
      }
    } catch (err) {
      setError('Failed to assign vehicle');
    } finally {
      setLoading(false);
    }
  };

  const handleClearUser = () => {
    setFoundUser(null);
    setSearchId('');
  };

  return (
    <div className="owner-assign-form">
      <h3>Assign Vehicle to User</h3>

      {error && <div className="error-message">{error}</div>}

      {/* User Search Section */}
      <div className="search-section">
        <label>Search User by AutoAdda ID</label>
        <div className="search-input-group">
          <input
            type="number"
            value={searchId}
            onChange={(e) => setSearchId(e.target.value)}
            placeholder="Enter AutoAdda ID"
            disabled={foundUser !== null}
          />
          {!foundUser ? (
            <button type="button" onClick={handleSearchUser} disabled={searching}>
              {searching ? 'Searching...' : 'Search'}
            </button>
          ) : (
            <button type="button" onClick={handleClearUser}>
              Clear
            </button>
          )}
        </div>
      </div>

      {/* Found User Display */}
      {foundUser && (
        <div className="found-user-card">
          <h4>Selected User</h4>
          <p><strong>Name:</strong> {foundUser.name || `${foundUser.firstName} ${foundUser.lastName}`}</p>
          <p><strong>Email:</strong> {foundUser.email}</p>
          <p><strong>Mobile:</strong> {foundUser.mobileNumber}</p>
          <p><strong>ID:</strong> {foundUser.id}</p>
        </div>
      )}

      {/* Assignment Form */}
      {foundUser && (
        <form onSubmit={handleAssign}>
          <div className="form-group">
            <label htmlFor="expectedReturnTime">Expected Return Date & Time *</label>
            <input
              type="datetime-local"
              id="expectedReturnTime"
              name="expectedReturnTime"
              value={formData.expectedReturnTime}
              onChange={handleChange}
              required
              min={new Date().toISOString().slice(0, 16)}
            />
          </div>

          <div className="form-group">
            <label htmlFor="borrowingPurpose">Purpose / Notes (Optional)</label>
            <textarea
              id="borrowingPurpose"
              name="borrowingPurpose"
              value={formData.borrowingPurpose}
              onChange={handleChange}
              placeholder="Reason for lending the vehicle"
              rows={3}
            />
          </div>

          <div className="form-actions">
            <button type="button" onClick={onCancel} disabled={loading}>
              Cancel
            </button>
            <button type="submit" disabled={loading}>
              {loading ? 'Assigning...' : 'Assign Vehicle'}
            </button>
          </div>
        </form>
      )}
    </div>
  );
};

export default OwnerAssignVehicleForm;
```

### 3. Pending Requests List (Owner)

```jsx
// src/components/borrowing/PendingRequestsList.jsx
import React, { useState, useEffect } from 'react';
import {
  getPendingBorrowings,
  approveBorrowingRequest,
  rejectBorrowingRequest
} from '../../services/vehicleBorrowingService';

const PendingRequestsList = ({ userId }) => {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [actionLoading, setActionLoading] = useState(null);
  const [rejectModal, setRejectModal] = useState({ open: false, borrowingId: null });
  const [rejectionReason, setRejectionReason] = useState('');

  useEffect(() => {
    fetchPendingRequests();
  }, [userId]);

  const fetchPendingRequests = async () => {
    setLoading(true);
    try {
      const response = await getPendingBorrowings(userId);
      if (response.status === 'success') {
        setRequests(response.body || []);
      } else {
        setError(response.message);
      }
    } catch (err) {
      setError('Failed to fetch pending requests');
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = async (borrowingId) => {
    setActionLoading(borrowingId);
    try {
      const response = await approveBorrowingRequest(userId, borrowingId);
      if (response.status === 'success') {
        setRequests(prev => prev.filter(r => r.id !== borrowingId));
      } else {
        alert(response.message);
      }
    } catch (err) {
      alert('Failed to approve request');
    } finally {
      setActionLoading(null);
    }
  };

  const openRejectModal = (borrowingId) => {
    setRejectModal({ open: true, borrowingId });
    setRejectionReason('');
  };

  const handleReject = async () => {
    const { borrowingId } = rejectModal;
    setActionLoading(borrowingId);

    try {
      const response = await rejectBorrowingRequest(userId, borrowingId, rejectionReason);
      if (response.status === 'success') {
        setRequests(prev => prev.filter(r => r.id !== borrowingId));
        setRejectModal({ open: false, borrowingId: null });
      } else {
        alert(response.message);
      }
    } catch (err) {
      alert('Failed to reject request');
    } finally {
      setActionLoading(null);
    }
  };

  const formatDateTime = (dateString) => {
    return new Date(dateString).toLocaleString();
  };

  if (loading) return <div>Loading pending requests...</div>;
  if (error) return <div className="error">{error}</div>;
  if (requests.length === 0) return <div>No pending requests</div>;

  return (
    <div className="pending-requests-list">
      <h3>Pending Borrowing Requests</h3>

      {requests.map(request => (
        <div key={request.id} className="request-card">
          <div className="request-info">
            <h4>{request.vehicleInfo}</h4>
            <p><strong>Borrower:</strong> {request.borrowerName}</p>
            <p><strong>Purpose:</strong> {request.borrowingPurpose || 'Not specified'}</p>
            <p><strong>Expected Return:</strong> {formatDateTime(request.expectedReturnTime)}</p>
            <p><strong>Requested:</strong> {formatDateTime(request.createdAt)}</p>
          </div>

          <div className="request-actions">
            <button
              className="approve-btn"
              onClick={() => handleApprove(request.id)}
              disabled={actionLoading === request.id}
            >
              {actionLoading === request.id ? 'Processing...' : 'Approve'}
            </button>
            <button
              className="reject-btn"
              onClick={() => openRejectModal(request.id)}
              disabled={actionLoading === request.id}
            >
              Reject
            </button>
          </div>
        </div>
      ))}

      {/* Rejection Modal */}
      {rejectModal.open && (
        <div className="modal-overlay">
          <div className="modal">
            <h4>Reject Request</h4>
            <textarea
              value={rejectionReason}
              onChange={(e) => setRejectionReason(e.target.value)}
              placeholder="Reason for rejection (optional)"
              rows={3}
            />
            <div className="modal-actions">
              <button onClick={() => setRejectModal({ open: false, borrowingId: null })}>
                Cancel
              </button>
              <button onClick={handleReject} disabled={actionLoading}>
                {actionLoading ? 'Rejecting...' : 'Confirm Reject'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default PendingRequestsList;
```

### 4. My Borrowings List (Borrower)

```jsx
// src/components/borrowing/MyBorrowingsList.jsx
import React, { useState, useEffect } from 'react';
import {
  getMyBorrowings,
  startBorrowing,
  initiateReturn,
  confirmReturn,
  cancelBorrowingRequest
} from '../../services/vehicleBorrowingService';
import { BORROWING_STATUS, STATUS_LABELS, STATUS_COLORS } from '../../constants/borrowingStatus';

const MyBorrowingsList = ({ userId }) => {
  const [borrowings, setBorrowings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [actionLoading, setActionLoading] = useState(null);

  useEffect(() => {
    fetchMyBorrowings();
  }, [userId]);

  const fetchMyBorrowings = async () => {
    setLoading(true);
    try {
      const response = await getMyBorrowings(userId);
      if (response.status === 'success') {
        setBorrowings(response.body || []);
      } else {
        setError(response.message);
      }
    } catch (err) {
      setError('Failed to fetch borrowings');
    } finally {
      setLoading(false);
    }
  };

  const handleAction = async (action, borrowingId, data = {}) => {
    setActionLoading(borrowingId);

    try {
      let response;
      switch (action) {
        case 'start':
          response = await startBorrowing(userId, borrowingId);
          break;
        case 'return':
          response = await initiateReturn(userId, borrowingId, data);
          break;
        case 'confirm':
          response = await confirmReturn(userId, borrowingId);
          break;
        case 'cancel':
          response = await cancelBorrowingRequest(userId, borrowingId);
          break;
        default:
          return;
      }

      if (response.status === 'success') {
        fetchMyBorrowings(); // Refresh list
      } else {
        alert(response.message);
      }
    } catch (err) {
      alert(`Failed to ${action} borrowing`);
    } finally {
      setActionLoading(null);
    }
  };

  const formatDateTime = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleString();
  };

  const renderActions = (borrowing) => {
    const isLoading = actionLoading === borrowing.id;

    switch (borrowing.status) {
      case BORROWING_STATUS.APPROVED:
        return (
          <button
            onClick={() => handleAction('start', borrowing.id)}
            disabled={isLoading}
          >
            {isLoading ? 'Processing...' : 'Pick Up Vehicle'}
          </button>
        );

      case BORROWING_STATUS.ACTIVE:
        return (
          <>
            <button
              onClick={() => handleAction('return', borrowing.id)}
              disabled={isLoading}
            >
              {isLoading ? 'Processing...' : 'Initiate Return'}
            </button>
            {/* Transfer button is enabled only while the borrowing is ACTIVE
                and no other transfer is pending. */}
            <button
              onClick={() => openTransferModal(borrowing.id)}
              disabled={isLoading}
            >
              Transfer to Another User
            </button>
          </>
        );

      case BORROWING_STATUS.TRANSFER_REQUESTED:
        // Current borrower can cancel their pending outgoing transfer.
        // Incoming transfers are handled in a separate inbox component.
        return borrowing.activeTransfer &&
          borrowing.activeTransfer.fromBorrowerId === userId ? (
          <button
            className="cancel-btn"
            onClick={() =>
              handleAction('cancelTransfer', borrowing.id, {
                transferId: borrowing.activeTransfer.id
              })
            }
            disabled={isLoading}
          >
            {isLoading ? 'Processing...' : 'Cancel Transfer'}
          </button>
        ) : (
          <span className="waiting-text">
            Transfer pending — awaiting recipient
          </span>
        );

      case BORROWING_STATUS.RETURN_INITIATED:
        return (
          <>
            {!borrowing.returnConfirmedByBorrower && (
              <button
                onClick={() => handleAction('confirm', borrowing.id)}
                disabled={isLoading}
              >
                {isLoading ? 'Processing...' : 'Confirm Return'}
              </button>
            )}
            {borrowing.returnConfirmedByBorrower && (
              <span className="waiting-text">Waiting for owner confirmation</span>
            )}
          </>
        );

      case BORROWING_STATUS.PENDING:
        return (
          <button
            className="cancel-btn"
            onClick={() => handleAction('cancel', borrowing.id)}
            disabled={isLoading}
          >
            {isLoading ? 'Processing...' : 'Cancel Request'}
          </button>
        );

      default:
        return null;
    }
  };

  if (loading) return <div>Loading your borrowings...</div>;
  if (error) return <div className="error">{error}</div>;
  if (borrowings.length === 0) return <div>No borrowings found</div>;

  return (
    <div className="my-borrowings-list">
      <h3>My Borrowings</h3>

      {borrowings.map(borrowing => (
        <div key={borrowing.id} className="borrowing-card">
          <div
            className="status-badge"
            style={{ backgroundColor: STATUS_COLORS[borrowing.status] }}
          >
            {STATUS_LABELS[borrowing.status]}
          </div>

          <div className="borrowing-info">
            <h4>{borrowing.vehicleInfo}</h4>
            <p><strong>Owner:</strong> {borrowing.ownerName}</p>
            <p><strong>Purpose:</strong> {borrowing.borrowingPurpose || 'Not specified'}</p>
            <p><strong>Expected Return:</strong> {formatDateTime(borrowing.expectedReturnTime)}</p>

            {borrowing.status === BORROWING_STATUS.ACTIVE && (
              <p><strong>Picked Up:</strong> {formatDateTime(borrowing.borrowingTime)}</p>
            )}

            {borrowing.status === BORROWING_STATUS.RETURNED && (
              <p><strong>Returned:</strong> {formatDateTime(borrowing.actualReturnTime)}</p>
            )}

            {borrowing.status === BORROWING_STATUS.REJECTED && (
              <p className="rejection-reason">
                <strong>Rejection Reason:</strong> {borrowing.rejectionReason || 'Not provided'}
              </p>
            )}
          </div>

          <div className="borrowing-actions">
            {renderActions(borrowing)}
          </div>
        </div>
      ))}
    </div>
  );
};

export default MyBorrowingsList;
```

### 5. My Vehicle Borrowings (Owner View)

```jsx
// src/components/borrowing/MyVehicleBorrowings.jsx
import React, { useState, useEffect } from 'react';
import { getMyVehicleBorrowings, confirmReturn, rejectReturn } from '../../services/vehicleBorrowingService';
import { BORROWING_STATUS, STATUS_LABELS, STATUS_COLORS } from '../../constants/borrowingStatus';

const MyVehicleBorrowings = ({ userId }) => {
  const [borrowings, setBorrowings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [actionLoading, setActionLoading] = useState(null);
  const [rejectModal, setRejectModal] = useState({ open: false, borrowingId: null });
  const [rejectReason, setRejectReason] = useState('');
  // Owner's own "was the key handed back?" observation per borrowing,
  // captured independently of borrowing.keyReturned (the borrower's claim).
  const [keyReturnedInputs, setKeyReturnedInputs] = useState({});

  useEffect(() => {
    fetchVehicleBorrowings();
  }, [userId]);

  const fetchVehicleBorrowings = async () => {
    setLoading(true);
    try {
      const response = await getMyVehicleBorrowings(userId);
      if (response.status === 'success') {
        setBorrowings(response.body || []);
      } else {
        setError(response.message);
      }
    } catch (err) {
      setError('Failed to fetch vehicle borrowings');
    } finally {
      setLoading(false);
    }
  };

  const handleConfirmReturn = async (borrowingId) => {
    setActionLoading(borrowingId);
    try {
      const keyReturned = keyReturnedInputs[borrowingId] ?? null;
      const response = await confirmReturn(userId, borrowingId, keyReturned);
      if (response.status === 'success') {
        fetchVehicleBorrowings();
      } else {
        alert(response.message);
      }
    } catch (err) {
      alert('Failed to confirm return');
    } finally {
      setActionLoading(null);
    }
  };

  const openRejectReturnModal = (borrowingId) => {
    setRejectModal({ open: true, borrowingId });
    setRejectReason('');
  };

  const handleRejectReturn = async () => {
    const { borrowingId } = rejectModal;
    setActionLoading(borrowingId);
    try {
      const keyReturned = keyReturnedInputs[borrowingId] ?? null;
      const response = await rejectReturn(userId, borrowingId, rejectReason, keyReturned);
      if (response.status === 'success') {
        setRejectModal({ open: false, borrowingId: null });
        fetchVehicleBorrowings();
      } else {
        alert(response.message);
      }
    } catch (err) {
      alert('Failed to reject return');
    } finally {
      setActionLoading(null);
    }
  };

  const setKeyReturnedInput = (borrowingId, value) => {
    setKeyReturnedInputs(prev => ({ ...prev, [borrowingId]: value }));
  };

  const formatDateTime = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleString();
  };

  if (loading) return <div>Loading vehicle borrowings...</div>;
  if (error) return <div className="error">{error}</div>;
  if (borrowings.length === 0) return <div>No borrowings for your vehicles</div>;

  return (
    <div className="vehicle-borrowings-list">
      <h3>My Vehicle Borrowings</h3>

      {borrowings.map(borrowing => (
        <div key={borrowing.id} className="borrowing-card">
          <div
            className="status-badge"
            style={{ backgroundColor: STATUS_COLORS[borrowing.status] }}
          >
            {STATUS_LABELS[borrowing.status]}
          </div>

          <div className="borrowing-info">
            <h4>{borrowing.vehicleInfo}</h4>
            <p><strong>Borrower:</strong> {borrowing.borrowerName}</p>
            <p><strong>Purpose:</strong> {borrowing.borrowingPurpose || 'Not specified'}</p>
            <p><strong>Expected Return:</strong> {formatDateTime(borrowing.expectedReturnTime)}</p>

            {borrowing.borrowingTime && (
              <p><strong>Picked Up:</strong> {formatDateTime(borrowing.borrowingTime)}</p>
            )}

            {borrowing.returnNotes && (
              <p><strong>Return Notes:</strong> {borrowing.returnNotes}</p>
            )}

            {borrowing.status === BORROWING_STATUS.RETURN_INITIATED && borrowing.returnInitiatedAt && (
              <p className="urgent-text">
                Awaiting your approval since {formatDateTime(borrowing.returnInitiatedAt)}
                {' '}(you'll be reminded every 30 min until you act)
              </p>
            )}

            {borrowing.keyReturned !== null && borrowing.keyReturned !== undefined && (
              <p><strong>Borrower says key returned:</strong> {borrowing.keyReturned ? 'Yes' : 'No'}</p>
            )}

            {borrowing.keyReturnedConfirmedByOwner !== null && borrowing.keyReturnedConfirmedByOwner !== undefined && (
              <p><strong>You confirmed key returned:</strong> {borrowing.keyReturnedConfirmedByOwner ? 'Yes' : 'No'}</p>
            )}

            {borrowing.status === BORROWING_STATUS.RETURN_INITIATED && !borrowing.returnConfirmedByOwner && (
              <div className="key-returned-input">
                <label>Was the key physically returned to you?</label>
                <div className="radio-group">
                  <label>
                    <input
                      type="radio"
                      name={`keyReturned-${borrowing.id}`}
                      checked={keyReturnedInputs[borrowing.id] === true}
                      onChange={() => setKeyReturnedInput(borrowing.id, true)}
                    />
                    Yes
                  </label>
                  <label>
                    <input
                      type="radio"
                      name={`keyReturned-${borrowing.id}`}
                      checked={keyReturnedInputs[borrowing.id] === false}
                      onChange={() => setKeyReturnedInput(borrowing.id, false)}
                    />
                    No
                  </label>
                </div>
              </div>
            )}
          </div>

          <div className="borrowing-actions">
            {borrowing.status === BORROWING_STATUS.RETURN_INITIATED && (
              <>
                {!borrowing.returnConfirmedByOwner ? (
                  <>
                    <button
                      onClick={() => handleConfirmReturn(borrowing.id)}
                      disabled={actionLoading === borrowing.id}
                    >
                      {actionLoading === borrowing.id ? 'Confirming...' : 'Confirm Return'}
                    </button>
                    <button
                      className="reject-btn"
                      onClick={() => openRejectReturnModal(borrowing.id)}
                      disabled={actionLoading === borrowing.id}
                    >
                      Reject Return
                    </button>
                  </>
                ) : (
                  <span className="waiting-text">Waiting for borrower confirmation</span>
                )}
              </>
            )}
          </div>
        </div>
      ))}

      {/* Reject Return Modal */}
      {rejectModal.open && (
        <div className="modal-overlay">
          <div className="modal">
            <h4>Reject Return</h4>
            <p className="hint">The vehicle stays with the borrower; they can re-initiate the return later.</p>
            <textarea
              value={rejectReason}
              onChange={(e) => setRejectReason(e.target.value)}
              placeholder="Reason for rejection (e.g. key not received)"
              rows={3}
            />
            <div className="modal-actions">
              <button onClick={() => setRejectModal({ open: false, borrowingId: null })}>
                Cancel
              </button>
              <button onClick={handleRejectReturn} disabled={actionLoading !== null}>
                {actionLoading ? 'Rejecting...' : 'Confirm Reject'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default MyVehicleBorrowings;
```

### 6. Initiate Transfer Modal (Current Borrower)

```jsx
// src/components/borrowing/InitiateTransferModal.jsx
import React, { useState } from 'react';
import { searchUserById, initiateTransfer } from '../../services/vehicleBorrowingService';

const InitiateTransferModal = ({ borrowingId, userId, onSuccess, onCancel }) => {
  const [searchId, setSearchId] = useState('');
  const [foundUser, setFoundUser] = useState(null);
  const [reason, setReason] = useState('');
  const [notes, setNotes] = useState('');
  const [searching, setSearching] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const handleSearch = async () => {
    if (!searchId.trim()) {
      setError('Please enter an AutoAdda ID');
      return;
    }
    setSearching(true);
    setError(null);
    try {
      const response = await searchUserById(searchId);
      if (response.status === 'success') {
        setFoundUser(response.body);
      } else {
        setError(response.message || 'User not found');
      }
    } catch {
      setError('Failed to search user');
    } finally {
      setSearching(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!foundUser) {
      setError('Please search and select a recipient first');
      return;
    }
    setSubmitting(true);
    setError(null);
    try {
      const response = await initiateTransfer(userId, borrowingId, {
        toBorrowerId: foundUser.id,
        reason,
        notes
      });
      if (response.status === 'success') {
        onSuccess(response.body);
      } else {
        setError(response.message);
      }
    } catch {
      setError('Failed to initiate transfer');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="modal-overlay">
      <div className="modal transfer-modal">
        <h3>Transfer Vehicle to Another User</h3>
        <p className="hint">
          The vehicle owner is notified automatically — no owner approval
          required. Expected return time stays the same.
        </p>

        {error && <div className="error-message">{error}</div>}

        <div className="search-section">
          <label>Recipient AutoAdda ID</label>
          <div className="search-input-group">
            <input
              type="number"
              value={searchId}
              onChange={(e) => setSearchId(e.target.value)}
              placeholder="Enter AutoAdda ID"
              disabled={foundUser !== null}
            />
            {!foundUser ? (
              <button type="button" onClick={handleSearch} disabled={searching}>
                {searching ? 'Searching...' : 'Search'}
              </button>
            ) : (
              <button type="button" onClick={() => { setFoundUser(null); setSearchId(''); }}>
                Clear
              </button>
            )}
          </div>
        </div>

        {foundUser && (
          <div className="found-user-card">
            <p><strong>Name:</strong> {foundUser.name}</p>
            <p><strong>Mobile:</strong> {foundUser.mobileNumber}</p>
            <p><strong>ID:</strong> {foundUser.id}</p>
          </div>
        )}

        {foundUser && (
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>Reason (Optional)</label>
              <input
                type="text"
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                placeholder="e.g. Going out of town"
              />
            </div>
            <div className="form-group">
              <label>Notes (Optional)</label>
              <textarea
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                rows={3}
                placeholder="Handover details for the recipient"
              />
            </div>
            <div className="modal-actions">
              <button type="button" onClick={onCancel} disabled={submitting}>Cancel</button>
              <button type="submit" disabled={submitting}>
                {submitting ? 'Submitting...' : 'Request Transfer'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
};

export default InitiateTransferModal;
```

### 7. Transfer Inbox (Recipient)

```jsx
// src/components/borrowing/TransferInbox.jsx
// Shows borrowings that currently have a PENDING incoming transfer to `userId`.
// Use getMyBorrowings() and filter where activeTransfer.toBorrowerId === userId.
import React, { useState, useEffect } from 'react';
import {
  getMyBorrowings,
  acceptTransfer,
  rejectTransfer
} from '../../services/vehicleBorrowingService';
import { BORROWING_STATUS } from '../../constants/borrowingStatus';

const TransferInbox = ({ userId }) => {
  const [incoming, setIncoming] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(null);
  const [rejectModal, setRejectModal] = useState({ open: false, borrowingId: null, transferId: null });
  const [rejectReason, setRejectReason] = useState('');

  useEffect(() => {
    fetchIncoming();
  }, [userId]);

  const fetchIncoming = async () => {
    setLoading(true);
    try {
      const response = await getMyBorrowings(userId);
      if (response.status === 'success') {
        const pending = (response.body || []).filter(
          (b) =>
            b.status === BORROWING_STATUS.TRANSFER_REQUESTED &&
            b.activeTransfer &&
            b.activeTransfer.toBorrowerId === userId
        );
        setIncoming(pending);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleAccept = async (borrowing) => {
    setActionLoading(borrowing.id);
    try {
      const response = await acceptTransfer(userId, borrowing.id, borrowing.activeTransfer.id);
      if (response.status === 'success') {
        fetchIncoming();
      } else {
        alert(response.message);
      }
    } finally {
      setActionLoading(null);
    }
  };

  const openReject = (borrowing) =>
    setRejectModal({ open: true, borrowingId: borrowing.id, transferId: borrowing.activeTransfer.id });

  const handleReject = async () => {
    setActionLoading(rejectModal.borrowingId);
    try {
      const response = await rejectTransfer(
        userId,
        rejectModal.borrowingId,
        rejectModal.transferId,
        rejectReason
      );
      if (response.status === 'success') {
        setRejectModal({ open: false, borrowingId: null, transferId: null });
        setRejectReason('');
        fetchIncoming();
      } else {
        alert(response.message);
      }
    } finally {
      setActionLoading(null);
    }
  };

  if (loading) return <div>Loading incoming transfers...</div>;
  if (incoming.length === 0) return <div>No pending transfers</div>;

  return (
    <div className="transfer-inbox">
      <h3>Incoming Vehicle Transfers</h3>

      {incoming.map((b) => (
        <div key={b.id} className="transfer-card">
          <h4>{b.vehicleInfo}</h4>
          <p><strong>From:</strong> {b.activeTransfer.fromBorrowerName}</p>
          <p><strong>Owner:</strong> {b.ownerName}</p>
          <p><strong>Reason:</strong> {b.activeTransfer.reason || 'Not specified'}</p>
          <p><strong>Notes:</strong> {b.activeTransfer.notes || '—'}</p>
          <p><strong>Expected Return (inherited):</strong> {new Date(b.expectedReturnTime).toLocaleString()}</p>

          <div className="transfer-actions">
            <button
              onClick={() => handleAccept(b)}
              disabled={actionLoading === b.id}
            >
              {actionLoading === b.id ? 'Accepting...' : 'Accept Transfer'}
            </button>
            <button
              className="reject-btn"
              onClick={() => openReject(b)}
              disabled={actionLoading === b.id}
            >
              Decline
            </button>
          </div>
        </div>
      ))}

      {rejectModal.open && (
        <div className="modal-overlay">
          <div className="modal">
            <h4>Decline Transfer</h4>
            <textarea
              value={rejectReason}
              onChange={(e) => setRejectReason(e.target.value)}
              placeholder="Reason (optional)"
              rows={3}
            />
            <div className="modal-actions">
              <button
                onClick={() =>
                  setRejectModal({ open: false, borrowingId: null, transferId: null })
                }
              >
                Cancel
              </button>
              <button onClick={handleReject} disabled={actionLoading !== null}>
                Confirm Decline
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default TransferInbox;
```

### 8. Transfer History Timeline

```jsx
// src/components/borrowing/TransferHistory.jsx
// Renders the chain of custody for a borrowing. Data comes from
// either borrowing.transferHistory (embedded) or getTransfersForBorrowing().
import React from 'react';
import { TRANSFER_STATUS_LABELS } from '../../constants/borrowingStatus';

const TransferHistory = ({ transfers = [] }) => {
  if (transfers.length === 0) {
    return <p className="muted">No transfers on this borrowing yet.</p>;
  }

  return (
    <div className="transfer-history">
      <h4>Chain of Custody</h4>
      <ol>
        {transfers.map((t) => (
          <li key={t.id} className={`transfer-row status-${t.status.toLowerCase()}`}>
            <span><strong>{t.fromBorrowerName}</strong> → <strong>{t.toBorrowerName}</strong></span>
            <span className="badge">{TRANSFER_STATUS_LABELS[t.status]}</span>
            <span className="muted">
              {t.resolvedAt
                ? new Date(t.resolvedAt).toLocaleString()
                : new Date(t.requestedAt).toLocaleString()}
            </span>
            {t.reason && <div className="note">Reason: {t.reason}</div>}
          </li>
        ))}
      </ol>
    </div>
  );
};

export default TransferHistory;
```

### 9. Vehicle Availability Check

```jsx
// src/components/borrowing/VehicleAvailabilityBadge.jsx
import React, { useState, useEffect } from 'react';
import { checkVehicleAvailability } from '../../services/vehicleBorrowingService';

const VehicleAvailabilityBadge = ({ vehicleId }) => {
  const [available, setAvailable] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const checkAvailability = async () => {
      try {
        const response = await checkVehicleAvailability(vehicleId);
        if (response.status === 'success') {
          setAvailable(response.body.available);
        }
      } catch (err) {
        console.error('Failed to check availability');
      } finally {
        setLoading(false);
      }
    };

    checkAvailability();
  }, [vehicleId]);

  if (loading) return <span className="badge loading">Checking...</span>;

  return (
    <span className={`badge ${available ? 'available' : 'unavailable'}`}>
      {available ? 'Available for Borrowing' : 'Not Available'}
    </span>
  );
};

export default VehicleAvailabilityBadge;
```

### 10. Quick-Borrow Button (Owner or Self-Request)

```jsx
// src/components/borrowing/QuickBorrowButton.jsx
// Pass `borrowerId` only when the caller is the vehicle owner assigning to
// someone else. Omit it for a self-request — the server forces the
// borrower to whoever is calling (the `userId` header), ignoring anything
// else supplied.
import React, { useState } from 'react';
import { quickBorrow } from '../../services/vehicleBorrowingService';

const QuickBorrowButton = ({ vehicleId, userId, borrowerId = null, onSuccess }) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleQuickBorrow = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await quickBorrow(userId, { vehicleId, borrowerId });
      if (response.status === 'success') {
        onSuccess(response.body); // status is already ACTIVE, no /start needed
      } else if (response.code === '403') {
        // Self-request path: caller isn't on the owner's trusted list yet.
        // Fall back to the normal request flow (createBorrowingRequest) or
        // prompt the user to ask the owner to add them as trusted.
        setError(response.message);
      } else {
        setError(response.message);
      }
    } catch (err) {
      setError('Failed to quick-borrow vehicle');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="quick-borrow">
      {error && <div className="error-message">{error}</div>}
      <button onClick={handleQuickBorrow} disabled={loading}>
        {loading ? 'Starting...' : borrowerId ? 'Assign & Start Now' : 'Borrow Now'}
      </button>
      <p className="hint">
        {borrowerId
          ? 'Skips approval and pickup — the borrowing is active immediately.'
          : 'Auto-approved — no need to wait for the owner.'}
      </p>
    </div>
  );
};

export default QuickBorrowButton;
```

### 11. Quick-Return Button (Owner Always, Borrower Only If Trusted)

```jsx
// src/components/borrowing/QuickReturnButton.jsx
// Shown for any borrowing with status ACTIVE or RETURN_INITIATED. The
// owner can always call this. The borrower can only call it successfully
// if the owner has them on their trusted borrower list — a 403 means the
// borrower should fall back to initiateReturn() and wait for the owner's
// confirmReturn() instead.
import React, { useState } from 'react';
import { quickReturn } from '../../services/vehicleBorrowingService';

const QuickReturnButton = ({ borrowingId, userId, onSuccess, onNotTrusted }) => {
  const [loading, setLoading] = useState(false);
  const [keyReturned, setKeyReturned] = useState(null);
  const [error, setError] = useState(null);

  const handleQuickReturn = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await quickReturn(userId, borrowingId, { keyReturned });
      if (response.status === 'success') {
        onSuccess(response.body); // status is already RETURNED
      } else if (response.code === '403' && onNotTrusted) {
        // Not on the owner's trusted list — offer the standard return flow instead.
        onNotTrusted(response.message);
      } else {
        setError(response.message);
      }
    } catch (err) {
      setError('Failed to quick-return vehicle');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="quick-return">
      {error && <div className="error-message">{error}</div>}
      <label className="checkbox-label">
        <input
          type="checkbox"
          checked={keyReturned === true}
          onChange={(e) => setKeyReturned(e.target.checked ? true : null)}
        />
        Key physically handed over
      </label>
      <button onClick={handleQuickReturn} disabled={loading}>
        {loading ? 'Completing...' : 'Return Now (No Confirmation Needed)'}
      </button>
    </div>
  );
};

export default QuickReturnButton;
```

### 12. Trusted Borrowers Manager (Owner)

```jsx
// src/components/borrowing/TrustedBorrowersManager.jsx
// Lets an owner view/add/remove the borrowers who can self-quick-borrow
// any of their vehicles without per-request approval.
import React, { useState, useEffect } from 'react';
import {
  getTrustedBorrowers,
  addTrustedBorrower,
  removeTrustedBorrower,
  searchUserById
} from '../../services/vehicleBorrowingService';

const TrustedBorrowersManager = ({ ownerId }) => {
  const [trustedBorrowers, setTrustedBorrowers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchId, setSearchId] = useState('');
  const [foundUser, setFoundUser] = useState(null);
  const [searching, setSearching] = useState(false);
  const [error, setError] = useState(null);
  const [actionLoading, setActionLoading] = useState(null);

  useEffect(() => {
    fetchTrustedBorrowers();
  }, [ownerId]);

  const fetchTrustedBorrowers = async () => {
    setLoading(true);
    try {
      const response = await getTrustedBorrowers(ownerId);
      if (response.status === 'success') {
        setTrustedBorrowers(response.body || []);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async () => {
    if (!searchId.trim()) return;
    setSearching(true);
    setError(null);
    try {
      const response = await searchUserById(searchId);
      if (response.status === 'success') {
        setFoundUser(response.body);
      } else {
        setError(response.message || 'User not found');
      }
    } finally {
      setSearching(false);
    }
  };

  const handleAdd = async () => {
    if (!foundUser) return;
    setActionLoading('add');
    setError(null);
    try {
      const response = await addTrustedBorrower(ownerId, foundUser.id);
      if (response.status === 'success') {
        setFoundUser(null);
        setSearchId('');
        fetchTrustedBorrowers();
      } else {
        setError(response.message);
      }
    } finally {
      setActionLoading(null);
    }
  };

  const handleRemove = async (borrowerId) => {
    setActionLoading(borrowerId);
    try {
      const response = await removeTrustedBorrower(ownerId, borrowerId);
      if (response.status === 'success') {
        setTrustedBorrowers(prev => prev.filter(tb => tb.borrowerId !== borrowerId));
      } else {
        alert(response.message);
      }
    } finally {
      setActionLoading(null);
    }
  };

  return (
    <div className="trusted-borrowers-manager">
      <h3>Trusted Borrowers</h3>
      <p className="hint">
        People on this list can quick-borrow any of your vehicles instantly,
        without waiting for your approval each time.
      </p>

      {error && <div className="error-message">{error}</div>}

      <div className="search-section">
        <div className="search-input-group">
          <input
            type="number"
            value={searchId}
            onChange={(e) => setSearchId(e.target.value)}
            placeholder="Enter AutoAdda ID to add"
            disabled={foundUser !== null}
          />
          {!foundUser ? (
            <button type="button" onClick={handleSearch} disabled={searching}>
              {searching ? 'Searching...' : 'Search'}
            </button>
          ) : (
            <button type="button" onClick={() => { setFoundUser(null); setSearchId(''); }}>
              Clear
            </button>
          )}
        </div>

        {foundUser && (
          <div className="found-user-card">
            <p><strong>Name:</strong> {foundUser.name}</p>
            <p><strong>Mobile:</strong> {foundUser.mobileNumber}</p>
            <button onClick={handleAdd} disabled={actionLoading === 'add'}>
              {actionLoading === 'add' ? 'Adding...' : 'Add as Trusted Borrower'}
            </button>
          </div>
        )}
      </div>

      {loading ? (
        <div>Loading trusted borrowers...</div>
      ) : trustedBorrowers.length === 0 ? (
        <div>No trusted borrowers yet.</div>
      ) : (
        <ul className="trusted-borrowers-list">
          {trustedBorrowers.map(tb => (
            <li key={tb.id}>
              <span>{tb.borrowerName} ({tb.borrowerMobileNumber})</span>
              <button
                className="remove-btn"
                onClick={() => handleRemove(tb.borrowerId)}
                disabled={actionLoading === tb.borrowerId}
              >
                {actionLoading === tb.borrowerId ? 'Removing...' : 'Remove'}
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

export default TrustedBorrowersManager;
```

---

## Workflow Diagrams

### Borrower-Initiated Flow
```
1. Borrower requests → PENDING
2. Owner approves → APPROVED  (or rejects → REJECTED)
3. Borrower picks up → ACTIVE
4. Borrower initiates return (optionally sets keyReturned) → RETURN_INITIATED
   - Borrower is auto-confirmed; only the owner's action can resolve this state now.
   - Owner is reminded every 30 minutes (email/push/SMS, per their reminder
     preferences) until they act — see "Forced Owner Approval" below.
5. Owner confirms → RETURNED
   OR Owner rejects (/reject-return, with a reason) → back to ACTIVE
      (borrower keeps the vehicle; re-initiate return when ready)
```

### Owner-Initiated Flow (Direct Assignment)
```
1. Owner searches user by ID
2. Owner assigns vehicle → APPROVED (skips PENDING)
3. Borrower picks up → ACTIVE
4. Borrower initiates return (optionally sets keyReturned) → RETURN_INITIATED
5. Owner confirms → RETURNED
   OR Owner rejects (/reject-return) → back to ACTIVE
```

### Forced Owner Approval on Return

Once a return is initiated, the vehicle is already excluded from new
borrowing requests, but nothing used to force the owner to act. Now:

- The owner must **explicitly confirm or reject** the return
  (`PUT /{id}/confirm-return` or `PUT /{id}/reject-return`).
- A background job re-notifies the owner every 30 minutes
  (`vehicle.borrowing.return-reminder.interval-ms`, default 1,800,000ms)
  for every borrowing still sitting in `RETURN_INITIATED`, with a message
  along the lines of "your vehicle is blocked pending your approval —
  please approve or reject as soon as possible." Reminders stop
  automatically the moment the owner confirms or rejects.
- Rejecting does **not** introduce a new status — the borrowing simply
  reverts to `ACTIVE` with `returnRejectionReason` populated, and the
  borrower is notified that the vehicle remains with them.
- `keyReturned` (set by the borrower at `/return`) is **not** validated
  automatically — it's only ever what the borrower claims. To actually
  check it, the owner must submit their own `keyReturned` value in the
  body of `/confirm-return` or `/reject-return`, which is stored
  separately as `keyReturnedConfirmedByOwner`. The two fields are
  intentionally independent so the owner's observation can never be
  silently overwritten by (or confused with) the borrower's claim.

### Borrower-to-Borrower Transfer Flow
Owner is notified on every transition but does NOT approve. Chain length
is unlimited — each accept rotates `borrowerId`; `originalBorrowerId` is
preserved. Expected return time is inherited (recipient cannot change it).

```
ACTIVE (borrowerId = B1)
  └─ B1 POST /{id}/transfer { toBorrowerId: B2 }
        ↓ Borrowing.status = TRANSFER_REQUESTED, Transfer.status = PENDING
        ↓ Owner notified | B2 notified
  ├─ B2 PUT /{id}/transfers/{tid}/accept
  │     ↓ Borrowing.status = ACTIVE, borrowerId rotated to B2
  │     ↓ Transfer.status = ACCEPTED | Owner + B1 notified
  ├─ B2 PUT /{id}/transfers/{tid}/reject
  │     ↓ Borrowing.status = ACTIVE (B1 keeps the vehicle)
  │     ↓ Transfer.status = REJECTED | Owner + B1 notified
  └─ B1 PUT /{id}/transfers/{tid}/cancel
        ↓ Borrowing.status = ACTIVE
        ↓ Transfer.status = CANCELLED | Owner + B2 notified

# Chain example:  owner → B1 → B2 → B3 → return to owner
# Each hop produces one BorrowingTransfer row; all visible in transferHistory.
```

Guards enforced server-side:
- Only the current `borrowerId` may initiate or cancel a transfer.
- Only `toBorrowerId` may accept or reject.
- A borrowing can only have one PENDING transfer at a time.
- `toBorrowerId` cannot equal current `borrowerId` or `ownerId`.
- Transfers are only allowed while `status === 'ACTIVE'`.

### Quick-Borrow / Quick-Return Flow (Fast Path)

Opt-in alternative to the flows above — same underlying statuses, safety
checks (vehicle exists, not already borrowed, not in garage), and
`VehicleBorrowingResponse` shape, just fewer round trips.

```
Quick-Borrow — one call straight to ACTIVE:

  Owner calls POST /quick-borrow { vehicleId, borrowerId }
        ↓ skips PENDING and APPROVED entirely
        Borrowing.status = ACTIVE, borrowingTime = now
        (borrower does NOT need to call /start separately)

  OR any other user calls POST /quick-borrow { vehicleId }
        ↓ borrowerId forced to the caller (self-request)
        ↓ REQUIRES caller to be on the owner's trusted borrower list —
          otherwise 403 "You are not a trusted borrower for this owner's
          vehicles. Ask the owner to add you as a trusted borrower first."
        Borrowing.status = ACTIVE, borrowingTime = now
        (no owner action required per-request, but the owner must have
         added this borrower to their trusted list beforehand — see
         Trusted Borrowers below)

Quick-Return — one call straight to RETURNED:

  Owner calls PUT /{id}/quick-return
        ↓ always allowed (they own the vehicle, no trust check)
        ↓ works from ACTIVE, or from RETURN_INITIATED (rescues a
          borrowing already stuck waiting on the slow-path owner
          confirmation, even if the borrower was never trusted)

  OR Borrower calls PUT /{id}/quick-return
        ↓ REQUIRES the borrower to be on the owner's trusted borrower
          list — otherwise 403 "You are not a trusted borrower for this
          owner's vehicles. Ask the owner to add you as a trusted
          borrower, or use the standard return + confirm-return flow
          instead."

  Either way, on success:
        Borrowing.status = RETURNED, actualReturnTime = now
        returnConfirmedByBorrower = true, returnConfirmedByOwner = true
        (no second-party confirmation, no 30-minute reminder involved)
```

When to use which:
- Use the slow path (`/`, `/approve`, `/start`, `/return`, `/confirm-return`)
  when the owner wants visibility/approval over who borrows their vehicle,
  or wants an explicit second confirmation before the return counts as done.
- Use quick-borrow/quick-return for trusted relationships (e.g. a family
  vehicle, a company fleet where the owner already knows and trusts every
  driver) where that ceremony is pure friction.
- The two paths interoperate freely: a borrowing started via the slow path
  can be closed out via `quick-return` (e.g. to rescue one stuck in
  `RETURN_INITIATED`) — the owner can always do this regardless of trust
  status, and a trusted borrower can too. A borrowing started via
  `quick-borrow` can still be ended via the slow `/return` +
  `/confirm-return` pair if desired.
- If a borrower isn't trusted, they aren't locked out of returning the
  vehicle — they just use `initiateReturn()` and wait for the owner's
  `confirmReturn()` instead of `quickReturn()`.

### Trusted Borrowers (Gate on Self-Request Quick-Borrow AND Borrower-Side Quick-Return)

Without this list, self-request quick-borrow would let *any* authenticated
user instantly claim *any* vehicle with zero owner involvement, and any
borrower could unilaterally mark any active borrowing as returned with no
owner confirmation — far more than just skipping ceremony. The trusted
borrower list closes both gaps:

- Scope is **per owner**, not per vehicle — one entry covers every vehicle
  that owner has. There's no per-vehicle trust list.
- **Quick-borrow**: only the **self-request** branch checks this list. The
  **owner-initiated** branch (owner passes `borrowerId` explicitly) never
  needs it — the owner picking the borrower themselves is inherently
  trusted for that instance.
- **Quick-return**: only the **borrower's** call checks this list. The
  **owner's** call never needs it — the owner asserting "this is returned"
  about their own vehicle is always honored, which also means the owner
  can still rescue a borrowing stuck in `RETURN_INITIATED` even when the
  borrower isn't trusted.
- Managed via `POST /trusted-borrowers`, `DELETE /trusted-borrowers/{borrowerId}`,
  `GET /trusted-borrowers` (all owner-only, keyed off the `X-Auto-Adda-Id`
  header — there's no `ownerId` path/body parameter, the caller IS the owner).
- Removing and re-adding the same borrower is safe (idempotent) — the
  server soft-deletes and revives the same record rather than erroring.
- A user who isn't yet trusted can still borrow and return the normal way
  (`createBorrowingRequest` → owner approves → `/start` → `/return` →
  `/confirm-return`), or the owner can quick-borrow-assign a vehicle to
  them directly (owner-initiated branch, no trust list needed) while still
  requiring the slow return handshake — trust status can differ for
  borrowing vs. returning the *same* borrowing if the owner revokes/adds
  trust mid-loan, since both checks are evaluated live at call time, not
  cached from when the borrowing started.

---

## API Endpoints Reference

| Method | Endpoint | Description | Required Header |
|--------|----------|-------------|-----------------|
| POST | `/vehicle-borrowings` | Create borrowing request | X-Auto-Adda-Id |
| POST | `/vehicle-borrowings/assign` | Owner assigns vehicle directly | X-Auto-Adda-Id |
| POST | `/vehicle-borrowings/quick-borrow` | Quick-borrow: owner-assign or self-request, straight to ACTIVE | X-Auto-Adda-Id |
| PUT | `/vehicle-borrowings/{id}/quick-return` | Quick-return: owner always allowed, borrower only if trusted, straight to RETURNED, no second confirmation | X-Auto-Adda-Id |
| GET | `/vehicle-borrowings/search-user?userId={id}` | Search user by ID | - |
| PUT | `/vehicle-borrowings/{id}/approve` | Approve request | X-Auto-Adda-Id |
| PUT | `/vehicle-borrowings/{id}/reject` | Reject request | X-Auto-Adda-Id |
| PUT | `/vehicle-borrowings/{id}/start` | Start borrowing | X-Auto-Adda-Id |
| PUT | `/vehicle-borrowings/{id}/return` | Initiate return | X-Auto-Adda-Id |
| PUT | `/vehicle-borrowings/{id}/confirm-return` | Confirm return | X-Auto-Adda-Id |
| PUT | `/vehicle-borrowings/{id}/reject-return` | Owner rejects a pending return (reverts to ACTIVE) | X-Auto-Adda-Id |
| PUT | `/vehicle-borrowings/{id}/cancel` | Cancel request | X-Auto-Adda-Id |
| GET | `/vehicle-borrowings/{id}` | Get by ID | - |
| GET | `/vehicle-borrowings/my-borrowings` | Get my borrowings | X-Auto-Adda-Id |
| GET | `/vehicle-borrowings/my-vehicles` | Get my vehicle borrowings | X-Auto-Adda-Id |
| GET | `/vehicle-borrowings/pending` | Get pending requests | X-Auto-Adda-Id |
| GET | `/vehicle-borrowings/vehicle/{vehicleId}` | Get by vehicle | - |
| GET | `/vehicle-borrowings/vehicle/{vehicleId}/available` | Check availability | - |
| GET | `/vehicle-borrowings/vehicle/{vehicleId}/active` | Get active borrowing | - |
| POST | `/vehicle-borrowings/{id}/transfer` | Current borrower initiates transfer | X-Auto-Adda-Id |
| PUT | `/vehicle-borrowings/{id}/transfers/{tid}/accept` | Recipient accepts transfer | X-Auto-Adda-Id |
| PUT | `/vehicle-borrowings/{id}/transfers/{tid}/reject` | Recipient rejects transfer | X-Auto-Adda-Id |
| PUT | `/vehicle-borrowings/{id}/transfers/{tid}/cancel` | Requester cancels pending transfer | X-Auto-Adda-Id |
| GET | `/vehicle-borrowings/{id}/transfers` | Full transfer chain for borrowing | - |
| POST | `/trusted-borrowers` | Owner adds a borrower to their trusted list (body: `{ borrowerId }`) | X-Auto-Adda-Id |
| DELETE | `/trusted-borrowers/{borrowerId}` | Owner removes a borrower from their trusted list | X-Auto-Adda-Id |
| GET | `/trusted-borrowers` | List the caller's (owner's) trusted borrowers | X-Auto-Adda-Id |

---

## Response Structure

```javascript
// Success Response
{
  "status": "success",
  "code": "200",
  "message": "Operation completed successfully",
  "body": { /* VehicleBorrowingResponse or array */ }
}

// Error Response
{
  "status": "error",
  "code": "400", // or 403, 404
  "message": "Error description",
  "body": null
}
```

### VehicleBorrowingResponse Object
```javascript
{
  "id": 1,
  "borrowerId": 7,                    // current holder — rotates on each accepted transfer
  "borrowerName": "Alex Kim",
  "originalBorrowerId": 5,            // first approved borrower; preserved for audit
  "originalBorrowerName": "John Doe",
  "vehicleId": 10,
  "vehicleInfo": "Honda City (MH12AB1234)",
  "ownerId": 3,
  "ownerName": "Jane Smith",
  "borrowingTime": "2024-01-15T10:00:00",
  "expectedReturnTime": "2024-01-20T18:00:00",
  "actualReturnTime": null,
  "status": "ACTIVE",                 // or TRANSFER_REQUESTED while a transfer is pending
  "returnConfirmedByBorrower": false,
  "returnConfirmedByOwner": false,
  "borrowingImage": null,
  "returnImage": null,
  "borrowingPurpose": "Weekend trip",
  "returnNotes": null,
  "rejectionReason": null,
  "keyReturned": null,                     // borrower's own claim, set at /return; null = not indicated
  "keyReturnedConfirmedByOwner": null,     // owner's own observation, set at /confirm-return or /reject-return; independent of the borrower's claim above
  "returnRejectionReason": null, // set only when the owner rejects a return via /reject-return
  "returnInitiatedAt": null,     // timestamp of the current RETURN_INITIATED cycle; null once resolved
  "createdAt": "2024-01-14T15:30:00",
  "updatedAt": "2024-01-15T10:00:00",

  // Present when a transfer is currently PENDING on this borrowing; null otherwise.
  "activeTransfer": null,

  // Full chain of custody — empty array if no transfers have been requested.
  "transferHistory": []
}
```

### BorrowingTransferResponse Object
Returned by transfer endpoints and embedded as `activeTransfer` /
`transferHistory[]` inside a borrowing response.

```javascript
{
  "id": 42,
  "borrowingId": 1,
  "fromBorrowerId": 5,
  "fromBorrowerName": "John Doe",
  "toBorrowerId": 7,
  "toBorrowerName": "Alex Kim",
  "status": "PENDING",                // PENDING | ACCEPTED | REJECTED | CANCELLED
  "reason": "Going out of town",
  "notes": "Keys with reception",
  "transferImage": null,
  "requestedAt": "2024-01-16T09:15:00",
  "resolvedAt": null,                  // set when ACCEPTED / REJECTED / CANCELLED
  "createdAt": "2024-01-16T09:15:00",
  "updatedAt": "2024-01-16T09:15:00"
}
```

### TrustedBorrowerResponse Object

Returned by `POST /trusted-borrowers` and embedded in the array from
`GET /trusted-borrowers`.

```javascript
{
  "id": 12,
  "ownerId": 3,
  "borrowerId": 7,
  "borrowerName": "Alex Kim",
  "borrowerMobileNumber": "+971501234567",
  "createdAt": "2024-01-16T09:15:00"
}
```

---

## Owner Email Notifications

Owners now receive an email (in addition to web push and SMS, gated by
`CommunicationPreference` flags) on **every** state transition:

| Transition | Email to Owner |
|---|---|
| Borrowing requested (PENDING) | ✅ |
| Owner approves | ✅ (confirmation) |
| Owner rejects | ✅ (confirmation) |
| Owner assigns directly | ✅ (confirmation) |
| Quick-borrow (owner-assign or self-request) | ✅ (both borrower and owner, generic "now active" copy — same regardless of who initiated) |
| Borrower cancels | ✅ |
| Borrower picks up (ACTIVE) | ✅ |
| Transfer requested | ✅ |
| Transfer accepted | ✅ |
| Transfer rejected | ✅ |
| Transfer cancelled | ✅ |
| Return initiated | ✅ |
| Return approval reminder (every 30 min while RETURN_INITIATED) | ✅ (respects owner's *reminder* notification preferences, separate from service-update preferences) |
| Return rejected (owner rejects → borrower notified) | ✅ (to borrower) |
| Return completed (RETURNED) | ✅ |
| Quick-return (RETURNED via the fast path) | ✅ (reuses the same "return completed" notification as the slow path) |

Nothing to do on the frontend — these are server-side. Surface them in
the UI via the existing borrowing status and `activeTransfer` field. It's
worth showing `returnInitiatedAt` ("awaiting owner approval since X") in
the owner's UI so the recurring reminders make sense in context.
