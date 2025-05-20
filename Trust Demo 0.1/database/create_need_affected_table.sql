CREATE TABLE IF NOT EXISTS need_affected (
    need_id INT NOT NULL,
    username VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    PRIMARY KEY (need_id, username),
    FOREIGN KEY (need_id) REFERENCES needs(id),
    FOREIGN KEY (username) REFERENCES users(username)
);
