package com.autoadda.apis.vehicle.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleSearchCriteria {
    private String location;
    private Double price;
    private String brand;
    private String model;
    private Integer year;
    private String globalSearch;

    // Pagination fields
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;

    // Pagination constants
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 10;
    public static final int MAX_SIZE = 100;
    public static final String DEFAULT_SORT_BY = "createdAt";
    public static final String DEFAULT_SORT_DIRECTION = "DESC";
    public static final Set<String> VALID_SORT_FIELDS = Set.of("id", "price", "listedDate", "createdAt");

    public boolean hasAnyCriteria() {
        return hasLocation() || hasPrice() || hasBrand() || hasModel() || hasYear() || hasGlobalSearch();
    }

    public boolean hasSpecificCriteria() {
        return hasLocation() || hasPrice() || hasBrand() || hasModel() || hasYear();
    }

    public boolean hasLocation() {
        return location != null && !location.trim().isEmpty();
    }

    public boolean hasPrice() {
        return price != null && price > 0;
    }

    public boolean hasBrand() {
        return brand != null && !brand.trim().isEmpty();
    }

    public boolean hasModel() {
        return model != null && !model.trim().isEmpty();
    }

    public boolean hasYear() {
        return year != null && year > 0;
    }

    public boolean hasGlobalSearch() {
        return globalSearch != null && !globalSearch.trim().isEmpty();
    }

    // Pagination helper methods
    public int getPageOrDefault() {
        return page != null && page >= 0 ? page : DEFAULT_PAGE;
    }

    public int getSizeOrDefault() {
        if (size == null) return DEFAULT_SIZE;
        if (size < 1) return DEFAULT_SIZE;
        return Math.min(size, MAX_SIZE);
    }

    public String getSortByOrDefault() {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return DEFAULT_SORT_BY;
        }
        String normalized = sortBy.trim();
        if (!VALID_SORT_FIELDS.contains(normalized)) {
            throw new IllegalArgumentException(
                "Invalid sort field: " + sortBy + ". Valid fields are: " + VALID_SORT_FIELDS
            );
        }
        return normalized;
    }

    public String getSortDirectionOrDefault() {
        if (sortDirection == null || sortDirection.trim().isEmpty()) {
            return DEFAULT_SORT_DIRECTION;
        }
        String normalized = sortDirection.trim().toUpperCase();
        if (!normalized.equals("ASC") && !normalized.equals("DESC")) {
            throw new IllegalArgumentException(
                "Invalid sort direction: " + sortDirection + ". Must be ASC or DESC"
            );
        }
        return normalized;
    }

    public boolean hasPagination() {
        return page != null || size != null;
    }
}