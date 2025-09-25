#!/bin/bash

# Real-time system monitoring script for Telco Alert System
# This script provides comprehensive monitoring of all services

echo "🔍 Telco System Real-Time Monitor"
echo "=================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Function to print colored output
print_header() {
    echo -e "${CYAN}════════════════════════════════════════${NC}"
    echo -e "${CYAN} $1${NC}"
    echo -e "${CYAN}════════════════════════════════════════${NC}"
}

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

# Function to check service health
check_service_health() {
    local service_name=$1
    local port=$2
    
    if curl -s -f "http://localhost:$port" > /dev/null 2>&1; then
        print_success "$service_name (port $port) is healthy"
        return 0
    else
        print_error "$service_name (port $port) is unhealthy"
        return 1
    fi
}

# Function to get service metrics
get_service_metrics() {
    print_header "SERVICE STATUS & METRICS"
    
    echo -e "${YELLOW}Docker Container Status:${NC}"
    docker-compose ps
    
    echo
    echo -e "${YELLOW}Service Health Checks:${NC}"
    
    # Check each service
    check_service_health "User Service" 8081
    check_service_health "Alert Trigger Service" 8082
    check_service_health "Notification Service" 8083
    
    echo
    echo -e "${YELLOW}Container Resource Usage:${NC}"
    docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}\t{{.BlockIO}}"
}

# Function to monitor Kafka topics
monitor_kafka() {
    print_header "KAFKA MONITORING"
    
    echo -e "${YELLOW}Kafka Topics:${NC}"
    docker exec telco-kafka-1 kafka-topics --bootstrap-server localhost:9092 --list 2>/dev/null || echo "Kafka not accessible"
    
    echo
    echo -e "${YELLOW}Kafka Consumer Groups:${NC}"
    docker exec telco-kafka-1 kafka-consumer-groups --bootstrap-server localhost:9092 --list 2>/dev/null || echo "Kafka not accessible"
    
    echo
    echo -e "${YELLOW}Kafka Topic Details:${NC}"
    docker exec telco-kafka-1 kafka-topics --bootstrap-server localhost:9092 --describe --topic sms-alerts 2>/dev/null || echo "sms-alerts topic not accessible"
}

# Function to monitor database
monitor_database() {
    print_header "DATABASE MONITORING"
    
    echo -e "${YELLOW}Database Connection:${NC}"
    if docker exec telco-postgres-1 pg_isready -U telco > /dev/null 2>&1; then
        print_success "PostgreSQL is ready"
    else
        print_error "PostgreSQL is not ready"
    fi
    
    echo
    echo -e "${YELLOW}User Count:${NC}"
    docker exec telco-postgres-1 psql -U telco -d telco -c "SELECT COUNT(*) as total_users FROM users;" 2>/dev/null || echo "Database not accessible"
    
    echo
    echo -e "${YELLOW}Users Above Threshold (80%):${NC}"
    docker exec telco-postgres-1 psql -U telco -d telco -c "
        SELECT COUNT(*) as users_above_threshold 
        FROM users 
        WHERE (current_usage::decimal / data_plan_limit::decimal) * 100 >= 80;
    " 2>/dev/null || echo "Database not accessible"
}

# Function to monitor logs
monitor_logs() {
    print_header "RECENT LOGS (Last 10 lines each service)"
    
    echo -e "${YELLOW}User Service Logs:${NC}"
    docker-compose logs --tail=10 user-service
    
    echo
    echo -e "${YELLOW}Alert Trigger Service Logs:${NC}"
    docker-compose logs --tail=10 alert-trigger-service
    
    echo
    echo -e "${YELLOW}Notification Service Logs:${NC}"
    docker-compose logs --tail=10 notification-service
}

# Function to test API endpoints
test_api_endpoints() {
    print_header "API ENDPOINT TESTING"
    
    echo -e "${YELLOW}Testing User Service Endpoints:${NC}"
    
    # Test GET /users
    response=$(curl -s -w "HTTP %{http_code}" http://localhost:8081/users)
    if [[ $response == *"HTTP 200"* ]]; then
        print_success "GET /users - OK"
    else
        print_error "GET /users - FAILED ($response)"
    fi
    
    # Test GET /users/user001
    response=$(curl -s -w "HTTP %{http_code}" http://localhost:8081/users/user001)
    if [[ $response == *"HTTP 200"* ]]; then
        print_success "GET /users/user001 - OK"
    else
        print_warning "GET /users/user001 - Not found or error ($response)"
    fi
}

# Function to monitor alert frequency
monitor_alerts() {
    print_header "ALERT MONITORING"
    
    echo -e "${YELLOW}Recent Alert Activity (Last 30 seconds):${NC}"
    
    # Count recent alert logs
    recent_alerts=$(docker-compose logs --since=30s notification-service | grep "SMS sent" | wc -l)
    echo "Alerts sent in last 30 seconds: $recent_alerts"
    
    if [ "$recent_alerts" -gt 0 ]; then
        print_success "Alert system is active"
        echo -e "${YELLOW}Recent Alert Details:${NC}"
        docker-compose logs --since=30s notification-service | grep "SMS sent" | tail -5
    else
        print_warning "No alerts in the last 30 seconds"
    fi
}

# Function to show system performance
show_performance() {
    print_header "SYSTEM PERFORMANCE"
    
    echo -e "${YELLOW}Memory Usage:${NC}"
    free -h
    
    echo
    echo -e "${YELLOW}Disk Usage:${NC}"
    df -h
    
    echo
    echo -e "${YELLOW}Docker System Info:${NC}"
    docker system df
}

# Main monitoring function
main_monitor() {
    while true; do
        clear
        echo -e "${CYAN}🔍 Telco System Real-Time Monitor${NC}"
        echo -e "${CYAN}Last Updated: $(date)${NC}"
        echo
        
        get_service_metrics
        echo
        
        monitor_kafka
        echo
        
        monitor_database
        echo
        
        test_api_endpoints
        echo
        
        monitor_alerts
        echo
        
        show_performance
        
        echo
        echo -e "${YELLOW}Press Ctrl+C to exit monitoring${NC}"
        echo -e "${YELLOW}Refreshing in 10 seconds...${NC}"
        
        sleep 10
    done
}

# Interactive menu
show_menu() {
    echo "Select monitoring option:"
    echo "1. Full System Monitor (continuous)"
    echo "2. Service Health Check (one-time)"
    echo "3. Database Status (one-time)"
    echo "4. Kafka Status (one-time)"
    echo "5. Recent Logs (one-time)"
    echo "6. API Endpoint Test (one-time)"
    echo "7. Alert Activity (one-time)"
    echo "8. Exit"
    echo
    read -p "Enter your choice (1-8): " choice
    
    case $choice in
        1)
            main_monitor
            ;;
        2)
            get_service_metrics
            ;;
        3)
            monitor_database
            ;;
        4)
            monitor_kafka
            ;;
        5)
            monitor_logs
            ;;
        6)
            test_api_endpoints
            ;;
        7)
            monitor_alerts
            ;;
        8)
            echo "Goodbye!"
            exit 0
            ;;
        *)
            echo "Invalid option. Please try again."
            ;;
    esac
}

# Check if running interactively
if [ "$1" = "--continuous" ]; then
    main_monitor
else
    show_menu
fi

