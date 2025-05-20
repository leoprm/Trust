-- Create need_affected table
CREATE TABLE IF NOT EXISTS need_affected (
    need_id INT,
    user_username VARCHAR(50),
    location VARCHAR(255),
    PRIMARY KEY (need_id, user_username),
    FOREIGN KEY (need_id) REFERENCES needs(id) ON DELETE CASCADE,
    FOREIGN KEY (user_username) REFERENCES users(username) ON DELETE CASCADE
); 