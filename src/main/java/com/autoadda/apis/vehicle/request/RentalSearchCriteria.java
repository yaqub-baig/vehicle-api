package com.autoadda.apis.vehicle.request;

import com.autoadda.apis.vehicle.enums.RentalRateType;
import com.autoadda.apis.vehicle.exception.InvalidInputException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentalSearchCriteria {
    private String brand;
    private String model;
    private Double fromPrice;
    private Double toPrice;
    private String rateType;
    private Integer minKmLimitPerDay;

    // Pagination fields
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;

    // Pagination constants
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 10;
    public static final int MAX_SIZE = 100;
    // Daily rate is the default sort field since every listing must set pricePerDay,
    // while pricePerHour is optional.
    public static final String DEFAULT_SORT_BY = "pricePerDay";
    public static final String DEFAULT_SORT_DIRECTION = "ASC";
    public static final Set<String> VALID_SORT_FIELDS =
            Set.of("pricePerDay", "pricePerHour", "createdAt", "kmLimitPerDay");

    public boolean hasBrand() {
        return brand != null && !brand.trim().isEmpty();
    }

    public boolean hasModel() {
        return model != null && !model.trim().isEmpty();
    }

    public boolean hasRateType() {
        return rateType != null && !rateType.trim().isEmpty();
    }

    public RentalRateType getRateTypeEnum() {
        if (!hasRateType()) {
            return null;
        }
        try {
            return RentalRateType.valueOf(rateType.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid rateType: " + rateType + ". Must be HOURLY or DAILY");
        }
    }

    public boolean hasPrice() {
        return fromPrice != null && toPrice != null;
    }

    public boolean hasMinKmLimitPerDay() {
        return minKmLimitPerDay != null;
    }

    public void validatePriceRange() {
        boolean hasFromPrice = fromPrice != null;
        boolean hasToPrice = toPrice != null;

        if (hasFromPrice != hasToPrice) {
            throw new InvalidInputException(
                "Both fromPrice and toPrice must be provided together, or neither should be provided"
            );
        }

        if (hasFromPrice) {
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

    public void validateRanges() {
        validatePriceRange();
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
}
