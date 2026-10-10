-- =============================================================================
-- Migration: set plate colours on colour-type plate codes
-- =============================================================================
-- Plate previews are drawn by the frontend from:
--   plate_source.image_id  - one rectangular PNG template per emirate (transparent plate body)
--   plate_code.color_hex   - set for colour codes (GREEN, RED, ...): the frontend fills the
--                            plate body with this colour. NULL for text codes (A, 1, ...), which
--                            are printed on the plate instead.
--
-- Both columns are created automatically by Hibernate (spring.jpa.hibernate.ddl-auto=update)
-- on first boot of the new code. This script only gives the seeded colour codes a starting
-- hex value; admins can fine-tune any of them later via PUT /plate-codes/{id}.
--
-- WHAT THIS SCRIPT DOES
--   Sets color_hex on every seeded plate_code whose code is a colour name. Codes that are
--   not colours (letters, numbers, CLASSIC, TOWER) are left NULL.
--   WHITE&GREEN (Ras Al Khaimah taxi) has no single colour; it is given green.
--
-- HOW TO RUN
--   1. Deploy the new application code and boot it once with ddl-auto=update.
--   2. Run this script against the auto_adda_vehicle database.
--
-- TARGET: MySQL 5.7+ / 8.x
-- SAFE TO RE-RUN: yes (only touches rows whose color_hex is still NULL, so admin edits
-- are never overwritten).
-- =============================================================================

SET @prior_sql_safe_updates = @@SESSION.sql_safe_updates;
SET SQL_SAFE_UPDATES = 0;

UPDATE plate_code pc
JOIN (
    SELECT 'WHITE' AS code, '#FFFFFF' AS color_hex
    UNION ALL SELECT 'GREY', '#8C8C8C'
    UNION ALL SELECT 'GREEN', '#1B7F3B'
    UNION ALL SELECT 'DARK_GREEN', '#0B4F2A'
    UNION ALL SELECT 'RED', '#C8102E'
    UNION ALL SELECT 'BLUE', '#0057B8'
    UNION ALL SELECT 'YELLOW', '#FFD100'
    UNION ALL SELECT 'BLACK', '#1A1A1A'
    UNION ALL SELECT 'ORANGE', '#F28C28'
    UNION ALL SELECT 'BROWN', '#7B4A2A'
    UNION ALL SELECT 'WHITE&GREEN', '#1B7F3B'
) c ON c.code = pc.code
SET pc.color_hex = c.color_hex
WHERE pc.color_hex IS NULL;

SET SQL_SAFE_UPDATES = @prior_sql_safe_updates;

-- =============================================================================
-- VERIFY (run this to confirm)
-- =============================================================================
-- SELECT ps.code AS source, pt.code AS type, pc.code, pc.color_hex
--   FROM plate_code pc
--   JOIN plate_type pt ON pt.id = pc.plate_type_id
--   JOIN plate_source ps ON ps.id = pt.plate_source_id
--  WHERE pc.color_hex IS NOT NULL
--  ORDER BY ps.display_order, pt.display_order, pc.display_order;  -- expect 36 rows
