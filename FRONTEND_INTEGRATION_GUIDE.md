# Vehicle Listing API - Frontend Integration Guide

## Overview
This document describes the recent enhancements to the Vehicle Listing API filters.

## Changes Summary

### 1. Range-Based Price and Year Filters (⚠️ BREAKING CHANGE)
Single-value filters have been replaced with range-based filters for better search flexibility.

### 2. Multi-City Location Search (✅ NON-BREAKING)
Location filter now supports searching across multiple cities using comma-separated values.

---

## 1. Range-Based Filters (Breaking Change)

### What Changed?

**Old API (No longer supported):**
```
GET /vehicle-listing?price=15000&year=2020
```

**New API (Required):**
```
GET /vehicle-listing?fromPrice=5000&toPrice=15000&fromYear=2018&toYear=2022
```

### Migration Guide

#### Before:
```javascript
const params = {
  price: 15000,
  year: 2020
};
```

#### After:
```javascript
const params = {
  fromPrice: 5000,
  toPrice: 15000,
  fromYear: 2018,
  toYear: 2022
};
```

### API Parameters

| Old Parameter | New Parameters | Type | Required | Description |
|--------------|----------------|------|----------|-------------|
| `price` | `fromPrice`, `toPrice` | Double | Both or neither | Price range (min and max) |
| `year` | `fromYear`, `toYear` | Integer | Both or neither | Year range (min and max) |

### Validation Rules

1. **Both values required:** If you provide `fromPrice`, you MUST also provide `toPrice` (and vice versa)
2. **Logical range:** `fromPrice` must be ≤ `toPrice`, `fromYear` must be ≤ `toYear`
3. **Positive values:** All values must be positive numbers

### Error Responses

```json
// Missing one value
{
  "code": "400",
  "status": "failed",
  "message": "Both fromPrice and toPrice must be provided together, or neither should be provided"
}

// Invalid range
{
  "code": "400",
  "status": "failed",
  "message": "fromPrice (15000.0) cannot be greater than toPrice (5000.0)"
}

// Negative values
{
  "code": "400",
  "status": "failed",
  "message": "Price values must be positive numbers"
}
```

### React Example

```jsx
import { useState } from 'react';

function VehicleSearch() {
  const [filters, setFilters] = useState({
    fromPrice: '',
    toPrice: '',
    fromYear: '',
    toYear: ''
  });

  const handleSearch = async () => {
    // Validate that both values in a range are provided
    if ((filters.fromPrice && !filters.toPrice) || (!filters.fromPrice && filters.toPrice)) {
      alert('Please provide both fromPrice and toPrice');
      return;
    }

    if ((filters.fromYear && !filters.toYear) || (!filters.fromYear && filters.toYear)) {
      alert('Please provide both fromYear and toYear');
      return;
    }

    const params = new URLSearchParams();
    if (filters.fromPrice) params.append('fromPrice', filters.fromPrice);
    if (filters.toPrice) params.append('toPrice', filters.toPrice);
    if (filters.fromYear) params.append('fromYear', filters.fromYear);
    if (filters.toYear) params.append('toYear', filters.toYear);

    const response = await fetch(`/vehicle-listing?${params.toString()}`);
    const data = await response.json();

    if (data.code === '400') {
      alert(data.message); // Show validation error
    }
  };

  return (
    <div>
      <input
        type="number"
        placeholder="Min Price"
        value={filters.fromPrice}
        onChange={(e) => setFilters({...filters, fromPrice: e.target.value})}
      />
      <input
        type="number"
        placeholder="Max Price"
        value={filters.toPrice}
        onChange={(e) => setFilters({...filters, toPrice: e.target.value})}
      />

      <input
        type="number"
        placeholder="From Year"
        value={filters.fromYear}
        onChange={(e) => setFilters({...filters, fromYear: e.target.value})}
      />
      <input
        type="number"
        placeholder="To Year"
        value={filters.toYear}
        onChange={(e) => setFilters({...filters, toYear: e.target.value})}
      />

      <button onClick={handleSearch}>Search</button>
    </div>
  );
}
```

---

## 2. Multi-City Location Search (Non-Breaking)

### What's New?

Location filter now accepts multiple cities separated by commas.

**Single City (Still works):**
```
GET /vehicle-listing?location=Bhopal
```

**Multiple Cities (New feature):**
```
GET /vehicle-listing?location=Bhopal,Indore,Delhi
```

### API Parameters

| Parameter | Type | Format | Example |
|-----------|------|--------|---------|
| `location` | String | Single city or comma-separated cities | `Bhopal,Indore,Delhi` |

