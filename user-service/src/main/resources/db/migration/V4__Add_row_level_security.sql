-- Row Level Security (RLS) Implementation
-- This migration adds RLS policies to protect user data

-- Enable RLS on users table
ALTER TABLE users ENABLE ROW LEVEL SECURITY;

-- Create a function to get current user context
CREATE OR REPLACE FUNCTION get_current_user_id() RETURNS TEXT AS $$
BEGIN
    -- In a real application, this would get the user ID from JWT token or session
    -- For now, we'll use a placeholder that can be set by the application
    RETURN current_setting('app.current_user_id', true);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Create RLS policy for users table
-- Users can only see and modify their own data
CREATE POLICY user_own_data_policy ON users
    FOR ALL
    TO PUBLIC
    USING (user_id = get_current_user_id())
    WITH CHECK (user_id = get_current_user_id());

-- Create RLS policy for user_audit table
ALTER TABLE user_audit ENABLE ROW LEVEL SECURITY;

CREATE POLICY user_audit_own_data_policy ON user_audit
    FOR ALL
    TO PUBLIC
    USING (user_id = get_current_user_id())
    WITH CHECK (user_id = get_current_user_id());

-- Create RLS policy for usage_alerts table
ALTER TABLE usage_alerts ENABLE ROW LEVEL SECURITY;

CREATE POLICY usage_alerts_own_data_policy ON usage_alerts
    FOR ALL
    TO PUBLIC
    USING (user_id = get_current_user_id())
    WITH CHECK (user_id = get_current_user_id());

-- Create a role for service accounts (for system operations)
CREATE ROLE telco_service_role;
GRANT USAGE ON SCHEMA public TO telco_service_role;
GRANT SELECT, INSERT, UPDATE, DELETE ON users TO telco_service_role;
GRANT SELECT, INSERT, UPDATE, DELETE ON user_audit TO telco_service_role;
GRANT SELECT, INSERT, UPDATE, DELETE ON usage_alerts TO telco_service_role;

-- Create a role for read-only access
CREATE ROLE telco_readonly_role;
GRANT USAGE ON SCHEMA public TO telco_readonly_role;
GRANT SELECT ON users TO telco_readonly_role;
GRANT SELECT ON user_audit TO telco_readonly_role;
GRANT SELECT ON usage_alerts TO telco_readonly_role;

-- Create a function to set user context (for application use)
CREATE OR REPLACE FUNCTION set_user_context(user_id TEXT) RETURNS VOID AS $$
BEGIN
    PERFORM set_config('app.current_user_id', user_id, true);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Create a function to clear user context
CREATE OR REPLACE FUNCTION clear_user_context() RETURNS VOID AS $$
BEGIN
    PERFORM set_config('app.current_user_id', '', true);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Add comments for documentation
COMMENT ON FUNCTION get_current_user_id() IS 'Gets the current user ID from application context';
COMMENT ON FUNCTION set_user_context(TEXT) IS 'Sets the current user context for RLS';
COMMENT ON FUNCTION clear_user_context() IS 'Clears the current user context';
COMMENT ON POLICY user_own_data_policy ON users IS 'Users can only access their own data';
COMMENT ON POLICY user_audit_own_data_policy ON user_audit IS 'Users can only access their own audit data';
COMMENT ON POLICY usage_alerts_own_data_policy ON usage_alerts IS 'Users can only access their own usage alerts';
