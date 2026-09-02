-- =============================================================================
-- Migration: car_rental_listing.location - new required column
-- =============================================================================
-- Adds:
--   car_rental_listing.location  (VARCHAR(255), NOT NULL)
--
-- The CarRentalListing entity now maps a `location` string (nullable = false),
-- following the CarpoolListing.originCity / NumberPlateListing.emirate pattern.
--
-- HOW TO RUN
--   Fresh database: nothing to do - boot the app with ddl-auto=update and
--   Hibernate creates the NOT NULL column directly.
--
--   Database that already has car_rental_listing rows: ddl-auto=update cannot add
--   a NOT NULL column without a default, so:
--   1. Deploy the new application code.
--   2. Run this script against the auto_adda_vehicle database (it adds the column
--      as nullable, backfills existing rows, then enforces NOT NULL).
--   3. Boot the app.
--
-- TARGET: MySQL 5.7+ / 8.x
-- SAFE TO RE-RUN: yes (all steps guarded via INFORMATION_SCHEMA).
-- =============================================================================

START TRANSACTION;

-- 1) Add the column as nullable if it does not exist yet.
DROP PROCEDURE IF EXISTS add_car_rental_listing_location;

DELIMITER $$
CREATE PROCEDURE add_car_rental_listing_location()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME   = 'car_rental_listing'
          AND COLUMN_NAME  = 'location'
    ) THEN
        ALTER TABLE car_rental_listing ADD COLUMN location VARCHAR(255) NULL;
    END IF;
END$$
DELIMITER ;

CALL add_car_rental_listing_location();
DROP PROCEDURE add_car_rental_listing_location;

-- 2) Backfill existing rows that have no location.
UPDATE car_rental_listing
SET location = 'Unknown'
WHERE location IS NULL;

-- 3) Enforce NOT NULL now that every row has a value.
DROP PROCEDURE IF EXISTS enforce_car_rental_listing_location_not_null;

DELIMITER $$
CREATE PROCEDURE enforce_car_rental_listing_location_not_null()
BEGIN
    IF EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME   = 'car_rental_listing'
          AND COLUMN_NAME  = 'location'
          AND IS_NULLABLE  = 'YES'
    ) THEN
        ALTER TABLE car_rental_listing MODIFY COLUMN location VARCHAR(255) NOT NULL;
    END IF;
END$$
DELIMITER ;

CALL enforce_car_rental_listing_location_not_null();
DROP PROCEDURE enforce_car_rental_listing_location_not_null;

COMMIT;

-- =============================================================================
-- VERIFY (run these to confirm)
-- =============================================================================
-- SHOW COLUMNS FROM car_rental_listing LIKE 'location';          -- Null = NO
-- SELECT COUNT(*) AS missing_location FROM car_rental_listing WHERE location IS NULL;  -- 0
