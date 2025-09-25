-- High-volume test data for load testing the Telco system
-- This adds 100 users with varying usage patterns to test system robustness

-- Insert 100 test users for load testing
INSERT INTO users (user_id, phone_number, data_plan_limit, current_usage) VALUES
-- Batch 1: Users below threshold (0-79%)
('load_user_001', '+1000000001', 5368709120, 1073741824),   -- 20%
('load_user_002', '+1000000002', 10737418240, 3221225472),  -- 30%
('load_user_003', '+1000000003', 8589934592, 4294967296),   -- 50%
('load_user_004', '+1000000004', 4294967296, 3435973836),   -- 80% (threshold)
('load_user_005', '+1000000005', 6442450944, 5153960755),   -- 80% (threshold)

-- Batch 2: Users above threshold (80-100%)
('load_user_006', '+1000000006', 5368709120, 4831838208),   -- 90%
('load_user_007', '+1000000007', 10737418240, 9663676416),  -- 90%
('load_user_008', '+1000000008', 8589934592, 7730941132),   -- 90%
('load_user_009', '+1000000009', 4294967296, 3865470566),   -- 90%
('load_user_010', '+1000000010', 6442450944, 5798205849),   -- 90%

-- Batch 3: Users over limit (100%+)
('load_user_011', '+1000000011', 5368709120, 5637144576),   -- 105%
('load_user_012', '+1000000012', 10737418240, 11274289152), -- 105%
('load_user_013', '+1000000013', 8589934592, 9019431321),   -- 105%
('load_user_014', '+1000000014', 4294967296, 4509715660),   -- 105%
('load_user_015', '+1000000015', 6442450944, 6764573491),   -- 105%

-- Batch 4: Mixed usage patterns (16-25)
('load_user_016', '+1000000016', 10737418240, 1610612736),  -- 15%
('load_user_017', '+1000000017', 8589934592, 2576980377),   -- 30%
('load_user_018', '+1000000018', 5368709120, 4294967296),   -- 80%
('load_user_019', '+1000000019', 4294967296, 3865470566),   -- 90%
('load_user_020', '+1000000020', 6442450944, 6764573491),   -- 105%

-- Batch 5: Mixed usage patterns (26-35)
('load_user_026', '+1000000026', 10737418240, 2147483648),  -- 20%
('load_user_027', '+1000000027', 8589934592, 3435973836),   -- 40%
('load_user_028', '+1000000028', 5368709120, 4294967296),   -- 80%
('load_user_029', '+1000000029', 4294967296, 3865470566),   -- 90%
('load_user_030', '+1000000030', 6442450944, 6764573491),   -- 105%

-- Batch 6: Mixed usage patterns (36-45)
('load_user_036', '+1000000036', 10737418240, 3221225472),  -- 30%
('load_user_037', '+1000000037', 8589934592, 4294967296),   -- 50%
('load_user_038', '+1000000038', 5368709120, 4294967296),   -- 80%
('load_user_039', '+1000000039', 4294967296, 3865470566),   -- 90%
('load_user_040', '+1000000040', 6442450944, 6764573491),   -- 105%

-- Batch 7: Mixed usage patterns (46-55)
('load_user_046', '+1000000046', 10737418240, 4294967296),  -- 40%
('load_user_047', '+1000000047', 8589934592, 5153960755),   -- 60%
('load_user_048', '+1000000048', 5368709120, 4294967296),   -- 80%
('load_user_049', '+1000000049', 4294967296, 3865470566),   -- 90%
('load_user_050', '+1000000050', 6442450944, 6764573491),   -- 105%

-- Batch 8: Mixed usage patterns (56-65)
('load_user_056', '+1000000056', 10737418240, 5368709120),  -- 50%
('load_user_057', '+1000000057', 8589934592, 6012954214),   -- 70%
('load_user_058', '+1000000058', 5368709120, 4294967296),   -- 80%
('load_user_059', '+1000000059', 4294967296, 3865470566),   -- 90%
('load_user_060', '+1000000060', 6442450944, 6764573491),   -- 105%

-- Batch 9: Mixed usage patterns (66-75)
('load_user_066', '+1000000066', 10737418240, 6442450944),  -- 60%
('load_user_067', '+1000000067', 8589934592, 6871947673),   -- 80%
('load_user_068', '+1000000068', 5368709120, 4294967296),   -- 80%
('load_user_069', '+1000000069', 4294967296, 3865470566),   -- 90%
('load_user_070', '+1000000070', 6442450944, 6764573491),   -- 105%

-- Batch 10: Mixed usage patterns (76-85)
('load_user_076', '+1000000076', 10737418240, 7516192768),  -- 70%
('load_user_077', '+1000000077', 8589934592, 6871947673),   -- 80%
('load_user_078', '+1000000078', 5368709120, 4294967296),   -- 80%
('load_user_079', '+1000000079', 4294967296, 3865470566),   -- 90%
('load_user_080', '+1000000080', 6442450944, 6764573491),   -- 105%

-- Batch 11: Mixed usage patterns (86-95)
('load_user_086', '+1000000086', 10737418240, 8589934592),  -- 80%
('load_user_087', '+1000000087', 8589934592, 6871947673),   -- 80%
('load_user_088', '+1000000088', 5368709120, 4294967296),   -- 80%
('load_user_089', '+1000000089', 4294967296, 3865470566),   -- 90%
('load_user_090', '+1000000090', 6442450944, 6764573491),   -- 105%

-- Batch 12: Mixed usage patterns (96-100)
('load_user_096', '+1000000096', 10737418240, 9663676416),  -- 90%
('load_user_097', '+1000000097', 8589934592, 7730941132),   -- 90%
('load_user_098', '+1000000098', 5368709120, 4831838208),   -- 90%
('load_user_099', '+1000000099', 4294967296, 3865470566),   -- 90%
('load_user_100', '+1000000100', 6442450944, 6764573491);   -- 105%

-- Verify the load test data
SELECT 
    'Load Test Summary' as test_type,
    COUNT(*) as total_users,
    COUNT(CASE WHEN (current_usage::decimal / data_plan_limit::decimal) * 100 >= 80 THEN 1 END) as users_above_threshold,
    COUNT(CASE WHEN (current_usage::decimal / data_plan_limit::decimal) * 100 >= 100 THEN 1 END) as users_over_limit
FROM users 
WHERE user_id LIKE 'load_user_%';

-- Show usage distribution
SELECT 
    CASE 
        WHEN (current_usage::decimal / data_plan_limit::decimal) * 100 < 80 THEN 'Below Threshold'
        WHEN (current_usage::decimal / data_plan_limit::decimal) * 100 < 100 THEN 'Above Threshold'
        ELSE 'Over Limit'
    END as usage_category,
    COUNT(*) as user_count
FROM users 
WHERE user_id LIKE 'load_user_%'
GROUP BY 
    CASE 
        WHEN (current_usage::decimal / data_plan_limit::decimal) * 100 < 80 THEN 'Below Threshold'
        WHEN (current_usage::decimal / data_plan_limit::decimal) * 100 < 100 THEN 'Above Threshold'
        ELSE 'Over Limit'
    END
ORDER BY user_count DESC;

