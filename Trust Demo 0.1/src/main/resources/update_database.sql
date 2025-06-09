USE trust_system;

-- Update the branches table to include additional fields
ALTER TABLE branches
    ADD COLUMN description TEXT NOT NULL DEFAULT '',
    ADD COLUMN current_phase ENUM('GENERATION', 'INVESTIGATION', 'DEVELOPMENT', 'PRODUCTION', 'DISTRIBUTION', 'MAINTENANCE', 'RECYCLING', 'COMPLETED') NOT NULL DEFAULT 'GENERATION',
    ADD COLUMN team_openings INT DEFAULT 0;

-- Create the phases table to store phase-specific data
CREATE TABLE IF NOT EXISTS phases (
    id INT AUTO_INCREMENT PRIMARY KEY,
    branch_id INT NOT NULL,
    phase_type ENUM('GENERATION', 'INVESTIGATION', 'DEVELOPMENT', 'PRODUCTION', 'DISTRIBUTION', 'MAINTENANCE', 'RECYCLING') NOT NULL,
    phase_data JSON,
    UNIQUE (branch_id, phase_type),
    FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE CASCADE
);

-- Update the branch_team_members table to include a primary key
ALTER TABLE branch_team_members
    ADD COLUMN id INT AUTO_INCREMENT PRIMARY KEY;

-- Update the branch_candidates table to include a primary key
ALTER TABLE branch_candidates
    ADD COLUMN id INT AUTO_INCREMENT PRIMARY KEY;

-- Add missing columns to the ideas table
ALTER TABLE ideas
    ADD COLUMN name VARCHAR(100) NOT NULL DEFAULT '',
    ADD COLUMN description TEXT DEFAULT NULL,
    ADD COLUMN vote_count INT DEFAULT 0;

-- Add missing columns to the need_supporters table
ALTER TABLE need_supporters
    ADD COLUMN points INT NOT NULL DEFAULT 0;

-- Create the level_proposals table
CREATE TABLE IF NOT EXISTS level_proposals (
    id INT AUTO_INCREMENT PRIMARY KEY,
    proposer_username VARCHAR(50),
    xp_increase_percentage DOUBLE NOT NULL,
    xp_threshold DOUBLE NOT NULL,
    votes INT DEFAULT 0,
    FOREIGN KEY (proposer_username) REFERENCES users(username)
);

-- Create the berry_earning_proposals table
CREATE TABLE IF NOT EXISTS berry_earning_proposals (
    id INT AUTO_INCREMENT PRIMARY KEY,
    proposer_username VARCHAR(50),
    initial_level_one_berry_earning INT NOT NULL,
    votes INT DEFAULT 0,
    FOREIGN KEY (proposer_username) REFERENCES users(username)
);

-- Create the berry_validity_proposals table
CREATE TABLE IF NOT EXISTS berry_validity_proposals (
    id INT AUTO_INCREMENT PRIMARY KEY,
    proposer_username VARCHAR(50),
    months INT NOT NULL,
    votes INT DEFAULT 0,
    FOREIGN KEY (proposer_username) REFERENCES users(username)
);

-- Create the berry_conversion_proposals table
CREATE TABLE IF NOT EXISTS berry_conversion_proposals (
    id INT AUTO_INCREMENT PRIMARY KEY,
    proposer_username VARCHAR(50),
    conversion_percentage DOUBLE NOT NULL,
    conversion_period INT NOT NULL,
    votes INT DEFAULT 0,
    FOREIGN KEY (proposer_username) REFERENCES users(username)
);

-- Create the need_threshold_proposals table
CREATE TABLE IF NOT EXISTS need_threshold_proposals (
    id INT AUTO_INCREMENT PRIMARY KEY,
    proposer_username VARCHAR(50) NOT NULL,
    global_threshold_percent DOUBLE NOT NULL,
    personal_threshold_percent DOUBLE NOT NULL,
    time_limit_months INT NOT NULL,
    branch_id INT DEFAULT -1, -- Default to -1 to indicate application to all branches
    votes INT DEFAULT 0,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (proposer_username) REFERENCES users(username)
);

-- Create the idea_needs table
CREATE TABLE IF NOT EXISTS idea_needs (
    idea_id INT,
    need_id INT,
    PRIMARY KEY (idea_id, need_id),
    FOREIGN KEY (idea_id) REFERENCES ideas(id),
    FOREIGN KEY (need_id) REFERENCES needs(id)
);

-- Create the fields_of_expertise table if it doesn't exist yet
CREATE TABLE IF NOT EXISTS fields_of_expertise (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    parent_id INT NULL, -- Added parent_id column
    FOREIGN KEY (parent_id) REFERENCES fields_of_expertise(id) ON DELETE SET NULL -- Added foreign key constraint
);

-- Create the user_expertise table if it doesn't exist yet
CREATE TABLE IF NOT EXISTS user_expertise (
    username VARCHAR(50),
    expertise_id INT,
    certification_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Optional: track when certified
    PRIMARY KEY (username, expertise_id),
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,
    FOREIGN KEY (expertise_id) REFERENCES fields_of_expertise(id) ON DELETE CASCADE
);

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

-- Create voting tables for berry conversion and need threshold proposals
CREATE TABLE IF NOT EXISTS berry_conversion_proposal_votes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    proposal_id INT NOT NULL,
    username VARCHAR(50) NOT NULL,
    UNIQUE KEY uq_berry_conversion_proposal_vote (proposal_id, username),
    FOREIGN KEY (proposal_id) REFERENCES berry_conversion_proposals(id) ON DELETE CASCADE,
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS need_threshold_proposal_votes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    proposal_id INT NOT NULL,
    username VARCHAR(50) NOT NULL,
    UNIQUE KEY uq_need_threshold_proposal_vote (proposal_id, username),
    FOREIGN KEY (proposal_id) REFERENCES need_threshold_proposals(id) ON DELETE CASCADE,
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
);

-- Also create voting tables for existing proposals if they don't exist
CREATE TABLE IF NOT EXISTS level_proposal_votes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    proposal_id INT NOT NULL,
    username VARCHAR(50) NOT NULL,
    UNIQUE KEY uq_level_proposal_vote (proposal_id, username),
    FOREIGN KEY (proposal_id) REFERENCES level_proposals(id) ON DELETE CASCADE,
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS berry_earning_proposal_votes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    proposal_id INT NOT NULL,
    username VARCHAR(50) NOT NULL,
    UNIQUE KEY uq_berry_earning_proposal_vote (proposal_id, username),
    FOREIGN KEY (proposal_id) REFERENCES berry_earning_proposals(id) ON DELETE CASCADE,
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS berry_validity_proposal_votes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    proposal_id INT NOT NULL,
    username VARCHAR(50) NOT NULL,
    UNIQUE KEY uq_berry_validity_proposal_vote (proposal_id, username),
    FOREIGN KEY (proposal_id) REFERENCES berry_validity_proposals(id) ON DELETE CASCADE,
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
);
