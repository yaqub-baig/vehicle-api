-- =============================================================================
-- Migration: make carpool_listing's orphaned origin_city/destination_city
-- columns nullable
-- =============================================================================
-- 2026-09-28_carpool_structured_address_backfill.sql moved CarpoolListing off
-- origin_city/destination_city onto the new address-lookup FK columns, but
-- deliberately left the old columns in place (undropped) as a historical
-- reference. They were originally NOT NULL with no default
-- (`@Column(name = "origin_city", nullable = false)`), and the entity no
-- longer sets them on insert - so every new carpool_listing row now fails
-- with "Field 'destination_city' doesn't have a default value" until these
-- are relaxed to nullable.
--
-- HOW TO RUN
--   Run once, immediately, against any database that already has a
--   carpool_listing table (fresh or existing) - this unblocks listing
--   creation. Safe to run before or after the address backfill script.
--
-- TARGET: MySQL 5.7+ / 8.x
-- SAFE TO RE-RUN: yes (MODIFY COLUMN to the same nullability is a no-op).
-- =============================================================================

ALTER TABLE carpool_listing
    MODIFY COLUMN origin_city VARCHAR(255) NULL,
    MODIFY COLUMN destination_city VARCHAR(255) NULL;

-- =============================================================================
-- VERIFY (run this to confirm)
-- =============================================================================
-- SELECT COLUMN_NAME, IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS
--   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'carpool_listing'
--     AND COLUMN_NAME IN ('origin_city', 'destination_city');  -- expect YES, YES
