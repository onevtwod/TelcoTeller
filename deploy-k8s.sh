#!/bin/bash

# Telco System Kubernetes Deployment Script
# This script deploys the Telco system with proper auto-scaling on Kubernetes

echo "🚀 Telco System Kubernetes Deployment"
echo "===================================="

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

# Check prerequisites
check_prerequisites() {
    print_header "CHECKING PREREQUISITES"
    
    # Check if kubectl is installed
    if ! command -v kubectl &> /dev/null; then
        print_error "kubectl is not installed. Please install kubectl first."
        exit 1
    fi
    
    # Check if cluster is accessible
    if ! kubectl cluster-info &> /dev/null; then
        print_error "Cannot connect to Kubernetes cluster. Please check your kubeconfig."
        exit 1
    fi
    
    # Check if cluster has metrics-server
    if ! kubectl get deployment metrics-server -n kube-system &> /dev/null; then
        print_warning "Metrics server not found. Installing..."
        kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
    fi
    
    print_success "Prerequisites check passed"
}

# Build and push Docker images
build_images() {
    print_header "BUILDING DOCKER IMAGES"
    
    # Build user-service
    print_status "Building user-service..."
    cd user-service
    docker build -t telco/user-service:latest .
    cd ..
    
    # Build alert-trigger-service
    print_status "Building alert-trigger-service..."
    cd alert-trigger-service
    docker build -t telco/alert-trigger-service:latest .
    cd ..
    
    # Build notification-service
    print_status "Building notification-service..."
    cd notification-service
    docker build -t telco/notification-service:latest .
    cd ..
    
    print_success "Docker images built successfully"
}

# Deploy the system
deploy_system() {
    print_header "DEPLOYING TELCO SYSTEM"
    
    # Create namespace
    print_status "Creating namespace..."
    kubectl apply -f k8s/namespace.yaml
    
    # Deploy infrastructure
    print_status "Deploying infrastructure (PostgreSQL, Kafka)..."
    kubectl apply -f k8s/postgres.yaml
    kubectl apply -f k8s/kafka.yaml
    
    # Wait for infrastructure to be ready
    print_status "Waiting for infrastructure to be ready..."
    kubectl wait --for=condition=available --timeout=300s deployment/postgres -n telco-system
    kubectl wait --for=condition=available --timeout=300s deployment/kafka -n telco-system
    
    # Deploy services
    print_status "Deploying services..."
    kubectl apply -f k8s/user-service.yaml
    kubectl apply -f k8s/alert-trigger-service.yaml
    kubectl apply -f k8s/notification-service.yaml
    
    # Deploy monitoring
    print_status "Deploying monitoring stack..."
    kubectl apply -f k8s/monitoring.yaml
    
    # Deploy VPA (if available)
    if kubectl get crd verticalpodautoscalers.autoscaling.k8s.io &> /dev/null; then
        print_status "Deploying VPA..."
        kubectl apply -f k8s/vpa.yaml
    else
        print_warning "VPA CRD not found. Skipping VPA deployment."
    fi
    
    # Deploy ingress
    print_status "Deploying ingress..."
    kubectl apply -f k8s/ingress.yaml
    
    print_success "System deployed successfully"
}

# Wait for deployment
wait_for_deployment() {
    print_header "WAITING FOR DEPLOYMENT"
    
    print_status "Waiting for all deployments to be ready..."
    kubectl wait --for=condition=available --timeout=600s deployment/user-service -n telco-system
    kubectl wait --for=condition=available --timeout=600s deployment/alert-trigger-service -n telco-system
    kubectl wait --for=condition=available --timeout=600s deployment/notification-service -n telco-system
    
    print_success "All services are ready"
}

# Show status
show_status() {
    print_header "DEPLOYMENT STATUS"
    
    print_status "Pods status:"
    kubectl get pods -n telco-system
    
    echo
    print_status "Services:"
    kubectl get services -n telco-system
    
    echo
    print_status "HPA status:"
    kubectl get hpa -n telco-system
    
    echo
    print_status "Ingress:"
    kubectl get ingress -n telco-system
}

# Test auto-scaling
test_autoscaling() {
    print_header "TESTING AUTO-SCALING"
    
    print_status "Generating load to trigger auto-scaling..."
    
    # Get service URLs
    USER_SERVICE_URL=$(kubectl get service user-service -n telco-system -o jsonpath='{.spec.clusterIP}')
    
    # Generate load
    for i in {1..100}; do
        kubectl run load-test-$i --image=busybox --rm -i --restart=Never -- \
            wget -qO- http://$USER_SERVICE_URL:8081/actuator/health &
    done
    
    print_status "Waiting for auto-scaling to respond..."
    sleep 60
    
    print_status "Checking HPA status:"
    kubectl get hpa -n telco-system
    
    print_status "Current pod count:"
    kubectl get pods -n telco-system
}

# Cleanup
cleanup() {
    print_header "CLEANING UP"
    
    print_status "Deleting all resources..."
    kubectl delete namespace telco-system
    
    print_success "Cleanup completed"
}

# Show help
show_help() {
    echo "Telco System Kubernetes Deployment"
    echo
    echo "Usage: $0 [COMMAND]"
    echo
    echo "Commands:"
    echo "  deploy     Deploy the complete system"
    echo "  status     Show deployment status"
    echo "  test       Test auto-scaling"
    echo "  cleanup    Remove all resources"
    echo "  help       Show this help"
    echo
    echo "Examples:"
    echo "  $0 deploy"
    echo "  $0 status"
    echo "  $0 test"
}

# Main execution
case "$1" in
    "deploy")
        check_prerequisites
        build_images
        deploy_system
        wait_for_deployment
        show_status
        ;;
    "status")
        show_status
        ;;
    "test")
        test_autoscaling
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
