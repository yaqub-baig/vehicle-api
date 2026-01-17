package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.VehicleListing;
import com.autoadda.apis.vehicle.request.VehicleSearchCriteria;
import com.autoadda.apis.vehicle.util.Status;
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
import java.util.ArrayList;
import java.util.List;


@Repository
public class VehicleListingRepositoryImpl implements VehicleListingCustomRepository {

    private static final String VEHICLE = "vehicle";
    private static final String STATUS = "status";
    private static final String CREATED_AT = "createdAt";
    public static final String BRAND = "brand";
    public static final String MODEL = "model";

    @PersistenceContext
    private EntityManager entityManager;

    public List<VehicleListing> findByCriteria(VehicleSearchCriteria criteria) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder ( );
        CriteriaQuery<VehicleListing> query = cb.createQuery ( VehicleListing.class );
        Root<VehicleListing> vehicle = query.from ( VehicleListing.class );
        List<Predicate> predicates = new ArrayList<> ( );
        predicates.add ( cb.notEqual (
                vehicle.get ( STATUS ),
                Status.REMOVED
        ) );
        // Add conditions based on provided criteria
        if ( criteria.hasLocation ( ) ) {
            List<String> cities = criteria.getCitiesAsList();

            if (cities.size() == 1) {
                // Single city - use equal for better performance
                predicates.add ( cb.equal (
                        cb.lower ( vehicle.get ( "city" ) ),
                        cities.get(0)
                ) );
            } else {
                // Multiple cities - use IN clause
                predicates.add ( cb.lower ( vehicle.get ( "city" ) ).in ( cities ) );
            }
        }

        if ( criteria.hasPrice ( ) ) {
            predicates.add ( cb.between (
                    vehicle.get ( "price" ),
                    criteria.getFromPrice ( ),
                    criteria.getToPrice ( )
            ) );
        }

        if ( criteria.hasBrand ( ) ) {
            predicates.add ( cb.equal (
                    cb.lower ( vehicle.get ( VEHICLE ).get (BRAND) ),
                    criteria.getBrand ( ).toLowerCase ( ).trim ( )
            ) );
        }

        if ( criteria.hasModel ( ) ) {
            predicates.add ( cb.equal (
                    cb.lower ( vehicle.get ( VEHICLE ).get (MODEL) ),
                    criteria.getModel ( ).toLowerCase ( ).trim ( )
            ) );
        }

        if ( criteria.hasYear ( ) ) {
            predicates.add ( cb.and (
                    cb.greaterThanOrEqualTo (
                            vehicle.get ( VEHICLE ).get ( "year" ).as ( Integer.class ),
                            criteria.getFromYear ( )
                    ),
                    cb.lessThanOrEqualTo (
                            vehicle.get ( VEHICLE ).get ( "year" ).as ( Integer.class ),
                            criteria.getToYear ( )
                    )
            ) );
        }

        // Combine all predicates with AND
        query.where ( cb.and ( predicates.toArray ( new Predicate[0] ) ) )
                .orderBy ( cb.desc ( vehicle.get ( CREATED_AT ) ) );

