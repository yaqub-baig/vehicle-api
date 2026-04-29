-- =============================================================================
-- Migration: vehicle_borrowing image fields - single -> list
-- =============================================================================
-- Converts:
--   vehicle_borrowing.borrowing_image_id  (FK -> media_file.id, ManyToOne)
--   vehicle_borrowing.return_image_id     (FK -> media_file.id, ManyToOne)
-- into:
--   media_file.borrowing_images_id        (FK -> vehicle_borrowing.id, OneToMany)
--   media_file.return_images_id           (FK -> vehicle_borrowing.id, OneToMany)
--
-- HOW TO RUN
--   1. Deploy the new application code.
--   2. Boot the app ONCE with ddl-auto=update so Hibernate auto-creates the new
--      columns media_file.borrowing_images_id and media_file.return_images_id
--      (with their FK constraints to vehicle_borrowing.id).
--   3. Stop the app.
--   4. Run this script against the auto_adda_vehicle database.
--   5. Restart the app.
--
-- TARGET: MySQL 5.7+ / 8.x
-- SAFE TO RE-RUN: yes (idempotent UPDATEs and guarded FK/column drops).
-- =============================================================================

START TRANSACTION;

-- 1) Backfill the new join columns from the old singletons.
UPDATE media_file mf
JOIN vehicle_borrowing vb ON vb.borrowing_image_id = mf.id
SET mf.borrowing_images_id = vb.id
WHERE vb.borrowing_image_id IS NOT NULL
  AND mf.borrowing_images_id IS NULL;

UPDATE media_file mf
JOIN vehicle_borrowing vb ON vb.return_image_id = mf.id
SET mf.return_images_id = vb.id
WHERE vb.return_image_id IS NOT NULL
  AND mf.return_images_id IS NULL;

COMMIT;

-- 2) Drop the auto-generated FK constraints on the old columns.
--    Hibernate names them FK<hash>, so we look them up via INFORMATION_SCHEMA.
DROP PROCEDURE IF EXISTS drop_old_borrowing_image_fks;

DELIMITER $$
CREATE PROCEDURE drop_old_borrowing_image_fks()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE fk_name VARCHAR(128);
    DECLARE cur CURSOR FOR
        SELECT CONSTRAINT_NAME
        FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME   = 'vehicle_borrowing'
          AND COLUMN_NAME IN ('borrowing_image_id', 'return_image_id')
          AND REFERENCED_TABLE_NAME IS NOT NULL;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cur;
    fk_loop: LOOP
        FETCH cur INTO fk_name;
        IF done THEN LEAVE fk_loop; END IF;
        SET @sql = CONCAT('ALTER TABLE vehicle_borrowing DROP FOREIGN KEY `', fk_name, '`');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END LOOP;
    CLOSE cur;
END$$
DELIMITER ;

CALL drop_old_borrowing_image_fks();
DROP PROCEDURE drop_old_borrowing_image_fks;

-- 3) Drop any leftover indexes on the old columns (Hibernate may have created them).
DROP PROCEDURE IF EXISTS drop_old_borrowing_image_indexes;

DELIMITER $$
CREATE PROCEDURE drop_old_borrowing_image_indexes()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE idx_name VARCHAR(128);
    DECLARE cur CURSOR FOR
        SELECT DISTINCT INDEX_NAME
        FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME   = 'vehicle_borrowing'
          AND COLUMN_NAME IN ('borrowing_image_id', 'return_image_id')
          AND INDEX_NAME <> 'PRIMARY';
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cur;
    idx_loop: LOOP
        FETCH cur INTO idx_name;
        IF done THEN LEAVE idx_loop; END IF;
        SET @sql = CONCAT('ALTER TABLE vehicle_borrowing DROP INDEX `', idx_name, '`');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END LOOP;
    CLOSE cur;
END$$
DELIMITER ;

CALL drop_old_borrowing_image_indexes();
DROP PROCEDURE drop_old_borrowing_image_indexes;

-- 4) Drop the now-orphan columns.
DROP PROCEDURE IF EXISTS drop_old_borrowing_image_columns;

DELIMITER $$
CREATE PROCEDURE drop_old_borrowing_image_columns()
BEGIN
    IF EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME   = 'vehicle_borrowing'
          AND COLUMN_NAME  = 'borrowing_image_id'
    ) THEN
        ALTER TABLE vehicle_borrowing DROP COLUMN borrowing_image_id;
    END IF;

    IF EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME   = 'vehicle_borrowing'
          AND COLUMN_NAME  = 'return_image_id'
    ) THEN
        ALTER TABLE vehicle_borrowing DROP COLUMN return_image_id;
    END IF;
END$$
DELIMITER ;

CALL drop_old_borrowing_image_columns();
DROP PROCEDURE drop_old_borrowing_image_columns;

-- =============================================================================
-- VERIFY (run these to confirm)
-- =============================================================================
-- SELECT COUNT(*) AS borrowings_with_image  FROM media_file WHERE borrowing_images_id IS NOT NULL;
-- SELECT COUNT(*) AS returns_with_image     FROM media_file WHERE return_images_id    IS NOT NULL;
-- SHOW COLUMNS FROM vehicle_borrowing LIKE '%image%';   -- should be empty
