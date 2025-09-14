-- Add role column to users table for RBAC
ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER' 
    CHECK (role IN ('ADMIN', 'USER'));

-- Create index for better performance on role queries
CREATE INDEX idx_users_role ON users(role);

-- Update existing users to have USER role (already set as default)
-- First user can be promoted to ADMIN manually if needed

-- Insert a default admin user for testing (password is 'admin123')
INSERT INTO users (email, username, password_hash, first_name, last_name, role) 
VALUES (
    'admin@expense-tracker.com',
    'admin',
    '$2a$10$rZ8R.3m6.lOr.7VL7fQ4L.7KJ3qJ8YqO5j1mX9Y8qZNHJ8T7xQxO2', -- BCrypt hash of 'admin123'
    'Admin',
    'User',
    'ADMIN'
) ON CONFLICT (email) DO NOTHING;