### Features

✅ **Backward Compatible:** Single city searches work exactly as before
✅ **Case Insensitive:** `Bhopal`, `bhopal`, `BHOPAL` all work the same
✅ **Auto-trimming:** Extra spaces are automatically removed
✅ **Duplicate Removal:** `Bhopal,bhopal,BHOPAL` → searches only once
✅ **Empty Value Filtering:** `Bhopal,,Indore` → ignores empty values

### React Example

```jsx
import { useState } from 'react';

function CitySelector() {
  const [selectedCities, setSelectedCities] = useState([]);

  const handleSearch = async () => {
    // Join selected cities with comma
    const locationParam = selectedCities.join(',');

    const params = new URLSearchParams();
    if (locationParam) params.append('location', locationParam);

    const response = await fetch(`/vehicle-listing?${params.toString()}`);
    const data = await response.json();
  };

  const toggleCity = (city) => {
    setSelectedCities(prev =>
      prev.includes(city)
        ? prev.filter(c => c !== city)
        : [...prev, city]
    );
  };

  return (
    <div>
      <h3>Select Cities:</h3>
      {['Bhopal', 'Indore', 'Delhi', 'Mumbai'].map(city => (
        <label key={city}>
          <input
            type="checkbox"
            checked={selectedCities.includes(city)}
            onChange={() => toggleCity(city)}
          />
          {city}
        </label>
      ))}
      <button onClick={handleSearch}>Search</button>
    </div>
  );
}
```

### Vue Example

```vue
<template>
  <div>
    <h3>Select Cities:</h3>
    <label v-for="city in cities" :key="city">
      <input
        type="checkbox"
        :value="city"
        v-model="selectedCities"
      />
      {{ city }}
    </label>
    <button @click="handleSearch">Search</button>
  </div>
</template>

<script>
export default {
  data() {
    return {
      cities: ['Bhopal', 'Indore', 'Delhi', 'Mumbai'],
      selectedCities: []
    };
  },
  methods: {
    async handleSearch() {
      const locationParam = this.selectedCities.join(',');

      const params = new URLSearchParams();
      if (locationParam) params.set('location', locationParam);

      const response = await fetch(`/vehicle-listing?${params.toString()}`);
      const data = await response.json();
    }
  }
};
</script>
```

---

## Complete API Examples

### Non-Paginated Endpoint

```
GET /vehicle-listing
```

**Example Requests:**

```bash
# Single city, price range
/vehicle-listing?location=Bhopal&fromPrice=10000&toPrice=50000

# Multiple cities, year range
/vehicle-listing?location=Bhopal,Indore,Delhi&fromYear=2015&toYear=2020

# All filters combined
/vehicle-listing?location=Bhopal,Indore&fromPrice=10000&toPrice=50000&fromYear=2018&toYear=2022&brand=Honda

# Global search (searches across all fields)
/vehicle-listing?search=honda+city
```

### Paginated Endpoint

```
GET /vehicle-listing/paginated
```

**Example Requests:**

```bash
# Basic pagination
/vehicle-listing/paginated?page=0&size=10

# With location filter
/vehicle-listing/paginated?location=Bhopal,Indore&page=0&size=20

# With all filters and sorting
/vehicle-listing/paginated?location=Bhopal,Indore&fromPrice=10000&toPrice=50000&fromYear=2018&toYear=2022&page=0&size=10&sortBy=price&sortDirection=ASC
```

### Pagination Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | Integer | 0 | Page number (0-indexed) |
| `size` | Integer | 10 | Items per page (max 100) |
| `sortBy` | String | createdAt | Sort field: `id`, `price`, `listedDate`, `createdAt` |
| `sortDirection` | String | DESC | Sort direction: `ASC` or `DESC` |

### Response Format (Paginated)

```json
{
  "code": "200",
  "status": "success",
  "message": "success",
  "body": {
    "content": [
      {
        "id": 1,
        "price": 25000,
        "city": "Bhopal",
        "description": "2019 Honda City",
        "vehicle": {
          "brand": "Honda",
          "model": "City",
          "year": "2019"
        }
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 25,
    "totalPages": 3,
    "first": true,
    "last": false,
    "empty": false,
    "numberOfElements": 10
  }
}
```

---

## Complete JavaScript Utility

