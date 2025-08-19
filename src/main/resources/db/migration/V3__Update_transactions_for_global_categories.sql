-- Update foreign key constraint for global categories
-- (categories no longer have user_id after V2 migration)

-- Add additional useful indexes for transaction queries
CREATE INDEX IF NOT EXISTS idx_transactions_user_type ON transactions(user_id, type);
CREATE INDEX IF NOT EXISTS idx_transactions_user_date ON transactions(user_id, transaction_date);
CREATE INDEX IF NOT EXISTS idx_transactions_user_category ON transactions(user_id, category_id);
CREATE INDEX IF NOT EXISTS idx_transactions_amount ON transactions(amount);
CREATE INDEX IF NOT EXISTS idx_transactions_type_date ON transactions(type, transaction_date);
CREATE INDEX IF NOT EXISTS idx_transactions_user_created ON transactions(user_id, created_at);

-- Add constraint to ensure transaction type matches category type
-- (This will be enforced at application level for now, but we could add a trigger later)

-- Create a view for transaction summaries (optional, for better performance)
CREATE OR REPLACE VIEW transaction_summary_by_user AS
SELECT 
    user_id,
    type,
    COUNT(*) as transaction_count,
    SUM(amount) as total_amount,
    MIN(transaction_date) as first_transaction_date,
    MAX(transaction_date) as last_transaction_date
FROM transactions 
GROUP BY user_id, type;

-- Create a view for category-wise transaction summaries
CREATE OR REPLACE VIEW transaction_summary_by_user_category AS
SELECT 
    t.user_id,
    t.category_id,
    c.name as category_name,
    c.color as category_color,
    c.icon as category_icon,
    t.type,
    COUNT(*) as transaction_count,
    SUM(t.amount) as total_amount
FROM transactions t
JOIN categories c ON t.category_id = c.id
GROUP BY t.user_id, t.category_id, c.name, c.color, c.icon, t.type; 