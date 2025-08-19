-- Remove user_id constraint and make categories global
ALTER TABLE categories DROP CONSTRAINT IF EXISTS categories_user_id_fkey;
ALTER TABLE categories DROP CONSTRAINT IF EXISTS uk_categories_user_name_type;

-- Add new columns for global categories
ALTER TABLE categories 
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT true,
    ADD COLUMN IF NOT EXISTS sort_order INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Remove user_id column (make categories global)
ALTER TABLE categories DROP COLUMN IF EXISTS user_id;

-- Add new unique constraint for global categories
ALTER TABLE categories ADD CONSTRAINT uk_categories_name_type 
    UNIQUE (name, type);

-- Add index for better performance
CREATE INDEX IF NOT EXISTS idx_categories_type_active_sort ON categories(type, is_active, sort_order);

-- Insert some default categories
INSERT INTO categories (name, color, icon, type, is_active, sort_order) VALUES
    ('Food & Dining', '#EF4444', 'utensils', 'EXPENSE', true, 1),
    ('Transportation', '#3B82F6', 'car', 'EXPENSE', true, 2),
    ('Shopping', '#8B5CF6', 'shopping-bag', 'EXPENSE', true, 3),
    ('Entertainment', '#F59E0B', 'film', 'EXPENSE', true, 4),
    ('Bills & Utilities', '#10B981', 'receipt', 'EXPENSE', true, 5),
    ('Healthcare', '#EC4899', 'heart', 'EXPENSE', true, 6),
    ('Education', '#6366F1', 'book', 'EXPENSE', true, 7),
    ('Travel', '#84CC16', 'plane', 'EXPENSE', true, 8),
    ('Miscellaneous', '#6B7280', 'folder', 'EXPENSE', true, 9),
    ('Salary', '#059669', 'dollar-sign', 'INCOME', true, 1),
    ('Freelance', '#DC2626', 'briefcase', 'INCOME', true, 2),
    ('Investment', '#7C3AED', 'trending-up', 'INCOME', true, 3),
    ('Gift', '#DB2777', 'gift', 'INCOME', true, 4),
    ('Other Income', '#6B7280', 'plus', 'INCOME', true, 5)
ON CONFLICT (name, type) DO NOTHING; 