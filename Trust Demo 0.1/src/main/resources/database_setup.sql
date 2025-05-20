-- Create the database if it doesn't exist
CREATE DATABASE IF NOT EXISTS trust_system;
USE trust_system;

-- Users table with ID as primary key
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    display_name VARCHAR(100),
    level INT DEFAULT 1,
    xp INT DEFAULT 0,
    points INT DEFAULT 100,
    total_berries_earned INT DEFAULT 0
);

-- Berries table
CREATE TABLE IF NOT EXISTS berries (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50),
    amount INT NOT NULL,
    source VARCHAR(50),
    expiration_date TIMESTAMP NOT NULL,
    expired BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (username) REFERENCES users(username)
);

-- Needs table
CREATE TABLE IF NOT EXISTS needs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

-- Need supporters table
CREATE TABLE IF NOT EXISTS need_supporters (
    need_id INT,
    username VARCHAR(50),
    points INT NOT NULL,
    PRIMARY KEY (need_id, username),
    FOREIGN KEY (need_id) REFERENCES needs(id),
    FOREIGN KEY (username) REFERENCES users(username)
);

-- Need affected users table
CREATE TABLE IF NOT EXISTS need_affected_users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    need_id INT,
    username VARCHAR(50),
    location VARCHAR(255),
    FOREIGN KEY (need_id) REFERENCES needs(id),
    FOREIGN KEY (username) REFERENCES users(username)
);

-- Ideas table
CREATE TABLE IF NOT EXISTS ideas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    author VARCHAR(50),
    vote_count INT DEFAULT 0,
    status VARCHAR(50) DEFAULT 'PENDING',
    FOREIGN KEY (author) REFERENCES users(username)
);

-- Idea supporters table
CREATE TABLE IF NOT EXISTS idea_supporters (
    idea_id INT,
    username VARCHAR(50),
    PRIMARY KEY (idea_id, username),
    FOREIGN KEY (idea_id) REFERENCES ideas(id),
    FOREIGN KEY (username) REFERENCES users(username)
);

-- Branches table
CREATE TABLE IF NOT EXISTS branches (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    description TEXT NOT NULL,
    parent_id INT DEFAULT 0,
    idea_id INT,
    current_phase ENUM('GENERATION', 'INVESTIGATION', 'DEVELOPMENT', 'PRODUCTION', 'DISTRIBUTION', 'MAINTENANCE', 'RECYCLING', 'COMPLETED') NOT NULL DEFAULT 'GENERATION',
    team_openings INT DEFAULT 0,
    FOREIGN KEY (idea_id) REFERENCES ideas(id)
);

-- Branch team members table
CREATE TABLE IF NOT EXISTS branch_team (
    branch_id INT,
    username VARCHAR(50),
    PRIMARY KEY (branch_id, username),
    FOREIGN KEY (branch_id) REFERENCES branches(id),
    FOREIGN KEY (username) REFERENCES users(username)
);

-- Branch candidates table
CREATE TABLE IF NOT EXISTS branch_candidates (
    branch_id INT,
    username VARCHAR(50),
    PRIMARY KEY (branch_id, username),
    FOREIGN KEY (branch_id) REFERENCES branches(id),
    FOREIGN KEY (username) REFERENCES users(username)
);

-- Proposals table for all proposal types
CREATE TABLE IF NOT EXISTS proposals (
    id INT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    proposer VARCHAR(50),
    parameters TEXT, -- JSON parameters
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (proposer) REFERENCES users(username)
);

-- Proposal votes table
CREATE TABLE IF NOT EXISTS proposal_votes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    proposal_type VARCHAR(50) NOT NULL,
    proposal_id INT NOT NULL,
    voter VARCHAR(50),
    vote_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (voter) REFERENCES users(username)
);

-- Idea-Need associations table
CREATE TABLE IF NOT EXISTS idea_needs (
    idea_id INT,
    need_id INT,
    PRIMARY KEY (idea_id, need_id),
    FOREIGN KEY (idea_id) REFERENCES ideas(id),
    FOREIGN KEY (need_id) REFERENCES needs(id)
);

-- Create the phases table to store phase-specific data
CREATE TABLE IF NOT EXISTS phases (
    id INT AUTO_INCREMENT PRIMARY KEY,
    branch_id INT NOT NULL,
    phase_type ENUM('GENERATION', 'INVESTIGATION', 'DEVELOPMENT', 'PRODUCTION', 'DISTRIBUTION', 'MAINTENANCE', 'RECYCLING', 'COMPLETED') NOT NULL,
    phase_data TEXT, -- Serialized object data
    UNIQUE (branch_id, phase_type),
    FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE CASCADE
);
-- Create branch_ideas table to maintain the relationship between branches and ideas
CREATE TABLE IF NOT EXISTS branch_ideas (
    branch_id INT,
    idea_id INT,
    PRIMARY KEY (branch_id, idea_id),
    FOREIGN KEY (branch_id) REFERENCES branches(id),
    FOREIGN KEY (idea_id) REFERENCES ideas(id)
);

-- Fields of Expertise table
CREATE TABLE IF NOT EXISTS fields_of_expertise (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    parent_id INT NULL, -- Added parent_id column
    FOREIGN KEY (parent_id) REFERENCES fields_of_expertise(id) ON DELETE SET NULL -- Added foreign key constraint
);

-- User Expertise table (certifications)
CREATE TABLE IF NOT EXISTS user_expertise (
    username VARCHAR(50),
    expertise_id INT,
    certification_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Optional: track when certified
    PRIMARY KEY (username, expertise_id),
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,
    FOREIGN KEY (expertise_id) REFERENCES fields_of_expertise(id) ON DELETE CASCADE
);

-- Branch Expertise Requirements table (for team openings)
CREATE TABLE IF NOT EXISTS branch_expertise_requirements (
    branch_id INT,
    phase_name VARCHAR(50),
    expertise_id INT,
    openings_count INT NOT NULL DEFAULT 0,
    PRIMARY KEY (branch_id, phase_name, expertise_id),
    FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE CASCADE,
    FOREIGN KEY (expertise_id) REFERENCES fields_of_expertise(id) ON DELETE CASCADE
);

-- Notifications table (for Job alerts, etc.)
CREATE TABLE IF NOT EXISTS notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE,
    related_branch_id INT NULL,
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,
    FOREIGN KEY (related_branch_id) REFERENCES branches(id) ON DELETE SET NULL,
    INDEX idx_notification_user_read (username, is_read) -- Index for faster querying of unread messages
);

-- Need Threshold Proposals table 
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

-- Need Threshold Proposal Votes table to track who has voted
CREATE TABLE IF NOT EXISTS need_threshold_proposal_votes (
    proposal_id INT,
    username VARCHAR(50),
    vote_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (proposal_id, username),
    FOREIGN KEY (proposal_id) REFERENCES need_threshold_proposals(id) ON DELETE CASCADE,
    FOREIGN KEY (username) REFERENCES users(username)
);

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

CREATE TABLE IF NOT EXISTS berry_conversion_proposal_votes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    proposal_id INT NOT NULL,
    username VARCHAR(50) NOT NULL,
    UNIQUE KEY uq_berry_conversion_proposal_vote (proposal_id, username),
    FOREIGN KEY (proposal_id) REFERENCES berry_conversion_proposals(id) ON DELETE CASCADE,
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
);

