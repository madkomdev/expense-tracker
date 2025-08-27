-- Remove type column from transactions table
-- Type is now inferred from the category relationship

-- Drop the indexes that use the transaction type column
DROP INDEX IF EXISTS idx_transactions_user_type;
DROP INDEX IF EXISTS idx_transactions_type_date;

-- Drop views that reference the transaction type column
DROP VIEW IF EXISTS transaction_summary_by_user;
DROP VIEW IF EXISTS transaction_summary_by_user_category;

-- Remove the type column from transactions table (type is now inferred from category)
ALTER TABLE transactions DROP COLUMN IF EXISTS type;

-- Create new indexes that use category relationships instead
CREATE INDEX IF NOT EXISTS idx_transactions_user_category_type ON transactions(user_id, category_id);
CREATE INDEX IF NOT EXISTS idx_transactions_category_date ON transactions(category_id, transaction_date);

-- Recreate the views using category type instead of transaction type
CREATE OR REPLACE VIEW transaction_summary_by_user AS
SELECT 
    t.user_id,
    c.type,
    COUNT(*) as transaction_count,
    SUM(t.amount) as total_amount,
    MIN(t.transaction_date) as first_transaction_date,
    MAX(t.transaction_date) as last_transaction_date
FROM transactions t
JOIN categories c ON t.category_id = c.id
GROUP BY t.user_id, c.type;

-- Recreate the category-wise transaction summaries view
CREATE OR REPLACE VIEW transaction_summary_by_user_category AS
SELECT 
    t.user_id,
    t.category_id,
    c.name as category_name,
    c.color as category_color,
    c.icon as category_icon,
    c.type as category_type,
    COUNT(*) as transaction_count,
    SUM(t.amount) as total_amount
FROM transactions t
JOIN categories c ON t.category_id = c.id
GROUP BY t.user_id, t.category_id, c.name, c.color, c.icon, c.type;
