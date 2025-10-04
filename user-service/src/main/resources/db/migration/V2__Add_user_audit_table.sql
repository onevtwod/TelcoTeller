-- Flyway Migration: Add user audit table
-- Version: 2
-- Description: Add audit table for tracking user data changes

CREATE TABLE IF NOT EXISTS user_audit (
    audit_id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    action VARCHAR(20) NOT NULL CHECK (action IN ('INSERT', 'UPDATE', 'DELETE')),
    old_data JSONB,
    new_data JSONB,
    changed_by VARCHAR(100) DEFAULT 'system',
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_user_audit_user_id ON user_audit(user_id);
CREATE INDEX IF NOT EXISTS idx_user_audit_changed_at ON user_audit(changed_at);
CREATE INDEX IF NOT EXISTS idx_user_audit_action ON user_audit(action);

-- Add comments
COMMENT ON TABLE user_audit IS 'Audit trail for user data changes';
COMMENT ON COLUMN user_audit.audit_id IS 'Unique audit record identifier';
COMMENT ON COLUMN user_audit.user_id IS 'Reference to users table';
COMMENT ON COLUMN user_audit.action IS 'Type of operation performed';
COMMENT ON COLUMN user_audit.old_data IS 'Previous data (JSON format)';
COMMENT ON COLUMN user_audit.new_data IS 'New data (JSON format)';
COMMENT ON COLUMN user_audit.changed_by IS 'User or system that made the change';
COMMENT ON COLUMN user_audit.changed_at IS 'Timestamp of the change';
