-- Add location column to need_affected table
ALTER TABLE need_affected
ADD COLUMN location VARCHAR(255); 