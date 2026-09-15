package com.autoadda.apis.vehicle.request;

import com.autoadda.apis.vehicle.exception.InvalidInputException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogueSearchCriteria {
    private String brand;
    private String model;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer fromYear;
    private Integer toYear;
    private Long companyId;

    // Pagination fields
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 10;
    public static final int MAX_SIZE = 100;
    public static final String DEFAULT_SORT_BY = "createdAt";
    public static final String DEFAULT_SORT_DIRECTION = "DESC";
    public static final Set<String> VALID_SORT_FIELDS =
            Set.of("id", "indicativePrice", "modelYear", "createdAt");

    public void validateRanges() {
        boolean hasMin = minPrice != null;
        boolean hasMax = maxPrice != null;
        if (hasMin != hasMax) {
            throw new InvalidInputException(
                    "Both minPrice and maxPrice must be provided together, or neither should be provided");
        }
        if (hasMin) {
            if (minPrice.signum() <= 0 || maxPrice.signum() <= 0) {
                throw new InvalidInputException("Price values must be positive numbers");
            }
            if (minPrice.compareTo(maxPrice) > 0) {
                throw new InvalidInputException(
                        "minPrice (" + minPrice + ") cannot be greater than maxPrice (" + maxPrice + ")");
            }
        }

        boolean hasFromYear = fromYear != null;
        boolean hasToYear = toYear != null;
        if (hasFromYear != hasToYear) {
            throw new InvalidInputException(
                    "Both fromYear and toYear must be provided together, or neither should be provided");
        }
        if (hasFromYear && fromYear > toYear) {
            throw new InvalidInputException(
                    "fromYear (" + fromYear + ") cannot be greater than toYear (" + toYear + ")");
        }
    }

    public int getPageOrDefault() {
        return page != null && page >= 0 ? page : DEFAULT_PAGE;
    }

    public int getSizeOrDefault() {
        if (size == null || size < 1) return DEFAULT_SIZE;
        return Math.min(size, MAX_SIZE);
    }

    public String getSortByOrDefault() {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return DEFAULT_SORT_BY;
        }
        String normalized = sortBy.trim();
        if (!VALID_SORT_FIELDS.contains(normalized)) {
            throw new IllegalArgumentException(
                    "Invalid sort field: " + sortBy + ". Valid fields are: " + VALID_SORT_FIELDS);
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
                    "Invalid sort direction: " + sortDirection + ". Must be ASC or DESC");
        }
        return normalized;
    }
}
