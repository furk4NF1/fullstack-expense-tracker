CREATE TABLE monthly_budgets (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL REFERENCES categories(id),
    budget_month DATE NOT NULL CHECK (EXTRACT(DAY FROM budget_month) = 1),
    budget_amount NUMERIC(12, 2) NOT NULL CHECK (budget_amount > 0),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_monthly_budgets_category_month UNIQUE (category_id, budget_month)
);

CREATE INDEX idx_monthly_budgets_budget_month ON monthly_budgets (budget_month);
