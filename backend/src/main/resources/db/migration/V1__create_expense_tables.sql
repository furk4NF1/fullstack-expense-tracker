CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE,
    color VARCHAR(7) NOT NULL
);

INSERT INTO categories (name, color) VALUES
    ('Gıda', '#2b7a66'),
    ('Ulaşım', '#5273b8'),
    ('Faturalar', '#d48b3d'),
    ('Sağlık', '#c26068'),
    ('Eğlence', '#8a6bb4'),
    ('Diğer', '#748078');

CREATE TABLE expenses (
    id BIGSERIAL PRIMARY KEY,
    transaction_type VARCHAR(10) NOT NULL CHECK (transaction_type IN ('INCOME', 'EXPENSE')),
    amount NUMERIC(12, 2) NOT NULL CHECK (amount > 0),
    description VARCHAR(120) NOT NULL,
    expense_date DATE NOT NULL,
    category_id BIGINT NOT NULL REFERENCES categories(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_expenses_expense_date ON expenses (expense_date DESC);
CREATE INDEX idx_expenses_category_id ON expenses (category_id);
