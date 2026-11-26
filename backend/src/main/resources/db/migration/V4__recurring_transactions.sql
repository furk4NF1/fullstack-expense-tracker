CREATE TABLE recurring_transactions (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL REFERENCES categories(id),
    transaction_type VARCHAR(10) NOT NULL CHECK (transaction_type IN ('INCOME', 'EXPENSE')),
    amount NUMERIC(12, 2) NOT NULL CHECK (amount > 0),
    description VARCHAR(120) NOT NULL,
    day_of_month SMALLINT NOT NULL CHECK (day_of_month BETWEEN 1 AND 31)
);

CREATE INDEX idx_recurring_transactions_category ON recurring_transactions (category_id);

-- This ledger remains when an individual generated expense is deleted, so it
-- cannot accidentally be recreated by applying the same month again.
CREATE TABLE recurring_transaction_applications (
    id BIGSERIAL PRIMARY KEY,
    recurring_id BIGINT NOT NULL REFERENCES recurring_transactions(id) ON DELETE CASCADE,
    application_month DATE NOT NULL CHECK (EXTRACT(DAY FROM application_month) = 1),
    expense_id BIGINT REFERENCES expenses(id) ON DELETE SET NULL,
    CONSTRAINT uq_recurring_application_month UNIQUE (recurring_id, application_month)
);
