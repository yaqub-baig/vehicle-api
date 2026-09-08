-- =============================================================================
-- Migration: backfill company_type for existing companies
-- =============================================================================
-- Adds:
--   company_type              (company_id, type)  -- @ElementCollection Set<CompanyType>
--   company_authorized_brand  (company_id, brand) -- @ElementCollection Set<String>
--   company.showroom_address        VARCHAR(500) NULL
--   company.rta_approval_number     VARCHAR(100) NULL
--
-- Only the company_type BACKFILL needs this script. The tables and the two nullable
-- columns are created automatically by Hibernate (spring.jpa.hibernate.ddl-auto=update)
-- on the first boot of the new code. Every existing company row must end up with at
-- least one type; this script gives type-less companies FLEET_OWNER (the default).
--
-- HOW TO RUN
--   Fresh database: nothing to do - boot the app with ddl-auto=update.
--   Existing database:
--     1. Deploy the new application code.
--     2. Boot the app ONCE with ddl-auto=update so Hibernate creates the
--        company_type / company_authorized_brand tables and the new company columns.
--     3. Run this script against the auto_adda_vehicle database.
--     4. (App can keep running; this is an INSERT-only backfill.)
--
-- TARGET: MySQL 5.7+ / 8.x
-- SAFE TO RE-RUN: yes (guarded on the table existing; INSERT ... WHERE NOT EXISTS).
-- =============================================================================

DROP PROCEDURE IF EXISTS backfill_company_type;

DELIMITER $$
CREATE PROCEDURE backfill_company_type()
BEGIN
    IF EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.TABLES
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME   = 'company_type'
    ) THEN
        INSERT INTO company_type (company_id, type)
        SELECT c.id, 'FLEET_OWNER'
        FROM company c
        WHERE NOT EXISTS (
            SELECT 1 FROM company_type ct WHERE ct.company_id = c.id
        );
    END IF;
END$$
DELIMITER ;

CALL backfill_company_type();
DROP PROCEDURE backfill_company_type;

-- =============================================================================
-- VERIFY (run these to confirm)
-- =============================================================================
-- SELECT COUNT(*) AS companies_without_type
--   FROM company c
--   WHERE NOT EXISTS (SELECT 1 FROM company_type ct WHERE ct.company_id = c.id);   -- expect 0
-- SELECT type, COUNT(*) FROM company_type GROUP BY type;
