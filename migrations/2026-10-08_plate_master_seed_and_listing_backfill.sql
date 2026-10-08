-- =============================================================================
-- Migration: seed plate master data (plate_source -> plate_type -> plate_code)
-- and move number_plate_listing off its free-text emirate/category columns
-- =============================================================================
-- NumberPlateListing moved from two free-text columns (emirate, category) to
-- three FK columns pointing at the new plate master lookups:
--   plate_source_id -> plate_source (the issuing emirate, e.g. DUBAI)
--   plate_type_id   -> plate_type   (Private, Taxi, ... - scoped per source)
--   plate_code_id   -> plate_code   (A, 1, WHITE, ... - scoped per type; NULL for
--                                    types that have no codes)
-- Each lookup has its own admin CRUD endpoints (/plate-sources, /plate-types,
-- /plate-codes) plus GET /plate-sources/tree for the full nested hierarchy.
--
-- The plate_source/plate_type/plate_code tables and the three new
-- number_plate_listing FK columns are created automatically by Hibernate
-- (spring.jpa.hibernate.ddl-auto=update) on first boot of the new code.
-- This script relaxes the old columns, seeds reference data and backfills
-- existing rows.
--
-- WHAT THIS SCRIPT DOES
--   1. Makes number_plate_listing.emirate / category nullable. They were NOT NULL
--      with no default and the entity no longer writes them, so listing creation
--      fails with "Field 'emirate' doesn't have a default value" until this runs.
--   2. Seeds 7 plate_source rows, 61 plate_type rows and 148 plate_code rows.
--   3. Points existing listings' plate_source_id / plate_type_id at the seeded rows,
--      matching emirate against plate_source name or code, and category against
--      plate_type name or code within that source. plate_code_id stays NULL (there
--      was no old data for it). Rows that don't match stay NULL - see VERIFY below
--      and fix them from the panel.
--
-- emirate / category are NOT dropped by this script - they're left in place as a
-- reference until the backfill is verified, then removed in a later cleanup migration.
--
-- HOW TO RUN
--   1. Deploy the new application code.
--   2. Boot the app ONCE with ddl-auto=update so Hibernate creates the three new
--      lookup tables and the new number_plate_listing FK columns.
--   3. Run this script against the auto_adda_vehicle database straight away
--      (step 1 unblocks listing creation).
--
-- TARGET: MySQL 5.7+ / 8.x
-- SAFE TO RE-RUN: yes (every INSERT is guarded with WHERE NOT EXISTS, every UPDATE
-- only touches rows whose FK is still NULL).
-- =============================================================================

-- The backfill UPDATEs below don't filter on a key column, which MySQL Workbench's
-- "safe update mode" (sql_safe_updates) rejects (Error Code: 1175). Disable it for
-- this script, then restore it at the end.
SET @prior_sql_safe_updates = @@SESSION.sql_safe_updates;
SET SQL_SAFE_UPDATES = 0;

-- -----------------------------------------------------------------------------
-- 1. Relax the old free-text columns
-- -----------------------------------------------------------------------------
ALTER TABLE number_plate_listing
    MODIFY COLUMN emirate VARCHAR(255) NULL,
    MODIFY COLUMN category VARCHAR(255) NULL;

-- -----------------------------------------------------------------------------
-- 2. Seed plate master data
-- -----------------------------------------------------------------------------
-- Renames a UMM_AL_QWAIN row seeded by an earlier draft of this script, so the guarded
-- INSERT below doesn't create a duplicate Umm Al Quwain source.
UPDATE plate_source SET code = 'UMM_AL_QUWAIN' WHERE code = 'UMM_AL_QWAIN';

INSERT INTO plate_source (code, name, display_order, is_active, created_at, updated_at)
SELECT v.code, v.name, v.display_order, 1, NOW(), NOW()
FROM (
    SELECT 'ABU_DHABI' AS code, 'Abu Dhabi' AS name, 1 AS display_order
    UNION ALL SELECT 'DUBAI', 'Dubai', 2
    UNION ALL SELECT 'SHARJAH', 'Sharjah', 3
    UNION ALL SELECT 'AJMAN', 'Ajman', 4
    UNION ALL SELECT 'UMM_AL_QUWAIN', 'Umm Al Quwain', 5
    UNION ALL SELECT 'RAS_AL_KHAIMAH', 'Ras Al Khaimah', 6
    UNION ALL SELECT 'FUJAIRAH', 'Fujairah', 7
) v
WHERE NOT EXISTS (SELECT 1 FROM plate_source ps WHERE ps.code = v.code);

