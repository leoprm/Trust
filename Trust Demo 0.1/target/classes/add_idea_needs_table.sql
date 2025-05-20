-- Add Idea-Need associations table
CREATE TABLE IF NOT EXISTS idea_needs (
    idea_id INT,
    need_id INT,
    PRIMARY KEY (idea_id, need_id),
    FOREIGN KEY (idea_id) REFERENCES ideas(id),
    FOREIGN KEY (need_id) REFERENCES needs(id)
); 