        return entityManager.createQuery ( query ).getResultList ( );
    }

    public List<VehicleListing> findByAdvancedSearch(String searchQuery) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder ( );
        CriteriaQuery<VehicleListing> query = cb.createQuery ( VehicleListing.class );
        Root<VehicleListing> vehicle = query.from ( VehicleListing.class );

        List<Predicate> predicates = new ArrayList<> ( );

        // Always exclude removed vehicles
        predicates.add ( cb.notEqual (
                vehicle.get ( STATUS ),
                Status.REMOVED
        ) );

        // Split search query into individual words for better matching
        String[] searchWords = searchQuery.toLowerCase ( ).trim ( ).split ( "\\s+" );
        List<Predicate> wordPredicates = new ArrayList<> ( );

        for (String word : searchWords) {
            if ( ! word.isEmpty ( ) ) {
                String wordPattern = "%" + word + "%";

                List<Predicate> fieldPredicates = new ArrayList<> ( );

                // Search each word in multiple fields
                fieldPredicates.add ( cb.like ( cb.lower ( vehicle.get ( "city" ) ), wordPattern ) );
                fieldPredicates.add ( cb.like ( cb.lower ( vehicle.get ( VEHICLE ).get (BRAND) ), wordPattern ) );
                fieldPredicates.add ( cb.like ( cb.lower ( vehicle.get ( VEHICLE ).get (MODEL) ), wordPattern ) );
                fieldPredicates.add ( cb.like ( cb.lower ( vehicle.get ( "description" ) ), wordPattern ) );

                // Each word should match at least one field
                wordPredicates.add ( cb.or ( fieldPredicates.toArray ( new Predicate[0] ) ) );
            }
        }

        // All words should match (AND logic between words, OR logic within each word)
        if ( ! wordPredicates.isEmpty ( ) ) {
            predicates.add ( cb.and ( wordPredicates.toArray ( new Predicate[0] ) ) );
        }

        query.where ( cb.and ( predicates.toArray ( new Predicate[0] ) ) )
                .orderBy ( cb.desc ( vehicle.get ( CREATED_AT ) ) );

        return entityManager.createQuery ( query ).getResultList ( );
    }

    // Paginated methods

    @Override
    public Page<VehicleListing> findByCriteriaWithPagination(VehicleSearchCriteria criteria) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Build data query
        CriteriaQuery<VehicleListing> dataQuery = cb.createQuery(VehicleListing.class);
        Root<VehicleListing> dataRoot = dataQuery.from(VehicleListing.class);

        // Build predicates
        List<Predicate> predicates = buildPredicates(cb, dataRoot, criteria);

        // Apply predicates
        dataQuery.where(cb.and(predicates.toArray(new Predicate[0])));

        // Apply sorting
        Order order = buildOrder(cb, dataRoot, criteria);
        dataQuery.orderBy(order);

        // Create typed query
        TypedQuery<VehicleListing> typedQuery = entityManager.createQuery(dataQuery);

        // Apply pagination
        int page = criteria.getPageOrDefault();
        int size = criteria.getSizeOrDefault();
        typedQuery.setFirstResult(page * size);
        typedQuery.setMaxResults(size);

        // Execute data query
        List<VehicleListing> results = typedQuery.getResultList();

        // Execute count query
        long total = executeCountQuery(cb, predicates);

        // Return PageImpl
        return new PageImpl<>(results, PageRequest.of(page, size), total);
    }

    @Override
    public Page<VehicleListing> findByAdvancedSearchWithPagination(String searchQuery, VehicleSearchCriteria criteria) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Build data query
        CriteriaQuery<VehicleListing> dataQuery = cb.createQuery(VehicleListing.class);
        Root<VehicleListing> dataRoot = dataQuery.from(VehicleListing.class);

        // Build search predicates
        List<Predicate> predicates = buildAdvancedSearchPredicates(cb, dataRoot, searchQuery);

        // Apply predicates
        dataQuery.where(cb.and(predicates.toArray(new Predicate[0])));

        // Apply sorting
        Order order = buildOrder(cb, dataRoot, criteria);
        dataQuery.orderBy(order);

        // Create typed query
        TypedQuery<VehicleListing> typedQuery = entityManager.createQuery(dataQuery);

        // Apply pagination
        int page = criteria.getPageOrDefault();
        int size = criteria.getSizeOrDefault();
        typedQuery.setFirstResult(page * size);
        typedQuery.setMaxResults(size);

        // Execute data query
        List<VehicleListing> results = typedQuery.getResultList();

        // Execute count query
        long total = executeCountQuery(cb, predicates);

        // Return PageImpl
        return new PageImpl<>(results, PageRequest.of(page, size), total);
    }

    // Helper methods

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<VehicleListing> vehicle, VehicleSearchCriteria criteria) {
        List<Predicate> predicates = new ArrayList<>();

        // Always exclude removed vehicles
        predicates.add(cb.notEqual(vehicle.get(STATUS), Status.REMOVED));

        // Add conditions based on provided criteria
        if (criteria.hasLocation()) {
            List<String> cities = criteria.getCitiesAsList();

            if (cities.size() == 1) {
                // Single city - use equal for better performance
                predicates.add(cb.equal(
                        cb.lower(vehicle.get("city")),
                        cities.get(0)
                ));
            } else {
                // Multiple cities - use IN clause
                predicates.add(cb.lower(vehicle.get("city")).in(cities));
            }
        }

        if (criteria.hasPrice()) {
            predicates.add(cb.between(
                    vehicle.get("price"),
                    criteria.getFromPrice(),
                    criteria.getToPrice()
            ));
        }

        if (criteria.hasBrand()) {
            predicates.add(cb.equal(
                    cb.lower(vehicle.get(VEHICLE).get(BRAND)),
                    criteria.getBrand().toLowerCase().trim()
            ));
        }

        if (criteria.hasModel()) {
            predicates.add(cb.equal(
                    cb.lower(vehicle.get(VEHICLE).get(MODEL)),
                    criteria.getModel().toLowerCase().trim()
            ));
        }

        if (criteria.hasYear()) {
            predicates.add(cb.and(
                    cb.greaterThanOrEqualTo(
                            vehicle.get(VEHICLE).get("year").as(Integer.class),
                            criteria.getFromYear()
                    ),
                    cb.lessThanOrEqualTo(
                            vehicle.get(VEHICLE).get("year").as(Integer.class),
                            criteria.getToYear()
                    )
            ));
        }

        return predicates;
    }

    private List<Predicate> buildAdvancedSearchPredicates(CriteriaBuilder cb, Root<VehicleListing> vehicle, String searchQuery) {
        List<Predicate> predicates = new ArrayList<>();

        // Always exclude removed vehicles
        predicates.add(cb.notEqual(vehicle.get(STATUS), Status.REMOVED));

        // Split search query into individual words
        String[] searchWords = searchQuery.toLowerCase().trim().split("\\s+");
        List<Predicate> wordPredicates = new ArrayList<>();

        for (String word : searchWords) {
            if (!word.isEmpty()) {
                String wordPattern = "%" + word + "%";

                List<Predicate> fieldPredicates = new ArrayList<>();

                // Search each word in multiple fields
                fieldPredicates.add(cb.like(cb.lower(vehicle.get("city")), wordPattern));
                fieldPredicates.add(cb.like(cb.lower(vehicle.get(VEHICLE).get(BRAND)), wordPattern));
                fieldPredicates.add(cb.like(cb.lower(vehicle.get(VEHICLE).get(MODEL)), wordPattern));
                fieldPredicates.add(cb.like(cb.lower(vehicle.get("description")), wordPattern));

                // Each word should match at least one field
                wordPredicates.add(cb.or(fieldPredicates.toArray(new Predicate[0])));
            }
        }

        // All words should match
        if (!wordPredicates.isEmpty()) {
            predicates.add(cb.and(wordPredicates.toArray(new Predicate[0])));
        }

        return predicates;
    }

    private Order buildOrder(CriteriaBuilder cb, Root<VehicleListing> vehicle, VehicleSearchCriteria criteria) {
        String sortBy = criteria.getSortByOrDefault();
        String sortDirection = criteria.getSortDirectionOrDefault();

        Expression<?> sortExpression = vehicle.get(sortBy);

        if ("DESC".equals(sortDirection)) {
            return cb.desc(sortExpression);
        } else {
            return cb.asc(sortExpression);
        }
    }

    private long executeCountQuery(CriteriaBuilder cb, List<Predicate> predicates) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<VehicleListing> countRoot = countQuery.from(VehicleListing.class);

        countQuery.select(cb.count(countRoot));
        countQuery.where(cb.and(predicates.toArray(new Predicate[0])));

        return entityManager.createQuery(countQuery).getSingleResult();
    }
}