INSERT INTO plate_type (code, name, plate_source_id, display_order, is_active, created_at, updated_at)
SELECT v.type_code, v.type_name, ps.id, v.display_order, 1, NOW(), NOW()
FROM (
    SELECT 'ABU_DHABI' AS source_code, 'PRIVATE' AS type_code, 'Private' AS type_name, 1 AS display_order
    UNION ALL SELECT 'ABU_DHABI', 'MOTORCYCLE', 'Motorcycle', 2
    UNION ALL SELECT 'ABU_DHABI', 'COMMERCIAL_MOTORCYCLE', 'Commercial Motorcycle', 3
    UNION ALL SELECT 'ABU_DHABI', 'TAXI', 'Taxi', 4
    UNION ALL SELECT 'ABU_DHABI', 'PUBLIC', 'Public', 5
    UNION ALL SELECT 'ABU_DHABI', 'PUBLIC_TRANSPORT', 'Public Transport', 6
    UNION ALL SELECT 'ABU_DHABI', 'GOVERNMENT', 'Government', 7
    UNION ALL SELECT 'ABU_DHABI', 'COMMERCIAL', 'Commercial', 8
    UNION ALL SELECT 'ABU_DHABI', 'UNDER_TEST', 'Under Test', 9
    UNION ALL SELECT 'ABU_DHABI', 'DIPLOMATIC', 'Diplomatic', 10
    UNION ALL SELECT 'ABU_DHABI', 'CONSULAR', 'Consular', 11
    UNION ALL SELECT 'ABU_DHABI', 'INTERNATIONAL_ORGANIZATION', 'International Organization', 12
    UNION ALL SELECT 'ABU_DHABI', 'EXPORT', 'Export', 13
    UNION ALL SELECT 'DUBAI', 'PRIVATE', 'Private', 1
    UNION ALL SELECT 'DUBAI', 'MOTORCYCLE', 'Motorcycle', 2
    UNION ALL SELECT 'DUBAI', 'TAXI', 'Taxi', 3
    UNION ALL SELECT 'DUBAI', 'GOVERNMENT', 'Government', 4
    UNION ALL SELECT 'DUBAI', 'PUBLIC_TRANSPORT', 'Public Transportation', 5
    UNION ALL SELECT 'DUBAI', 'PRIVATE_TRANSPORT', 'Private Transportation', 6
    UNION ALL SELECT 'DUBAI', 'LEARNING_VEHICLE', 'Learning Vehicle', 7
    UNION ALL SELECT 'DUBAI', 'EXPORT', 'Export', 8
    UNION ALL SELECT 'DUBAI', 'IMPORT', 'Import', 9
    UNION ALL SELECT 'DUBAI', 'TRADE', 'Trade', 10
    UNION ALL SELECT 'DUBAI', 'CLASSIC', 'Classic', 11
    UNION ALL SELECT 'DUBAI', 'UNDER_TEST', 'Under Test', 12
    UNION ALL SELECT 'DUBAI', 'POLICE', 'Police', 13
    UNION ALL SELECT 'DUBAI', 'INTERNATIONAL_ORGANIZATION', 'International Organization', 14
    UNION ALL SELECT 'DUBAI', 'CONSULAR', 'Consular', 15
    UNION ALL SELECT 'DUBAI', 'PROTOCOL', 'Protocol', 16
    UNION ALL SELECT 'SHARJAH', 'PRIVATE', 'Private', 1
    UNION ALL SELECT 'SHARJAH', 'MOTORCYCLE', 'Motorcycle', 2
    UNION ALL SELECT 'SHARJAH', 'TAXI', 'Taxi', 3
    UNION ALL SELECT 'SHARJAH', 'PUBLIC', 'Public', 4
    UNION ALL SELECT 'SHARJAH', 'GOVERNMENT', 'Government', 5
    UNION ALL SELECT 'SHARJAH', 'COMMERCIAL', 'Commercial', 6
    UNION ALL SELECT 'SHARJAH', 'CLASSIC', 'Classic', 7
    UNION ALL SELECT 'AJMAN', 'PRIVATE', 'Private', 1
    UNION ALL SELECT 'AJMAN', 'MOTORCYCLE', 'Motorcycle', 2
    UNION ALL SELECT 'AJMAN', 'TAXI', 'Taxi', 3
    UNION ALL SELECT 'AJMAN', 'PUBLIC', 'Public', 4
    UNION ALL SELECT 'AJMAN', 'GOVERNMENT', 'Government', 5
    UNION ALL SELECT 'AJMAN', 'COMMERCIAL', 'Commercial', 6
    UNION ALL SELECT 'AJMAN', 'CLASSIC', 'Classic', 7
    UNION ALL SELECT 'UMM_AL_QUWAIN', 'PRIVATE', 'Private', 1
    UNION ALL SELECT 'UMM_AL_QUWAIN', 'MOTORCYCLE', 'Motorcycle', 2
    UNION ALL SELECT 'UMM_AL_QUWAIN', 'TAXI', 'Taxi', 3
    UNION ALL SELECT 'UMM_AL_QUWAIN', 'PUBLIC', 'Public', 4
    UNION ALL SELECT 'UMM_AL_QUWAIN', 'GOVERNMENT', 'Government', 5
    UNION ALL SELECT 'UMM_AL_QUWAIN', 'COMMERCIAL', 'Commercial', 6
    UNION ALL SELECT 'RAS_AL_KHAIMAH', 'PRIVATE', 'Private', 1
    UNION ALL SELECT 'RAS_AL_KHAIMAH', 'MOTORCYCLE', 'Motorcycle', 2
    UNION ALL SELECT 'RAS_AL_KHAIMAH', 'TAXI', 'Taxi', 3
    UNION ALL SELECT 'RAS_AL_KHAIMAH', 'PUBLIC', 'Public', 4
    UNION ALL SELECT 'RAS_AL_KHAIMAH', 'GOVERNMENT', 'Government', 5
    UNION ALL SELECT 'RAS_AL_KHAIMAH', 'COMMERCIAL', 'Commercial', 6
    UNION ALL SELECT 'FUJAIRAH', 'PRIVATE', 'Private', 1
    UNION ALL SELECT 'FUJAIRAH', 'MOTORCYCLE', 'Motorcycle', 2
    UNION ALL SELECT 'FUJAIRAH', 'TAXI', 'Taxi', 3
    UNION ALL SELECT 'FUJAIRAH', 'PUBLIC', 'Public', 4
    UNION ALL SELECT 'FUJAIRAH', 'GOVERNMENT', 'Government', 5
    UNION ALL SELECT 'FUJAIRAH', 'COMMERCIAL', 'Commercial', 6
) v
JOIN plate_source ps ON ps.code = v.source_code
WHERE NOT EXISTS (
    SELECT 1 FROM plate_type pt WHERE pt.plate_source_id = ps.id AND pt.code = v.type_code
);

