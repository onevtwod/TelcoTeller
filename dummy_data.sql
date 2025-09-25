-- Dummy data for testing the Telco Real-Time Alert System
-- Run these queries in your PostgreSQL database

-- Insert test users with different usage scenarios
INSERT INTO users (user_id, phone_number, data_plan_limit, current_usage) VALUES
-- User 1: Low usage (20% of 5GB plan)
('user001', '+1234567890', 5368709120, 1073741824),

-- User 2: Medium usage (60% of 10GB plan)  
('user002', '+1234567891', 10737418240, 6442450944),

-- User 3: High usage (85% of 8GB plan) - Should trigger alert
('user003', '+1234567892', 8589934592, 7301444403),

-- User 4: Critical usage (95% of 3GB plan) - Should trigger alert
('user004', '+1234567893', 3221225472, 3053453312),

-- User 5: Over limit (105% of 2GB plan) - Should trigger alert
('user005', '+1234567894', 2147483648, 2254857830),

-- User 6: Just below threshold (79% of 4GB plan) - Should NOT trigger alert
('user006', '+1234567895', 4294967296, 3393028095),

-- User 7: Unlimited plan user (50% of 50GB plan)
('user007', '+1234567896', 53687091200, 26843545600),

-- User 8: Small plan user (90% of 1GB plan) - Should trigger alert
('user008', '+1234567897', 1073741824, 966367641);

-- Verify the data was inserted
SELECT 
    user_id,
    phone_number,
    ROUND(data_plan_limit / 1024.0 / 1024.0 / 1024.0, 2) AS data_plan_gb,
    ROUND(current_usage / 1024.0 / 1024.0 / 1024.0, 2) AS current_usage_gb,
    ROUND((current_usage::decimal / data_plan_limit::decimal) * 100, 1) AS usage_percent
FROM users 
ORDER BY usage_percent DESC;
