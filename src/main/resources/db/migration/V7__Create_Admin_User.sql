-- Create default admin user for production use
-- This replaces the startup AdminUserInitializer class

-- Insert admin user with secure credentials
-- Password: ExpenseTracker@Admin2024! (BCrypt hash below)
-- Share these credentials securely with the team
INSERT INTO users (
    id,
    email, 
    username, 
    password_hash, 
    first_name, 
    last_name, 
    phone, 
    address,
    role,
    created_at,
    updated_at
) 
VALUES (
    'a0000000-0000-4000-8000-000000000001', -- Fixed UUID for admin
    'admin@expense-tracker.com',
    'admin',
    '$2a$12$YQiQxpQy6stIFIXa5Fg5z.Zv8K1pJkMxeozQQn7MQXbTxLx4E6C0W', -- BCrypt hash of 'ExpenseTracker@Admin2024!'
    'System',
    'Administrator',
    '+1-000-000-0000',
    'System Generated Admin User',
    'ADMIN',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) 
ON CONFLICT (email) DO NOTHING; -- Avoid duplicate if admin already exists

-- Log admin creation result
DO $$ 
BEGIN 
    IF EXISTS (SELECT 1 FROM users WHERE email = 'admin@expense-tracker.com' AND role = 'ADMIN') THEN
        RAISE NOTICE '✅ Admin user exists and is properly configured!';
        RAISE NOTICE '📧 Email: admin@expense-tracker.com';
        RAISE NOTICE '👤 Username: admin';
        RAISE NOTICE '🔑 Password: ExpenseTracker@Admin2024!';
        RAISE NOTICE '⚠️  Please share these credentials securely with your team';
        RAISE NOTICE '⚠️  Consider changing the password after first login via the API';
    ELSE
        RAISE NOTICE '❌ Admin user creation may have failed - please check manually';
    END IF;
END $$;