INSERT INTO plate_code (code, plate_type_id, display_order, is_active, created_at, updated_at)
SELECT v.code, pt.id, v.display_order, 1, NOW(), NOW()
FROM (
    SELECT 'ABU_DHABI' AS source_code, 'PRIVATE' AS type_code, '1' AS code, 1 AS display_order
    UNION ALL SELECT 'ABU_DHABI', 'PRIVATE', '4', 2
    UNION ALL SELECT 'ABU_DHABI', 'PRIVATE', '5', 3
    UNION ALL SELECT 'ABU_DHABI', 'PRIVATE', '6', 4
    UNION ALL SELECT 'ABU_DHABI', 'PRIVATE', '7', 5
    UNION ALL SELECT 'ABU_DHABI', 'PRIVATE', '8', 6
    UNION ALL SELECT 'ABU_DHABI', 'PRIVATE', '9', 7
    UNION ALL SELECT 'ABU_DHABI', 'PRIVATE', '10', 8
    UNION ALL SELECT 'ABU_DHABI', 'PRIVATE', '11', 9
    UNION ALL SELECT 'ABU_DHABI', 'PRIVATE', '12', 10
    UNION ALL SELECT 'ABU_DHABI', 'PRIVATE', '13', 11
    UNION ALL SELECT 'ABU_DHABI', 'PRIVATE', '14', 12
    UNION ALL SELECT 'ABU_DHABI', 'PRIVATE', '15', 13
    UNION ALL SELECT 'ABU_DHABI', 'PRIVATE', '16', 14
    UNION ALL SELECT 'ABU_DHABI', 'PRIVATE', '17', 15
    UNION ALL SELECT 'ABU_DHABI', 'PRIVATE', '18', 16
    UNION ALL SELECT 'ABU_DHABI', 'PRIVATE', '19', 17
    UNION ALL SELECT 'ABU_DHABI', 'PRIVATE', '20', 18
    UNION ALL SELECT 'ABU_DHABI', 'PRIVATE', '21', 19
    UNION ALL SELECT 'ABU_DHABI', 'PRIVATE', '22', 20
    UNION ALL SELECT 'ABU_DHABI', 'PRIVATE', '50', 21
    UNION ALL SELECT 'ABU_DHABI', 'PRIVATE', 'GREY', 22
    UNION ALL SELECT 'ABU_DHABI', 'PRIVATE', 'GREEN', 23
    UNION ALL SELECT 'ABU_DHABI', 'PRIVATE', 'RED', 24
    UNION ALL SELECT 'ABU_DHABI', 'PRIVATE', 'BLUE', 25
    UNION ALL SELECT 'ABU_DHABI', 'MOTORCYCLE', '4', 1
    UNION ALL SELECT 'ABU_DHABI', 'MOTORCYCLE', 'RED', 2
    UNION ALL SELECT 'ABU_DHABI', 'MOTORCYCLE', 'WHITE', 3
    UNION ALL SELECT 'ABU_DHABI', 'COMMERCIAL_MOTORCYCLE', 'YELLOW', 1
    UNION ALL SELECT 'ABU_DHABI', 'TAXI', 'GREEN', 1
    UNION ALL SELECT 'ABU_DHABI', 'TAXI', 'YELLOW', 2
    UNION ALL SELECT 'ABU_DHABI', 'PUBLIC', '1', 1
    UNION ALL SELECT 'ABU_DHABI', 'PUBLIC', '2', 2
    UNION ALL SELECT 'ABU_DHABI', 'EXPORT', 'BLUE', 1
    UNION ALL SELECT 'DUBAI', 'PRIVATE', 'WHITE', 1
    UNION ALL SELECT 'DUBAI', 'PRIVATE', 'A', 2
    UNION ALL SELECT 'DUBAI', 'PRIVATE', 'B', 3
    UNION ALL SELECT 'DUBAI', 'PRIVATE', 'C', 4
    UNION ALL SELECT 'DUBAI', 'PRIVATE', 'D', 5
    UNION ALL SELECT 'DUBAI', 'PRIVATE', 'E', 6
    UNION ALL SELECT 'DUBAI', 'PRIVATE', 'F', 7
    UNION ALL SELECT 'DUBAI', 'PRIVATE', 'G', 8
    UNION ALL SELECT 'DUBAI', 'PRIVATE', 'H', 9
    UNION ALL SELECT 'DUBAI', 'PRIVATE', 'I', 10
    UNION ALL SELECT 'DUBAI', 'PRIVATE', 'J', 11
    UNION ALL SELECT 'DUBAI', 'PRIVATE', 'K', 12
    UNION ALL SELECT 'DUBAI', 'PRIVATE', 'L', 13
    UNION ALL SELECT 'DUBAI', 'PRIVATE', 'M', 14
    UNION ALL SELECT 'DUBAI', 'PRIVATE', 'N', 15
    UNION ALL SELECT 'DUBAI', 'PRIVATE', 'O', 16
    UNION ALL SELECT 'DUBAI', 'PRIVATE', 'P', 17
    UNION ALL SELECT 'DUBAI', 'PRIVATE', 'Q', 18
    UNION ALL SELECT 'DUBAI', 'PRIVATE', 'R', 19
    UNION ALL SELECT 'DUBAI', 'PRIVATE', 'S', 20
    UNION ALL SELECT 'DUBAI', 'PRIVATE', 'T', 21
    UNION ALL SELECT 'DUBAI', 'PRIVATE', 'U', 22
    UNION ALL SELECT 'DUBAI', 'PRIVATE', 'V', 23
    UNION ALL SELECT 'DUBAI', 'PRIVATE', 'W', 24
    UNION ALL SELECT 'DUBAI', 'PRIVATE', 'X', 25
    UNION ALL SELECT 'DUBAI', 'PRIVATE', 'Y', 26
    UNION ALL SELECT 'DUBAI', 'PRIVATE', 'Z', 27
    UNION ALL SELECT 'DUBAI', 'PRIVATE', 'AA', 28
    UNION ALL SELECT 'DUBAI', 'PRIVATE', 'BB', 29
    UNION ALL SELECT 'DUBAI', 'PRIVATE', 'CC', 30
    UNION ALL SELECT 'DUBAI', 'PRIVATE', 'DD', 31
    UNION ALL SELECT 'DUBAI', 'PRIVATE', 'EE', 32
    UNION ALL SELECT 'DUBAI', 'MOTORCYCLE', '1', 1
    UNION ALL SELECT 'DUBAI', 'MOTORCYCLE', '2', 2
    UNION ALL SELECT 'DUBAI', 'MOTORCYCLE', '3', 3
    UNION ALL SELECT 'DUBAI', 'MOTORCYCLE', '9', 4
    UNION ALL SELECT 'DUBAI', 'TAXI', 'BLACK', 1
    UNION ALL SELECT 'DUBAI', 'TAXI', 'YELLOW', 2
    UNION ALL SELECT 'DUBAI', 'EXPORT', 'BLUE', 1
    UNION ALL SELECT 'DUBAI', 'CLASSIC', 'CLASSIC', 1
    UNION ALL SELECT 'DUBAI', 'POLICE', 'DARK_GREEN', 1
    UNION ALL SELECT 'SHARJAH', 'PRIVATE', '1', 1
    UNION ALL SELECT 'SHARJAH', 'PRIVATE', '2', 2
    UNION ALL SELECT 'SHARJAH', 'PRIVATE', '3', 3
    UNION ALL SELECT 'SHARJAH', 'PRIVATE', '4', 4
    UNION ALL SELECT 'SHARJAH', 'PRIVATE', 'ORANGE', 5
    UNION ALL SELECT 'SHARJAH', 'PRIVATE', 'WHITE', 6
    UNION ALL SELECT 'SHARJAH', 'MOTORCYCLE', '13', 1
    UNION ALL SELECT 'SHARJAH', 'MOTORCYCLE', 'BROWN', 2
    UNION ALL SELECT 'SHARJAH', 'MOTORCYCLE', 'WHITE', 3
    UNION ALL SELECT 'SHARJAH', 'TAXI', 'GREEN', 1
    UNION ALL SELECT 'SHARJAH', 'PUBLIC', '1', 1
    UNION ALL SELECT 'SHARJAH', 'PUBLIC', 'GREEN', 2
    UNION ALL SELECT 'SHARJAH', 'CLASSIC', '1', 1
    UNION ALL SELECT 'AJMAN', 'PRIVATE', 'A', 1
    UNION ALL SELECT 'AJMAN', 'PRIVATE', 'B', 2
    UNION ALL SELECT 'AJMAN', 'PRIVATE', 'C', 3
    UNION ALL SELECT 'AJMAN', 'PRIVATE', 'D', 4
    UNION ALL SELECT 'AJMAN', 'PRIVATE', 'E', 5
    UNION ALL SELECT 'AJMAN', 'PRIVATE', 'H', 6
    UNION ALL SELECT 'AJMAN', 'PRIVATE', 'CLASSIC', 7
    UNION ALL SELECT 'AJMAN', 'PRIVATE', 'ORANGE', 8
    UNION ALL SELECT 'AJMAN', 'PRIVATE', 'WHITE', 9
    UNION ALL SELECT 'AJMAN', 'MOTORCYCLE', '3', 1
    UNION ALL SELECT 'AJMAN', 'MOTORCYCLE', 'WHITE', 2
    UNION ALL SELECT 'AJMAN', 'TAXI', 'GREEN', 1
    UNION ALL SELECT 'AJMAN', 'CLASSIC', 'CLASSIC', 1
    UNION ALL SELECT 'UMM_AL_QUWAIN', 'PRIVATE', 'A', 1
    UNION ALL SELECT 'UMM_AL_QUWAIN', 'PRIVATE', 'B', 2
    UNION ALL SELECT 'UMM_AL_QUWAIN', 'PRIVATE', 'C', 3
    UNION ALL SELECT 'UMM_AL_QUWAIN', 'PRIVATE', 'D', 4
    UNION ALL SELECT 'UMM_AL_QUWAIN', 'PRIVATE', 'E', 5
    UNION ALL SELECT 'UMM_AL_QUWAIN', 'PRIVATE', 'F', 6
    UNION ALL SELECT 'UMM_AL_QUWAIN', 'PRIVATE', 'G', 7
    UNION ALL SELECT 'UMM_AL_QUWAIN', 'PRIVATE', 'H', 8
    UNION ALL SELECT 'UMM_AL_QUWAIN', 'PRIVATE', 'I', 9
    UNION ALL SELECT 'UMM_AL_QUWAIN', 'PRIVATE', 'X', 10
    UNION ALL SELECT 'UMM_AL_QUWAIN', 'PRIVATE', 'WHITE', 11
    UNION ALL SELECT 'UMM_AL_QUWAIN', 'MOTORCYCLE', 'WHITE', 1
    UNION ALL SELECT 'UMM_AL_QUWAIN', 'TAXI', 'GREEN', 1
    UNION ALL SELECT 'RAS_AL_KHAIMAH', 'PRIVATE', 'A', 1
    UNION ALL SELECT 'RAS_AL_KHAIMAH', 'PRIVATE', 'C', 2
    UNION ALL SELECT 'RAS_AL_KHAIMAH', 'PRIVATE', 'D', 3
    UNION ALL SELECT 'RAS_AL_KHAIMAH', 'PRIVATE', 'I', 4
    UNION ALL SELECT 'RAS_AL_KHAIMAH', 'PRIVATE', 'K', 5
    UNION ALL SELECT 'RAS_AL_KHAIMAH', 'PRIVATE', 'M', 6
    UNION ALL SELECT 'RAS_AL_KHAIMAH', 'PRIVATE', 'N', 7
    UNION ALL SELECT 'RAS_AL_KHAIMAH', 'PRIVATE', 'S', 8
    UNION ALL SELECT 'RAS_AL_KHAIMAH', 'PRIVATE', 'V', 9
    UNION ALL SELECT 'RAS_AL_KHAIMAH', 'PRIVATE', 'Y', 10
    UNION ALL SELECT 'RAS_AL_KHAIMAH', 'PRIVATE', 'WHITE', 11
    UNION ALL SELECT 'RAS_AL_KHAIMAH', 'PRIVATE', 'TOWER', 12
    UNION ALL SELECT 'RAS_AL_KHAIMAH', 'MOTORCYCLE', '1', 1
    UNION ALL SELECT 'RAS_AL_KHAIMAH', 'MOTORCYCLE', '4', 2
    UNION ALL SELECT 'RAS_AL_KHAIMAH', 'MOTORCYCLE', 'WHITE', 3
    UNION ALL SELECT 'RAS_AL_KHAIMAH', 'TAXI', 'WHITE', 1
    UNION ALL SELECT 'RAS_AL_KHAIMAH', 'TAXI', 'WHITE&GREEN', 2
    UNION ALL SELECT 'FUJAIRAH', 'PRIVATE', 'A', 1
    UNION ALL SELECT 'FUJAIRAH', 'PRIVATE', 'B', 2
    UNION ALL SELECT 'FUJAIRAH', 'PRIVATE', 'C', 3
    UNION ALL SELECT 'FUJAIRAH', 'PRIVATE', 'D', 4
    UNION ALL SELECT 'FUJAIRAH', 'PRIVATE', 'E', 5
    UNION ALL SELECT 'FUJAIRAH', 'PRIVATE', 'F', 6
    UNION ALL SELECT 'FUJAIRAH', 'PRIVATE', 'G', 7
    UNION ALL SELECT 'FUJAIRAH', 'PRIVATE', 'K', 8
    UNION ALL SELECT 'FUJAIRAH', 'PRIVATE', 'M', 9
    UNION ALL SELECT 'FUJAIRAH', 'PRIVATE', 'P', 10
    UNION ALL SELECT 'FUJAIRAH', 'PRIVATE', 'R', 11
    UNION ALL SELECT 'FUJAIRAH', 'PRIVATE', 'S', 12
    UNION ALL SELECT 'FUJAIRAH', 'PRIVATE', 'T', 13
    UNION ALL SELECT 'FUJAIRAH', 'PRIVATE', 'WHITE', 14
    UNION ALL SELECT 'FUJAIRAH', 'MOTORCYCLE', 'WHITE', 1
    UNION ALL SELECT 'FUJAIRAH', 'TAXI', 'GREEN', 1
    UNION ALL SELECT 'FUJAIRAH', 'PUBLIC', 'GREEN', 1
) v
JOIN plate_source ps ON ps.code = v.source_code
JOIN plate_type pt ON pt.plate_source_id = ps.id AND pt.code = v.type_code
WHERE NOT EXISTS (
    SELECT 1 FROM plate_code pc WHERE pc.plate_type_id = pt.id AND pc.code = v.code
);


