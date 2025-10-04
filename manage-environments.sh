#!/bin/bash

# Telco System Environment Management Script
# This script manages different environments (dev, staging, prod, test)

echo "🏗️ Telco System Environment Manager"
echo "==================================="

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

# Function to start development environment
start_dev() {
    print_header "STARTING DEVELOPMENT ENVIRONMENT"
    
    print_status "Starting development services..."
    docker-compose -f docker-compose.dev.yml up -d --build
    
    print_status "Waiting for services to start..."
    sleep 30
    
    print_status "Checking service health..."
    check_services "dev"
    
    print_success "Development environment started!"
    print_info "Services available at:"
    print_info "  User Service: http://localhost:8081/api/v1"
    print_info "  Swagger UI: http://localhost:8081/api/v1/swagger-ui.html"
    print_info "  Health Check: http://localhost:8081/api/v1/actuator/health"
}

# Function to start staging environment
start_staging() {
    print_header "STARTING STAGING ENVIRONMENT"
    
    if [ -z "$IMAGE_TAG" ]; then
        print_error "IMAGE_TAG environment variable is required for staging"
        print_info "Usage: IMAGE_TAG=v1.0.0 $0 staging"
        exit 1
    fi
    
    print_status "Starting staging services with image tag: $IMAGE_TAG"
    docker-compose -f docker-compose.staging.yml up -d
    
    print_status "Waiting for services to start..."
    sleep 30
    
    print_status "Checking service health..."
    check_services "staging"
    
    print_success "Staging environment started!"
    print_info "Services available at:"
    print_info "  User Service: http://localhost:8081/api/v1"
    print_info "  Swagger UI: http://localhost:8081/api/v1/swagger-ui.html"
}

# Function to start test environment
start_test() {
    print_header "STARTING TEST ENVIRONMENT"
    
    print_status "Starting test services..."
    docker-compose -f docker-compose.test.yml up -d --build
    
    print_status "Waiting for services to start..."
    sleep 30
    
    print_status "Running tests..."
    run_tests
    
    print_success "Test environment completed!"
}

# Function to run comprehensive tests
run_tests() {
    print_header "RUNNING COMPREHENSIVE TESTS"
    
    print_status "Running unit tests..."
    mvn test -Dspring.profiles.active=test
    
    print_status "Running integration tests..."
    mvn test -Dtest=*IntegrationTest -Dspring.profiles.active=test
    
    print_status "Running TestContainers tests..."
    mvn test -Dtest=*TestContainers* -Dspring.profiles.active=test
    
    print_status "Generating test coverage report..."
    mvn jacoco:report
    
    print_success "All tests completed!"
}

# Function to check service health
check_services() {
    local environment=$1
    
    print_status "Checking service health for $environment environment..."
    
    # Check user service
    if curl -s -f http://localhost:8081/api/v1/actuator/health > /dev/null; then
        print_success "User Service is healthy"
    else
        print_error "User Service is not responding"
    fi
    
    # Check alert service
    if curl -s -f http://localhost:8082/actuator/health > /dev/null; then
        print_success "Alert Service is healthy"
    else
        print_error "Alert Service is not responding"
    fi
    
    # Check notification service
    if curl -s -f http://localhost:8083/actuator/health > /dev/null; then
        print_success "Notification Service is healthy"
    else
        print_error "Notification Service is not responding"
    fi
}

# Function to show environment status
show_status() {
    print_header "ENVIRONMENT STATUS"
    
    print_status "Docker containers:"
    docker-compose ps
    
    echo
    print_status "Service health:"
    check_services "current"
    
    echo
    print_status "Database status:"
    docker exec telco-postgres-1 psql -U telco -d telco -c "SELECT COUNT(*) as user_count FROM users;" 2>/dev/null || echo "Database not accessible"
}

# Function to stop all environments
stop_all() {
    print_header "STOPPING ALL ENVIRONMENTS"
    
    print_status "Stopping all services..."
    docker-compose -f docker-compose.dev.yml down
    docker-compose -f docker-compose.staging.yml down
    docker-compose -f docker-compose.test.yml down
    docker-compose -f docker-compose.yml down
    
    print_success "All environments stopped!"
}

# Function to clean up
cleanup() {
    print_header "CLEANING UP"
    
    print_status "Removing containers and volumes..."
    docker-compose -f docker-compose.dev.yml down -v
    docker-compose -f docker-compose.staging.yml down -v
    docker-compose -f docker-compose.test.yml down -v
    docker-compose -f docker-compose.yml down -v
    
    print_status "Removing unused images..."
    docker image prune -f
    
    print_success "Cleanup completed!"
}

# Function to show help
show_help() {
    echo "Telco System Environment Manager"
    echo
    echo "Usage: $0 [COMMAND] [OPTIONS]"
    echo
    echo "Commands:"
    echo "  dev                 Start development environment"
    echo "  staging [TAG]       Start staging environment (requires IMAGE_TAG)"
    echo "  test                Start test environment and run tests"
    echo "  status              Show current environment status"
    echo "  stop                Stop all environments"
    echo "  cleanup             Clean up containers and volumes"
    echo "  help                Show this help message"
    echo
    echo "Examples:"
    echo "  $0 dev"
    echo "  IMAGE_TAG=v1.0.0 $0 staging"
    echo "  $0 test"
    echo "  $0 status"
}

# Main execution
case "$1" in
    "dev")
        start_dev
        ;;
    "staging")
        start_staging
        ;;
    "test")
        start_test
        ;;
    "status")
        show_status
        ;;
    "stop")
        stop_all
        ;;
    "cleanup")
        cleanup
        ;;
    "help"|"--help"|"-h")
        show_help
        ;;
    *)
        print_error "Unknown command: $1"
        echo
        show_help
        exit 1
        ;;
esac
