CREATE TABLE urls (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_url TEXT NOT NULL,
    short_code VARCHAR(20) UNIQUE,
    created_at DATETIME NOT NULL
);

CREATE INDEX idx_short_code
ON urls(short_code);