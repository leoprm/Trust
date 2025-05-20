CREATE TABLE branches (
    id INT AUTO_INCREMENT PRIMARY KEY,
    idea_id INT NOT NULL,
    description TEXT NOT NULL,
    current_phase ENUM('GENERATION', 'INVESTIGATION', 'DEVELOPMENT', 'PRODUCTION', 'DISTRIBUTION', 'MAINTENANCE', 'RECYCLING', 'COMPLETED') NOT NULL,
    FOREIGN KEY (idea_id) REFERENCES ideas(id) ON DELETE CASCADE
);

CREATE TABLE phases (
    id INT AUTO_INCREMENT PRIMARY KEY,
    branch_id INT NOT NULL,
    phase_type ENUM('GENERATION', 'INVESTIGATION', 'DEVELOPMENT', 'PRODUCTION', 'DISTRIBUTION', 'MAINTENANCE', 'RECYCLING') NOT NULL,
    phase_data JSON,
    UNIQUE (branch_id, phase_type),
    FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE CASCADE
);

CREATE TABLE branch_team_members (
    id INT AUTO_INCREMENT PRIMARY KEY,
    branch_id INT NOT NULL,
    user_username VARCHAR(255) NOT NULL,
    FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE CASCADE,
    FOREIGN KEY (user_username) REFERENCES users(username) ON DELETE CASCADE
);

CREATE TABLE branch_candidates (
    id INT AUTO_INCREMENT PRIMARY KEY,
    branch_id INT NOT NULL,
    user_username VARCHAR(255) NOT NULL,
    FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE CASCADE,
    FOREIGN KEY (user_username) REFERENCES users(username) ON DELETE CASCADE
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

CREATE TABLE IF NOT EXISTS need_threshold_proposal_votes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    proposal_id INT NOT NULL,
    username VARCHAR(50) NOT NULL,
    UNIQUE KEY uq_need_threshold_proposal_vote (proposal_id, username),
    FOREIGN KEY (proposal_id) REFERENCES need_threshold_proposals(id) ON DELETE CASCADE,
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
);
