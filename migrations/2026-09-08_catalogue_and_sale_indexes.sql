-- =============================================================================
-- Migration: indexes for company types, catalogue and vehicle-sale tables
-- =============================================================================
-- All additive, non-unique indexes. Optional but recommended for production - the
-- browse / inbox / "my sales" queries filter on these columns.
--
-- HOW TO RUN
--   Run after the new code has booted once (so the tables exist).
--   Fresh database: run any time after first boot.
--
-- TARGET: MySQL 5.7+ / 8.x
-- SAFE TO RE-RUN: yes (each CREATE INDEX is guarded via INFORMATION_SCHEMA.STATISTICS).
-- =============================================================================

DROP PROCEDURE IF EXISTS add_index_if_missing;

DELIMITER $$
CREATE PROCEDURE add_index_if_missing(IN p_table VARCHAR(64), IN p_index VARCHAR(64), IN p_cols VARCHAR(255))
BEGIN
    IF EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.TABLES
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table
    ) AND NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table AND INDEX_NAME = p_index
    ) THEN
        SET @sql = CONCAT('CREATE INDEX `', p_index, '` ON `', p_table, '` (', p_cols, ')');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

CALL add_index_if_missing('company_type', 'idx_company_type_type', 'type');

CALL add_index_if_missing('catalogue_offering', 'idx_cat_offering_company_status', 'company_id, status');
CALL add_index_if_missing('catalogue_offering', 'idx_cat_offering_status_year', 'status, model_year');

CALL add_index_if_missing('catalogue_quote_request', 'idx_cat_quote_company_status', 'company_id, status');
CALL add_index_if_missing('catalogue_quote_request', 'idx_cat_quote_buyer_status', 'buyer_user_id, status');
CALL add_index_if_missing('catalogue_quote_request', 'idx_cat_quote_offering', 'offering_id');

CALL add_index_if_missing('vehicle_sale', 'idx_vehicle_sale_listing', 'vehicle_listing_id');
CALL add_index_if_missing('vehicle_sale', 'idx_vehicle_sale_new_owner', 'new_owner_id');
CALL add_index_if_missing('vehicle_sale', 'idx_vehicle_sale_prev_owner', 'previous_owner_id');

DROP PROCEDURE add_index_if_missing;

-- =============================================================================
-- VERIFY
-- =============================================================================
-- SELECT TABLE_NAME, INDEX_NAME, GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS cols
--   FROM INFORMATION_SCHEMA.STATISTICS
--   WHERE TABLE_SCHEMA = DATABASE()
--     AND TABLE_NAME IN ('company_type','catalogue_offering','catalogue_quote_request','vehicle_sale')
--   GROUP BY TABLE_NAME, INDEX_NAME;
