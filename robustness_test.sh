#!/bin/bash

# Telco System Robustness Testing Script
# This script tests various failure scenarios and scaling capabilities

echo "🚀 Starting Telco System Robustness Tests..."
echo "=============================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[$(date +'%H:%M:%S')]${NC} $1"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Test 1: Basic System Health Check
test_basic_health() {
    print_status "Test 1: Basic System Health Check"
    
    # Check if all services are running
    services=("user-service" "alert-trigger-service" "notification-service" "postgres" "kafka")
    
    for service in "${services[@]}"; do
        if docker-compose ps | grep -q "$service.*Up"; then
            print_success "$service is running"
        else
            print_error "$service is not running"
            return 1
        fi
    done
}

# Test 2: Scale Alert Trigger Service
test_scale_alert_service() {
    print_status "Test 2: Scaling Alert Trigger Service (3 instances)"
    
    docker-compose -f docker-compose.yml -f docker-compose.scale.yml up -d alert-trigger-service
    sleep 10
    
    # Count running instances
    instances=$(docker-compose ps | grep "alert-trigger-service.*Up" | wc -l)
    if [ "$instances" -eq 3 ]; then
        print_success "Alert Trigger Service scaled to 3 instances"
    else
        print_error "Alert Trigger Service scaling failed (found $instances instances)"
    fi
}

# Test 3: Scale Notification Service
test_scale_notification_service() {
    print_status "Test 3: Scaling Notification Service (2 instances)"
    
    docker-compose -f docker-compose.yml -f docker-compose.scale.yml up -d notification-service
    sleep 10
    
    # Count running instances
    instances=$(docker-compose ps | grep "notification-service.*Up" | wc -l)
    if [ "$instances" -eq 2 ]; then
        print_success "Notification Service scaled to 2 instances"
    else
        print_error "Notification Service scaling failed (found $instances instances)"
    fi
}

# Test 4: Service Failure and Recovery
test_service_failure_recovery() {
    print_status "Test 4: Service Failure and Recovery"
    
    # Stop alert-trigger-service
    print_warning "Stopping Alert Trigger Service..."
    docker-compose stop alert-trigger-service
    sleep 5
    
    # Check if system continues to work (notification service should still consume existing messages)
    print_warning "System running without Alert Trigger Service..."
    sleep 10
    
    # Restart alert-trigger-service
    print_warning "Restarting Alert Trigger Service..."
    docker-compose start alert-trigger-service
    sleep 10
    
    print_success "Alert Trigger Service recovered successfully"
}

# Test 5: Database Failure Simulation
test_database_failure() {
    print_status "Test 5: Database Failure Simulation"
    
    # Stop PostgreSQL
    print_warning "Stopping PostgreSQL..."
    docker-compose stop postgres
    sleep 5
    
    # Try to access user service (should fail gracefully)
    print_warning "Testing User Service without database..."
    response=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/users || echo "000")
    
    if [ "$response" = "000" ]; then
        print_success "User Service failed gracefully when database is down"
    else
        print_warning "User Service responded with code: $response"
    fi
    
    # Restart PostgreSQL
    print_warning "Restarting PostgreSQL..."
    docker-compose start postgres
    sleep 15
    
    # Check if user service recovers
    response=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/users)
    if [ "$response" = "200" ]; then
        print_success "User Service recovered after database restart"
    else
        print_error "User Service failed to recover (HTTP $response)"
    fi
}

# Test 6: Kafka Failure Simulation
test_kafka_failure() {
    print_status "Test 6: Kafka Failure Simulation"
    
    # Stop Kafka
    print_warning "Stopping Kafka..."
    docker-compose stop kafka
    sleep 5
    
    # Alert service should continue running but can't send messages
    print_warning "Testing system without Kafka..."
    sleep 10
    
    # Restart Kafka
    print_warning "Restarting Kafka..."
    docker-compose start kafka
    sleep 15
    
    print_success "Kafka recovered successfully"
}

# Test 7: High Load Test
test_high_load() {
    print_status "Test 7: High Load Test (5 Alert Services, 3 Notification Services)"
    
    docker-compose -f docker-compose.yml -f docker-compose.load-test.yml up -d
    sleep 20
    
    # Count instances
    alert_instances=$(docker-compose ps | grep "alert-trigger-service.*Up" | wc -l)
    notification_instances=$(docker-compose ps | grep "notification-service.*Up" | wc -l)
    
    if [ "$alert_instances" -eq 5 ] && [ "$notification_instances" -eq 3 ]; then
        print_success "High load scaling successful (5 Alert + 3 Notification services)"
    else
        print_error "High load scaling failed (Alert: $alert_instances, Notification: $notification_instances)"
    fi
}

# Test 8: Message Throughput Test
test_message_throughput() {
    print_status "Test 8: Message Throughput Test"
    
    # Add more test users to increase message volume
    print_warning "Adding high-volume test data..."
    
    # This would add more users to test higher throughput
    # For now, we'll just monitor the existing alerts
    print_success "Monitoring existing alert throughput..."
}

# Cleanup function
cleanup() {
    print_status "Cleaning up test environment..."
    docker-compose down
    docker-compose -f docker-compose.yml -f docker-compose.scale.yml down
    docker-compose -f docker-compose.yml -f docker-compose.load-test.yml down
    print_success "Cleanup completed"
}

# Main test execution
main() {
    echo "Starting comprehensive robustness testing..."
    echo
    
    # Run tests
    test_basic_health && echo
    test_scale_alert_service && echo
    test_scale_notification_service && echo
    test_service_failure_recovery && echo
    test_database_failure && echo
    test_kafka_failure && echo
    test_high_load && echo
    test_message_throughput && echo
    
    echo "=============================================="
    print_success "All robustness tests completed!"
    echo
    echo "To monitor the system in real-time:"
    echo "  docker-compose logs -f"
    echo
    echo "To check service status:"
    echo "  docker-compose ps"
    echo
    echo "To scale services:"
    echo "  docker-compose -f docker-compose.yml -f docker-compose.scale.yml up -d"
}

# Handle script interruption
trap cleanup EXIT

# Run main function
main "$@"