```javascript
// vehicleApi.js
class VehicleAPI {
  constructor(baseURL = '') {
    this.baseURL = baseURL;
  }

  /**
   * Build query parameters for vehicle search
   */
  buildParams(filters) {
    const params = new URLSearchParams();

    // Multi-city location
    if (filters.cities && filters.cities.length > 0) {
      params.append('location', filters.cities.join(','));
    }

    // Price range (both required)
    if (filters.fromPrice && filters.toPrice) {
      params.append('fromPrice', filters.fromPrice);
      params.append('toPrice', filters.toPrice);
    }

    // Year range (both required)
    if (filters.fromYear && filters.toYear) {
      params.append('fromYear', filters.fromYear);
      params.append('toYear', filters.toYear);
    }

    // Other filters
    if (filters.brand) params.append('brand', filters.brand);
    if (filters.model) params.append('model', filters.model);
    if (filters.search) params.append('search', filters.search);

    // Pagination
    if (filters.page !== undefined) params.append('page', filters.page);
    if (filters.size) params.append('size', filters.size);
    if (filters.sortBy) params.append('sortBy', filters.sortBy);
    if (filters.sortDirection) params.append('sortDirection', filters.sortDirection);

    return params;
  }

  /**
   * Validate filters before making API call
   */
  validateFilters(filters) {
    const errors = [];

    // Validate price range
    if ((filters.fromPrice && !filters.toPrice) || (!filters.fromPrice && filters.toPrice)) {
      errors.push('Both fromPrice and toPrice must be provided together');
    }
    if (filters.fromPrice && filters.toPrice && parseFloat(filters.fromPrice) > parseFloat(filters.toPrice)) {
      errors.push('fromPrice cannot be greater than toPrice');
    }

    // Validate year range
    if ((filters.fromYear && !filters.toYear) || (!filters.fromYear && filters.toYear)) {
      errors.push('Both fromYear and toYear must be provided together');
    }
    if (filters.fromYear && filters.toYear && parseInt(filters.fromYear) > parseInt(filters.toYear)) {
      errors.push('fromYear cannot be greater than toYear');
    }

    return errors;
  }

  /**
   * Search vehicles (non-paginated)
   */
  async searchVehicles(filters) {
    const errors = this.validateFilters(filters);
    if (errors.length > 0) {
      throw new Error(errors.join(', '));
    }

    const params = this.buildParams(filters);
    const response = await fetch(`${this.baseURL}/vehicle-listing?${params.toString()}`);
    const data = await response.json();

    if (data.code !== '200') {
      throw new Error(data.message || 'Search failed');
    }

    return data.body;
  }

  /**
   * Search vehicles (paginated)
   */
  async searchVehiclesPaginated(filters) {
    const errors = this.validateFilters(filters);
    if (errors.length > 0) {
      throw new Error(errors.join(', '));
    }

    const params = this.buildParams(filters);
    const response = await fetch(`${this.baseURL}/vehicle-listing/paginated?${params.toString()}`);
    const data = await response.json();

    if (data.code !== '200') {
      throw new Error(data.message || 'Search failed');
    }

    return data.body;
  }
}

// Usage Example
const api = new VehicleAPI('http://localhost:8080');

// Search with filters
try {
  const results = await api.searchVehiclesPaginated({
    cities: ['Bhopal', 'Indore', 'Delhi'],
    fromPrice: 10000,
    toPrice: 50000,
    fromYear: 2018,
    toYear: 2022,
    brand: 'Honda',
    page: 0,
    size: 20,
    sortBy: 'price',
    sortDirection: 'ASC'
  });

  console.log('Total results:', results.totalElements);
  console.log('Vehicles:', results.content);
} catch (error) {
  console.error('Search error:', error.message);
}
```

---

## Testing Checklist

### For Range Filters:
- [ ] Test with both `fromPrice` and `toPrice` provided
- [ ] Test with only `fromPrice` (should fail with 400 error)
- [ ] Test with only `toPrice` (should fail with 400 error)
- [ ] Test with `fromPrice > toPrice` (should fail with 400 error)
- [ ] Test with negative values (should fail with 400 error)
- [ ] Same tests for year range

### For Multi-City Search:
- [ ] Test with single city (backward compatibility)
- [ ] Test with multiple cities
- [ ] Test with cities containing spaces
- [ ] Test with duplicate cities (should handle gracefully)
- [ ] Test with empty values between commas
- [ ] Test with mixed case cities

### Combined:
- [ ] Test all filters together
- [ ] Test pagination with filters
- [ ] Test sorting with filters
- [ ] Test empty results handling

---

## Support

For issues or questions, please contact the backend team or refer to:
- API Documentation: [Swagger/OpenAPI URL]
- Backend Repository: [GitHub URL]
- Issue Tracker: [Jira/GitHub Issues URL]

---

**Last Updated:** 2026-01-08
**API Version:** 0.0.1-SNAPSHOT
