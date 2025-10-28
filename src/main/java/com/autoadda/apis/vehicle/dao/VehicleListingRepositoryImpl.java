package com.autoadda.apis.vehicle.dao;

import com.autoadda.apis.vehicle.entity.VehicleListing;
import com.autoadda.apis.vehicle.request.VehicleSearchCriteria;
import com.autoadda.apis.vehicle.util.Status;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;


@Repository
public class VehicleListingRepositoryImpl implements VehicleListingCustomRepository {

    private static final String VEHICLE = "vehicle";
    @PersistenceContext
    private EntityManager entityManager;

    public List<VehicleListing> findByCriteria(VehicleSearchCriteria criteria) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder ( );
        CriteriaQuery<VehicleListing> query = cb.createQuery ( VehicleListing.class );
        Root<VehicleListing> vehicle = query.from ( VehicleListing.class );
        List<Predicate> predicates = new ArrayList<> ( );
        predicates.add ( cb.notEqual (
                vehicle.get ( "status" ),
                Status.REMOVED
        ) );
        // Add conditions based on provided criteria
        if ( criteria.hasLocation ( ) ) {
            predicates.add ( cb.equal (
                    cb.lower ( vehicle.get ( "city" ) ),
                    criteria.getLocation ( ).toLowerCase ( ).trim ( )
            ) );
        }

        if ( criteria.hasPrice ( ) ) {
            predicates.add ( cb.lessThanOrEqualTo (
                    vehicle.get ( "price" ),
                    criteria.getPrice ( )
            ) );
        }

        if ( criteria.hasBrand ( ) ) {
            predicates.add ( cb.equal (
                    cb.lower ( vehicle.get ( VEHICLE ).get ( "brand" ) ),
                    criteria.getBrand ( ).toLowerCase ( ).trim ( )
            ) );
        }

        if ( criteria.hasModel ( ) ) {
            predicates.add ( cb.equal (
                    cb.lower ( vehicle.get ( VEHICLE ).get ( "model" ) ),
                    criteria.getModel ( ).toLowerCase ( ).trim ( )
            ) );
        }

        if ( criteria.hasYear ( ) ) {
            predicates.add ( cb.equal (
                    vehicle.get ( VEHICLE ).get ( "year" ),
                    criteria.getYear ( )
            ) );
        }

        // Combine all predicates with AND
        query.where ( cb.and ( predicates.toArray ( new Predicate[0] ) ) )
                .orderBy ( cb.desc ( vehicle.get ( "createdAt" ) ) );

        return entityManager.createQuery ( query ).getResultList ( );
    }

    public List<VehicleListing> findByAdvancedSearch(String searchQuery) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder ( );
        CriteriaQuery<VehicleListing> query = cb.createQuery ( VehicleListing.class );
        Root<VehicleListing> vehicle = query.from ( VehicleListing.class );

        List<Predicate> predicates = new ArrayList<> ( );

        // Always exclude removed vehicles
        predicates.add ( cb.notEqual (
                vehicle.get ( "status" ),
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
                fieldPredicates.add ( cb.like ( cb.lower ( vehicle.get ( VEHICLE ).get ( "brand" ) ), wordPattern ) );
                fieldPredicates.add ( cb.like ( cb.lower ( vehicle.get ( VEHICLE ).get ( "model" ) ), wordPattern ) );
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
                .orderBy ( cb.desc ( vehicle.get ( "createdAt" ) ) );

        return entityManager.createQuery ( query ).getResultList ( );
    }
}