CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    legacy_data_pending BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uq_users_email_ci ON users (LOWER(email));

-- Keep existing categories, transactions, and budgets intact. The first account
-- created after this migration adopts this locked placeholder's data.
INSERT INTO users (email, password_hash, enabled, legacy_data_pending)
VALUES ('legacy-data@internal.invalid', '!', FALSE, TRUE);

ALTER TABLE categories ADD COLUMN owner_id BIGINT;
UPDATE categories
SET owner_id = (SELECT id FROM users WHERE legacy_data_pending = TRUE);
ALTER TABLE categories ALTER COLUMN owner_id SET NOT NULL;
ALTER TABLE categories
    ADD CONSTRAINT fk_categories_owner FOREIGN KEY (owner_id) REFERENCES users(id);

ALTER TABLE categories DROP CONSTRAINT IF EXISTS categories_name_key;
CREATE UNIQUE INDEX uq_categories_owner_name_ci ON categories (owner_id, LOWER(name));
CREATE INDEX idx_categories_owner_id ON categories (owner_id);
