-- Flyway migration: initial schema
-- Items table
CREATE TABLE IF NOT EXISTS items (
    item_name VARCHAR(255) PRIMARY KEY
);

-- Price history table
CREATE TABLE IF NOT EXISTS price_history (
    id BIGSERIAL PRIMARY KEY,
    item_name VARCHAR(255) NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    median DOUBLE PRECISION NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    CONSTRAINT fk_price_item FOREIGN KEY (item_name)
        REFERENCES items (item_name)
        ON DELETE CASCADE
);

-- Buy infos table
CREATE TABLE IF NOT EXISTS buy_infos (
    id UUID PRIMARY KEY,
    item_name VARCHAR(255) NOT NULL,
    amount INTEGER NOT NULL,
    buy_price DOUBLE PRECISION NOT NULL
);
