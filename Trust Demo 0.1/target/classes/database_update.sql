-- Check if display_name column exists, add it if it doesn't
SET @columnExists = 0;
SELECT COUNT(*) INTO @columnExists FROM information_schema.columns 
WHERE table_name = 'users' AND column_name = 'display_name';

SET @alterQuery = IF(@columnExists = 0, 
    'ALTER TABLE users ADD COLUMN display_name VARCHAR(100)',
    'SELECT "display_name column already exists" AS message');
PREPARE stmt FROM @alterQuery;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Drop primary key from username
ALTER TABLE users DROP PRIMARY KEY;

-- Add ID column as primary key
ALTER TABLE users
ADD COLUMN id INT AUTO_INCREMENT PRIMARY KEY FIRST;

-- Make username UNIQUE to maintain referential integrity
ALTER TABLE users
MODIFY COLUMN username VARCHAR(50) UNIQUE NOT NULL;

-- Add table for branch team opening expertise requirements
CREATE TABLE IF NOT EXISTS branch_expertise_requirements (
    branch_id INT NOT NULL,
    phase_name VARCHAR(50) NOT NULL,
    expertise_id INT NOT NULL,
    openings_count INT NOT NULL DEFAULT 1,
    PRIMARY KEY (branch_id, phase_name, expertise_id),
    FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE CASCADE,
    FOREIGN KEY (expertise_id) REFERENCES fields_of_expertise(id) ON DELETE CASCADE
);

-- Verify the changes
DESCRIBE users;

-- Display migration completion message
SELECT 'Users table migration complete. ID column added as primary key.' AS message;
