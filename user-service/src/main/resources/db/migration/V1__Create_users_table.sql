-- Flyway Migration: Create users table
-- Version: 1
-- Description: Initial users table creation

CREATE TABLE IF NOT EXISTS users (
    user_id VARCHAR(50) PRIMARY KEY,
    phone_number VARCHAR(20) NOT NULL UNIQUE,
    data_plan_limit BIGINT NOT NULL CHECK (data_plan_limit > 0),
    current_usage BIGINT NOT NULL DEFAULT 0 CHECK (current_usage >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index for performance
CREATE INDEX IF NOT EXISTS idx_users_phone_number ON users(phone_number);
CREATE INDEX IF NOT EXISTS idx_users_usage_percentage ON users((current_usage::decimal / data_plan_limit::decimal));

-- Add comments for documentation
COMMENT ON TABLE users IS 'User data and usage information for telco system';
COMMENT ON COLUMN users.user_id IS 'Unique identifier for the user';
COMMENT ON COLUMN users.phone_number IS 'User phone number (unique)';
COMMENT ON COLUMN users.data_plan_limit IS 'Data plan limit in bytes';
COMMENT ON COLUMN users.current_usage IS 'Current data usage in bytes';
COMMENT ON COLUMN users.created_at IS 'Record creation timestamp';
COMMENT ON COLUMN users.updated_at IS 'Record last update timestamp';
