# 🚀 Telco System Robustness Testing Guide

This guide demonstrates how to test the robustness and scalability of your telco real-time alert system through various scenarios.

## 📋 Testing Scenarios Overview

### 1. **Horizontal Scaling Tests**
- Scale Alert Trigger Service (3 instances)
- Scale Notification Service (2 instances) 
- Scale User Service (2 instances)
- High-load scaling (5 Alert + 3 Notification services)

### 2. **Failure & Recovery Tests**
- Service failure simulation
- Database failure and recovery
- Kafka failure and recovery
- Network partition scenarios

### 3. **Load Testing**
- 100+ users with varying usage patterns
- High message throughput
- Resource utilization monitoring

### 4. **Monitoring & Observability**
- Real-time system monitoring
- Performance metrics
- Alert frequency tracking
- Service health checks

## 🛠️ Quick Start Testing

### Step 1: Run Basic Robustness Test
```bash
# Make scripts executable
chmod +x robustness_test.sh
chmod +x monitor_system.sh

# Run comprehensive robustness test
./robustness_test.sh
```

### Step 2: Test Horizontal Scaling
```bash
# Scale to 3 Alert Services + 2 Notification Services
docker-compose -f docker-compose.yml -f docker-compose.scale.yml up -d

# Verify scaling
docker-compose ps
```

### Step 3: Load Testing with High Volume
```bash
# Add 100 test users for load testing
docker exec -it telco-postgres-1 psql -U telco -d telco -f /path/to/load_test_data.sql

# Scale to high-load configuration
docker-compose -f docker-compose.yml -f docker-compose.load-test.yml up -d
```

### Step 4: Real-time Monitoring
```bash
# Start continuous monitoring
./monitor_system.sh --continuous

# Or run one-time health check
./monitor_system.sh
```

## 📊 Expected Results

### ✅ **Scaling Tests**
- **3 Alert Services**: Should generate 3x more frequent checks (every ~5 seconds combined)
- **2 Notification Services**: Kafka consumer group distributes load evenly
- **High Load**: 5 Alert + 3 Notification services handle 100+ users smoothly

### ✅ **Failure Tests**
- **Service Recovery**: Services restart and resume normal operation
- **Database Failure**: User service fails gracefully, recovers on restart
- **Kafka Failure**: Alert service continues running, notifications resume on restart

### ✅ **Load Tests**
- **100 Users**: System handles high volume without performance degradation
- **Alert Throughput**: ~60+ users above threshold generate alerts every 15 seconds
- **Resource Usage**: CPU/Memory stay within reasonable limits

## 🔍 Monitoring Key Metrics

### Service Health
- Container status and resource usage
- API endpoint responsiveness
- Database connectivity

### Alert System
- Alert generation frequency
- Message processing throughput
- Kafka topic health

### Performance
- Memory and CPU utilization
- Network I/O
- Disk usage

## 🚨 Failure Scenarios to Test

### 1. **Single Service Failure**
```bash
# Stop alert service
docker-compose stop alert-trigger-service

# Wait 30 seconds, then restart
docker-compose start alert-trigger-service
```

### 2. **Database Failure**
```bash
# Stop PostgreSQL
docker-compose stop postgres

# Test user service (should fail gracefully)
curl http://localhost:8081/users

# Restart database
docker-compose start postgres
```

### 3. **Kafka Failure**
```bash
# Stop Kafka
docker-compose stop kafka

# Wait, then restart
docker-compose start kafka
```

### 4. **Network Partition**
```bash
# Simulate network issues by stopping multiple services
docker-compose stop alert-trigger-service notification-service

# Restart in sequence
docker-compose start alert-trigger-service
docker-compose start notification-service
```

## 📈 Performance Benchmarks

### Baseline (Single Instance)
- **Alert Check Frequency**: Every 15 seconds
- **Users Processed**: 8 users
- **Alerts Generated**: 4 alerts per cycle
- **Memory Usage**: ~200MB per service

### Scaled (3 Alert + 2 Notification)
- **Alert Check Frequency**: Every ~5 seconds (combined)
- **Users Processed**: 8 users (3x faster)
- **Alerts Generated**: 4 alerts per cycle (distributed processing)
- **Memory Usage**: ~600MB total

### High Load (5 Alert + 3 Notification + 100 Users)
- **Alert Check Frequency**: Every ~3 seconds (combined)
- **Users Processed**: 100 users
- **Alerts Generated**: ~60 alerts per cycle
- **Memory Usage**: ~1.5GB total

## 🔧 Customization Options

### Adjust Alert Threshold
```bash
# Set threshold to 70%
docker-compose -f docker-compose.yml -f docker-compose.scale.yml up -d \
  -e ALERT_THRESHOLD_PERCENT=70
```

### Modify Check Frequency
Edit `UsageCheckScheduler.java`:
```java
@Scheduled(fixedDelay = 10000, initialDelay = 5000) // Every 10 seconds
```

### Scale Individual Services
```bash
# Scale only alert service to 5 instances
docker-compose up -d --scale alert-trigger-service=5
```

## 📝 Test Results Interpretation

### ✅ **Healthy System Indicators**
- All services show "Up" status
- API endpoints return 200 OK
- Alerts generate consistently
- No error logs
- Resource usage stable

### ⚠️ **Warning Indicators**
- Intermittent service restarts
- Some API timeouts
- Delayed alert generation
- High memory usage

### ❌ **Problem Indicators**
- Services failing to start
- Database connection errors
- Kafka consumer lag
- High error rates in logs

## 🎯 Success Criteria

Your system is robust if it can:
1. **Scale horizontally** without data loss
2. **Recover gracefully** from failures
3. **Handle load spikes** without degradation
4. **Maintain consistency** during scaling
5. **Provide observability** for troubleshooting

## 🚀 Next Steps

1. **Run the robustness test suite**
2. **Monitor system behavior** under load
3. **Document performance baselines**
4. **Set up alerting** for production monitoring
5. **Implement auto-scaling** based on metrics

---

**Happy Testing!** 🎉 Your telco system is designed to be resilient and scalable. These tests will help you understand its limits and capabilities.

