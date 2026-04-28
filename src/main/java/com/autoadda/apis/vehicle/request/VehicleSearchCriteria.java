package com.autoadda.apis.vehicle.request;

import com.autoadda.apis.vehicle.exception.InvalidInputException;
import com.autoadda.apis.vehicle.util.CarCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleSearchCriteria {
    private String location;
    private Double fromPrice;
    private Double toPrice;
    private String brand;
    private String model;
    private Integer fromYear;
    private Integer toYear;
    private String globalSearch;
    private String condition;

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
    public static final Set<String> VALID_SORT_FIELDS = Set.of("id", "price", "listedDate", DEFAULT_SORT_BY);

    public boolean hasAnyCriteria() {
        return hasLocation() || hasPrice() || hasBrand() || hasModel() || hasYear() || hasGlobalSearch() || hasCondition();
    }

    public boolean hasSpecificCriteria() {
        return hasLocation() || hasPrice() || hasBrand() || hasModel() || hasYear();
    }

    public boolean hasCondition() {
        return condition != null && !condition.trim().isEmpty();
    }

    public CarCondition getConditionEnum() {
        if (!hasCondition()) {
            return null;
        }
        try {
            return CarCondition.valueOf(condition.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException(
                "Invalid condition: " + condition + ". Must be NEW or USED"
            );
        }
    }

    public boolean hasLocation() {
        return location != null && !location.trim().isEmpty();
    }

    /**
     * Parses the location parameter into a list of cities.
     * Supports both single city and comma-separated multiple cities.
     *
     * @return List of trimmed, lowercase city names
     */
    public List<String> getCitiesAsList() {
        if (!hasLocation()) {
            return Collections.emptyList();
        }

        // Split by comma, trim each value, convert to lowercase, remove empty strings
        return Arrays.stream(location.split(","))
                .map(String::trim)
                .filter(city -> !city.isEmpty())
                .map(String::toLowerCase)
                .distinct()  // Remove duplicates
                .collect(Collectors.toList());
    }

    /**
     * Check if location filter contains multiple cities
     */
    public boolean hasMultipleCities() {
        return getCitiesAsList().size() > 1;
    }

    public boolean hasPrice() {
        return fromPrice != null && toPrice != null && fromPrice > 0 && toPrice > 0;
    }

    public boolean hasBrand() {
        return brand != null && !brand.trim().isEmpty();
    }

    public boolean hasModel() {
        return model != null && !model.trim().isEmpty();
    }

    public boolean hasYear() {
        return fromYear != null && toYear != null && fromYear > 0 && toYear > 0;
    }

    public boolean hasGlobalSearch() {
        return globalSearch != null && !globalSearch.trim().isEmpty();
    }

    // Range validation methods
    public void validatePriceRange() {
        boolean hasFromPrice = fromPrice != null;
        boolean hasToPrice = toPrice != null;

        if (hasFromPrice != hasToPrice) {
            throw new InvalidInputException(
                "Both fromPrice and toPrice must be provided together, or neither should be provided"
            );
        }

        if (hasFromPrice && hasToPrice) {
            if (fromPrice <= 0 || toPrice <= 0) {
                throw new InvalidInputException("Price values must be positive numbers");
            }
            if (fromPrice > toPrice) {
                throw new InvalidInputException(
                    "fromPrice (" + fromPrice + ") cannot be greater than toPrice (" + toPrice + ")"
                );
            }
        }
    }

    public void validateYearRange() {
        boolean hasFromYear = fromYear != null;
        boolean hasToYear = toYear != null;

        if (hasFromYear != hasToYear) {
            throw new InvalidInputException(
                "Both fromYear and toYear must be provided together, or neither should be provided"
            );
        }

        if (hasFromYear && hasToYear) {
            if (fromYear <= 0 || toYear <= 0) {
                throw new InvalidInputException("Year values must be positive numbers");
            }
            if (fromYear > toYear) {
                throw new InvalidInputException(
                    "fromYear (" + fromYear + ") cannot be greater than toYear (" + toYear + ")"
                );
            }
        }
    }

    public void validateRanges() {
        validatePriceRange();
        validateYearRange();
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