-- -----------------------------------------------------------------------------
-- 3. Backfill existing listings
-- -----------------------------------------------------------------------------
UPDATE number_plate_listing l
JOIN plate_source ps
    ON LOWER(TRIM(l.emirate)) = LOWER(ps.name)
    OR UPPER(REPLACE(TRIM(l.emirate), ' ', '_')) = ps.code
SET l.plate_source_id = ps.id
WHERE l.plate_source_id IS NULL
  AND l.emirate IS NOT NULL;

UPDATE number_plate_listing l
JOIN plate_type pt
    ON pt.plate_source_id = l.plate_source_id
   AND (LOWER(TRIM(l.category)) = LOWER(pt.name)
        OR UPPER(REPLACE(TRIM(l.category), ' ', '_')) = pt.code)
SET l.plate_type_id = pt.id
WHERE l.plate_type_id IS NULL
  AND l.plate_source_id IS NOT NULL
  AND l.category IS NOT NULL;

SET SQL_SAFE_UPDATES = @prior_sql_safe_updates;

-- =============================================================================
-- VERIFY (run these to confirm)
-- =============================================================================
-- SELECT COUNT(*) FROM plate_source;  -- expect 7
-- SELECT COUNT(*) FROM plate_type;    -- expect 61
-- SELECT COUNT(*) FROM plate_code;    -- expect 148
--
-- Listings the backfill couldn't map (fix from the panel):
-- SELECT id, plate_number, emirate, category, plate_source_id, plate_type_id
--   FROM number_plate_listing
--  WHERE plate_source_id IS NULL OR plate_type_id IS NULL;
--
-- Listings whose type requires a code but have none (all backfilled rows of such
-- types - the seller must pick a code on their next edit):
-- SELECT l.id, l.plate_number, ps.code AS source, pt.code AS type
--   FROM number_plate_listing l
--   JOIN plate_source ps ON ps.id = l.plate_source_id
--   JOIN plate_type pt ON pt.id = l.plate_type_id
--  WHERE l.plate_code_id IS NULL
--    AND EXISTS (SELECT 1 FROM plate_code pc WHERE pc.plate_type_id = pt.id AND pc.is_active = 1);
