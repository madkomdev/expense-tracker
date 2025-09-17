-- Add role column to users table for RBAC (only if it doesn't exist)
ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'USER';

-- Add check constraint for valid roles (only if it doesn't exist)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE table_name = 'users' 
        AND constraint_name = 'chk_user_role'
        AND constraint_type = 'CHECK'
    ) THEN
        ALTER TABLE users ADD CONSTRAINT chk_user_role 
            CHECK (role IN ('ADMIN', 'USER'));
    END IF;
END $$;

-- Create index for better performance on role queries (only if it doesn't exist)
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- Update existing users to have USER role (already set as default)
-- First user can be promoted to ADMIN manually if needed

-- Admin user will be created in a later migration (V7__Create_Admin_User.sql)
