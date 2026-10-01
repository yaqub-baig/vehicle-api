-- =============================================================================
-- Migration: backfill discoverable=1 on existing carpool_listing rows
-- =============================================================================
-- CarpoolListing gained a new `discoverable` column: true for a listing the user
-- actually posted, false for one auto-created behind the scenes to back a direct
-- connection request (POST /carpool-connections with requesterListingDetails
-- instead of requesterListingId - see CarpoolConnectionServiceImpl.
-- createImplicitRequesterListing). browse/matches/my-listings now filter on it.
--
-- The column itself is added automatically by Hibernate
-- (spring.jpa.hibernate.ddl-auto=update) on first boot of the new code, as
-- nullable. This script only backfills existing rows - every one of them was
-- genuinely posted by its owner, so they all become discoverable=1.
--
-- HOW TO RUN
--   Fresh database: nothing to do - boot the app with ddl-auto=update (no
--     existing carpool_listing rows to backfill).
--   Existing database:
--     1. Deploy the new application code.
--     2. Boot the app ONCE with ddl-auto=update so Hibernate adds the
--        discoverable column.
--     3. Run this script against the auto_adda_vehicle database.
--
-- TARGET: MySQL 5.7+ / 8.x
-- SAFE TO RE-RUN: yes (WHERE discoverable IS NULL; a no-op once backfilled).
-- =============================================================================

-- WHERE discoverable IS NULL isn't a key column of carpool_listing, which MySQL
-- Workbench's "safe update mode" rejects by default (Error Code: 1175, same
-- issue as the address backfill migration). Disable it for this statement.
SET @prior_sql_safe_updates = @@SESSION.sql_safe_updates;
SET SQL_SAFE_UPDATES = 0;

UPDATE carpool_listing SET discoverable = 1 WHERE discoverable IS NULL;

SET SQL_SAFE_UPDATES = @prior_sql_safe_updates;

-- =============================================================================
-- VERIFY (run this to confirm)
-- =============================================================================
-- SELECT COUNT(*) AS listings_missing_discoverable
--   FROM carpool_listing WHERE discoverable IS NULL;  -- expect 0
