-- =============================================================================
-- Migration: seed country/state-territory lookups and backfill carpool_listing
-- addresses from the old free-text city columns
-- =============================================================================
-- CarpoolListing moved from two free-text columns (origin_city, destination_city)
-- to a full address hierarchy: country -> state_territory -> territory_area -> street
-- (four new lookup tables, each with its own admin CRUD endpoints), embedded twice
-- on carpool_listing as origin_* / destination_* FK columns.
--
-- The country/state_territory/territory_area/street tables and the new
-- carpool_listing FK columns (origin_country_id, origin_state_territory_id,
-- origin_territory_area_id, origin_street_id, origin_google_maps_url, and the
-- destination_* equivalents) are created automatically by Hibernate
-- (spring.jpa.hibernate.ddl-auto=update) on first boot of the new code.
-- This script only seeds starting reference data and backfills existing rows.
--
-- WHAT THIS SCRIPT DOES
--   1. Seeds one country row: UAE / United Arab Emirates.
--   2. Seeds the 7 emirates as state_territory rows under UAE.
--   3. For every distinct existing origin_city/destination_city value that has no
--      matching territory_area yet, creates one - defaulting its state_territory to
--      DUBAI. This is a best-effort placeholder so existing listings keep working;
--      an admin should review and correct the state/area assignment for backfilled
--      rows from the panel afterwards.
--   4. Points carpool_listing.origin_country_id/origin_state_territory_id/
--      origin_territory_area_id (and the destination_* equivalents) at the seeded/
--      backfilled rows, matched by territory_area.name = origin_city/destination_city.
--
-- origin_city / destination_city are NOT dropped by this script - they're left in
-- place as a reference until the backfill above is verified, then removed in a
-- later cleanup migration.
--
-- HOW TO RUN
--   Fresh database: nothing to do - boot the app with ddl-auto=update (no existing
--     carpool_listing rows to backfill).
--   Existing database:
--     1. Deploy the new application code.
--     2. Boot the app ONCE with ddl-auto=update so Hibernate creates the four new
--        lookup tables and the new carpool_listing FK columns.
--     3. Run this script against the auto_adda_vehicle database.
--     4. (App can keep running; every write here is guarded/idempotent.)
--
-- TARGET: MySQL 5.7+ / 8.x
-- SAFE TO RE-RUN: yes (every INSERT is guarded with WHERE NOT EXISTS).
-- =============================================================================

-- Two UPDATE statements below don't filter on a key column of the table they update,
-- which MySQL Workbench's "safe update mode" (sql_safe_updates) rejects by default
-- (Error Code: 1175). Disable it for this script, then restore it at the end.
SET @prior_sql_safe_updates = @@SESSION.sql_safe_updates;
SET SQL_SAFE_UPDATES = 0;

-- Renames an already-seeded AE row from an earlier run of this script to UAE, so the
-- INSERT below (guarded on code = 'UAE') doesn't create a duplicate country row.
UPDATE country SET code = 'UAE' WHERE code = 'AE';

INSERT INTO country (code, name, is_active, created_at, updated_at)
SELECT 'UAE', 'United Arab Emirates', 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code = 'UAE');

INSERT INTO state_territory (code, name, country_id, is_active, created_at, updated_at)
SELECT v.code, v.name, c.id, 1, NOW(), NOW()
FROM (
    SELECT 'ABU_DHABI' AS code, 'Abu Dhabi' AS name
    UNION ALL SELECT 'DUBAI', 'Dubai'
    UNION ALL SELECT 'SHARJAH', 'Sharjah'
    UNION ALL SELECT 'AJMAN', 'Ajman'
    UNION ALL SELECT 'UMM_AL_QUWAIN', 'Umm Al Quwain'
    UNION ALL SELECT 'RAS_AL_KHAIMAH', 'Ras Al Khaimah'
    UNION ALL SELECT 'FUJAIRAH', 'Fujairah'
) v
JOIN country c ON c.code = 'UAE'
WHERE NOT EXISTS (
    SELECT 1 FROM state_territory st WHERE st.country_id = c.id AND st.code = v.code
);

-- One territory_area per distinct old city value not already represented, defaulted
-- to DUBAI - reassign the correct state/area from the panel once reviewed.
INSERT INTO territory_area (code, name, state_territory_id, is_active, created_at, updated_at)
SELECT DISTINCT UPPER(REPLACE(TRIM(city.name), ' ', '_')), TRIM(city.name), dubai.id, 1, NOW(), NOW()
FROM (
    SELECT origin_city AS name FROM carpool_listing WHERE origin_city IS NOT NULL
    UNION
    SELECT destination_city AS name FROM carpool_listing WHERE destination_city IS NOT NULL
) city
JOIN state_territory dubai ON dubai.code = 'DUBAI'
WHERE NOT EXISTS (
    SELECT 1 FROM territory_area ta WHERE LOWER(ta.name) = LOWER(TRIM(city.name))
);

UPDATE carpool_listing l
JOIN territory_area ota ON LOWER(ota.name) = LOWER(TRIM(l.origin_city))
JOIN state_territory ost ON ost.id = ota.state_territory_id
JOIN country oc ON oc.id = ost.country_id
JOIN territory_area dta ON LOWER(dta.name) = LOWER(TRIM(l.destination_city))
JOIN state_territory dst ON dst.id = dta.state_territory_id
JOIN country dc ON dc.id = dst.country_id
SET l.origin_country_id = oc.id,
    l.origin_state_territory_id = ost.id,
    l.origin_territory_area_id = ota.id,
    l.destination_country_id = dc.id,
    l.destination_state_territory_id = dst.id,
    l.destination_territory_area_id = dta.id
WHERE l.origin_territory_area_id IS NULL OR l.destination_territory_area_id IS NULL;

SET SQL_SAFE_UPDATES = @prior_sql_safe_updates;

-- =============================================================================
-- VERIFY (run these to confirm)
-- =============================================================================
-- SELECT COUNT(*) AS listings_missing_territory_area
--   FROM carpool_listing
--   WHERE origin_territory_area_id IS NULL OR destination_territory_area_id IS NULL;  -- expect 0
-- SELECT code, name FROM state_territory WHERE country_id = (SELECT id FROM country WHERE code = 'UAE');
-- SELECT ta.name, st.name AS state, ta.id FROM territory_area ta JOIN state_territory st ON st.id = ta.state_territory_id;
