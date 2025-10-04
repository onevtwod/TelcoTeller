-- Flyway Migration: Add usage alerts table
-- Version: 3
-- Description: Track usage alerts and notifications

CREATE TABLE IF NOT EXISTS usage_alerts (
    alert_id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    alert_type VARCHAR(20) NOT NULL CHECK (alert_type IN ('THRESHOLD', 'OVER_LIMIT', 'CRITICAL')),
    usage_percentage DECIMAL(5,2) NOT NULL,
    data_used BIGINT NOT NULL,
    data_limit BIGINT NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    alert_status VARCHAR(20) DEFAULT 'PENDING' CHECK (alert_status IN ('PENDING', 'SENT', 'FAILED', 'CANCELLED')),
    sent_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_usage_alerts_user_id ON usage_alerts(user_id);
CREATE INDEX IF NOT EXISTS idx_usage_alerts_status ON usage_alerts(alert_status);
CREATE INDEX IF NOT EXISTS idx_usage_alerts_created_at ON usage_alerts(created_at);
CREATE INDEX IF NOT EXISTS idx_usage_alerts_type ON usage_alerts(alert_type);

-- Add comments
COMMENT ON TABLE usage_alerts IS 'Usage alerts and notification tracking';
COMMENT ON COLUMN usage_alerts.alert_id IS 'Unique alert identifier';
COMMENT ON COLUMN usage_alerts.user_id IS 'Reference to users table';
COMMENT ON COLUMN usage_alerts.alert_type IS 'Type of alert (THRESHOLD, OVER_LIMIT, CRITICAL)';
COMMENT ON COLUMN usage_alerts.usage_percentage IS 'Usage percentage when alert was triggered';
COMMENT ON COLUMN usage_alerts.data_used IS 'Data used in bytes when alert was triggered';
COMMENT ON COLUMN usage_alerts.data_limit IS 'Data limit in bytes when alert was triggered';
COMMENT ON COLUMN usage_alerts.phone_number IS 'Phone number for notification';
COMMENT ON COLUMN usage_alerts.alert_status IS 'Current status of the alert';
COMMENT ON COLUMN usage_alerts.sent_at IS 'Timestamp when alert was sent';
COMMENT ON COLUMN usage_alerts.created_at IS 'Timestamp when alert was created';
