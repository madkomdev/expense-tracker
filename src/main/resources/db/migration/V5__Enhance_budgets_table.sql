-- Add useful indexes for budget queries
CREATE INDEX IF NOT EXISTS idx_budgets_user_category ON budgets(user_id, category_id);
CREATE INDEX IF NOT EXISTS idx_budgets_date_range ON budgets(start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_budgets_period ON budgets(period);

-- Prevent overlapping budgets for same user+category+period
ALTER TABLE budgets ADD CONSTRAINT uk_budgets_user_category_period_dates 
    UNIQUE (user_id, category_id, period, start_date);
