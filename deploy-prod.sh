#!/bin/bash

# Production Deployment Script
set -e

echo "🚀 Starting Telco Production Deployment..."

# Check if .env file exists
if [ ! -f .env ]; then
    echo "❌ .env file not found. Please copy env.prod.template to .env and configure it."
    exit 1
fi

# Load environment variables
source .env

# Create necessary directories
mkdir -p nginx/logs
mkdir -p monitoring/grafana/dashboards
mkdir -p monitoring/grafana/datasources
mkdir -p postgres-init

# Build and start services
echo "📦 Building and starting production services..."
docker-compose -f docker-compose.prod.yml up -d --build

# Wait for services to be healthy
echo "⏳ Waiting for services to be healthy..."
sleep 30

# Check service health
echo "🔍 Checking service health..."
docker-compose -f docker-compose.prod.yml ps

# Display access information
echo ""
echo "✅ Production deployment completed!"
echo ""
echo "🌐 Access URLs:"
echo "   API: http://localhost/api/v1/users"
echo "   Prometheus: http://localhost:9090"
echo "   Grafana: http://localhost:3000"
echo ""
echo "📊 Monitoring:"
echo "   Health Check: http://localhost/health"
echo "   User Service Health: http://localhost/actuator/health"
echo ""
echo "🔧 Management Commands:"
echo "   View logs: docker-compose -f docker-compose.prod.yml logs -f"
echo "   Stop services: docker-compose -f docker-compose.prod.yml down"
echo "   Scale services: docker-compose -f docker-compose.prod.yml up -d --scale user-service=3"
echo ""
echo "⚠️  Remember to:"
echo "   1. Update DNS records to point to your server"
echo "   2. Configure SSL certificates in nginx/ssl/"
echo "   3. Set up proper firewall rules"
echo "   4. Configure backup strategies for volumes"
