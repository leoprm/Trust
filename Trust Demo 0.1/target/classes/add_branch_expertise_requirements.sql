-- Create branch expertise requirements table
CREATE TABLE IF NOT EXISTS branch_expertise_requirements (
    branch_id INT,
    phase_name VARCHAR(50),
    expertise_id INT,
    openings_count INT NOT NULL DEFAULT 0,
    PRIMARY KEY (branch_id, phase_name, expertise_id),
    FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE CASCADE,
    FOREIGN KEY (expertise_id) REFERENCES fields_of_expertise(id) ON DELETE CASCADE
);
