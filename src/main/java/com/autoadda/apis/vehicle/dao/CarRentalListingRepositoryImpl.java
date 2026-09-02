package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.CarRentalListing;
import com.autoadda.apis.vehicle.entity.VehicleDetails;
import com.autoadda.apis.vehicle.enums.RentalListingStatus;
import com.autoadda.apis.vehicle.enums.RentalRateType;
import com.autoadda.apis.vehicle.request.RentalSearchCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CarRentalListingRepositoryImpl implements CarRentalListingCustomRepository {

    private static final String STATUS = "status";
    private static final String CREATED_AT = "createdAt";
    private static final String VEHICLE_ID = "vehicleId";
    private static final String PRICE_PER_DAY = "pricePerDay";
    private static final String PRICE_PER_HOUR = "pricePerHour";
    private static final String KM_LIMIT_PER_DAY = "kmLimitPerDay";
    private static final String BRAND = "brand";
    private static final String MODEL = "model";
    private static final String LOCATION = "location";

    @PersistenceContext
    private EntityManager entityManager;

    private final VehicleDetailsRepository vehicleDetailsRepository;

    @Override
    public Page<CarRentalListing> findByCriteriaWithPagination(RentalSearchCriteria criteria) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<CarRentalListing> dataQuery = cb.createQuery(CarRentalListing.class);
        Root<CarRentalListing> dataRoot = dataQuery.from(CarRentalListing.class);

        List<Predicate> predicates = buildPredicates(cb, dataRoot, criteria);
        dataQuery.where(cb.and(predicates.toArray(new Predicate[0])));

        Order order = buildOrder(cb, dataRoot, criteria);
        dataQuery.orderBy(order);

        TypedQuery<CarRentalListing> typedQuery = entityManager.createQuery(dataQuery);

        int page = criteria.getPageOrDefault();
        int size = criteria.getSizeOrDefault();
        typedQuery.setFirstResult(page * size);
        typedQuery.setMaxResults(size);

        List<CarRentalListing> results = typedQuery.getResultList();

        long total = executeCountQuery(cb, criteria);

        return new PageImpl<>(results, PageRequest.of(page, size), total);
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<CarRentalListing> listing, RentalSearchCriteria criteria) {
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(listing.get(STATUS), RentalListingStatus.ACTIVE));
        predicates.add(cb.equal(listing.get("isDeleted"), false));

        if (criteria.hasBrand() || criteria.hasModel()) {
            List<Integer> vehicleIds = resolveMatchingVehicleIds(criteria);
            if (vehicleIds.isEmpty()) {
                // No vehicle matches the brand/model filter; force an empty result set.
                predicates.add(cb.equal(listing.get(VEHICLE_ID), -1));
            } else {
                predicates.add(listing.get(VEHICLE_ID).in(vehicleIds));
            }
        }

        if (criteria.hasLocation()) {
            predicates.add(cb.equal(
                    cb.lower(listing.get(LOCATION)),
                    criteria.getLocation().trim().toLowerCase()));
        }

        if (criteria.hasRateType()) {
            RentalRateType rateType = criteria.getRateTypeEnum();
            String priceField = rateType == RentalRateType.HOURLY ? PRICE_PER_HOUR : PRICE_PER_DAY;
            predicates.add(cb.isNotNull(listing.get(priceField)));

            if (criteria.hasPrice()) {
                predicates.add(cb.between(
                        listing.<BigDecimal>get(priceField),
                        BigDecimal.valueOf(criteria.getFromPrice()),
                        BigDecimal.valueOf(criteria.getToPrice())
                ));
            }
        } else if (criteria.hasPrice()) {
            predicates.add(cb.between(
                    listing.<BigDecimal>get(PRICE_PER_DAY),
                    BigDecimal.valueOf(criteria.getFromPrice()),
                    BigDecimal.valueOf(criteria.getToPrice())
            ));
        }

        if (criteria.hasMinKmLimitPerDay()) {
            predicates.add(cb.greaterThanOrEqualTo(
                    listing.get(KM_LIMIT_PER_DAY),
                    criteria.getMinKmLimitPerDay()
            ));
        }

        return predicates;
    }

    private List<Integer> resolveMatchingVehicleIds(RentalSearchCriteria criteria) {
        List<VehicleDetails> vehicles = vehicleDetailsRepository.findAll();
        return vehicles.stream()
                .filter(v -> !criteria.hasBrand()
                        || (v.getBrand() != null && v.getBrand().equalsIgnoreCase(criteria.getBrand().trim())))
                .filter(v -> !criteria.hasModel()
                        || (v.getModel() != null && v.getModel().equalsIgnoreCase(criteria.getModel().trim())))
                .map(VehicleDetails::getId)
                .toList();
    }

    private Order buildOrder(CriteriaBuilder cb, Root<CarRentalListing> listing, RentalSearchCriteria criteria) {
        String sortBy = criteria.getSortByOrDefault();
        String sortDirection = criteria.getSortDirectionOrDefault();

        Expression<?> sortExpression = listing.get(sortBy);

        if ("DESC".equals(sortDirection)) {
            return cb.desc(sortExpression);
        } else {
            return cb.asc(sortExpression);
        }
    }

    private long executeCountQuery(CriteriaBuilder cb, RentalSearchCriteria criteria) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<CarRentalListing> countRoot = countQuery.from(CarRentalListing.class);

        List<Predicate> predicates = buildPredicates(cb, countRoot, criteria);

        countQuery.select(cb.count(countRoot));
        countQuery.where(cb.and(predicates.toArray(new Predicate[0])));

        return entityManager.createQuery(countQuery).getSingleResult();
    }
}
