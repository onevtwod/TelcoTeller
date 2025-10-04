# Environment Setup Guide

## Overview

This guide explains how to set up and manage different environments for the Telco system. The system supports multiple environments with proper configuration management, testing, and deployment strategies.

## Environment Types

### 1. Development Environment (`dev`)
- **Purpose**: Local development and testing
- **Database**: PostgreSQL with development data
- **Configuration**: `application-dev.properties`
- **Docker Compose**: `docker-compose.dev.yml`
- **Features**: 
  - Debug logging enabled
  - Swagger UI enabled
  - Flyway clean enabled
  - Hot reload support

### 2. Staging Environment (`staging`)
- **Purpose**: Pre-production testing and validation
- **Database**: PostgreSQL with staging data
- **Configuration**: `application-staging.properties`
- **Docker Compose**: `docker-compose.staging.yml`
- **Features**:
  - Production-like configuration
  - Swagger UI enabled
  - File logging enabled
  - Security enabled

### 3. Production Environment (`prod`)
- **Purpose**: Live production system
- **Database**: PostgreSQL with production data
- **Configuration**: `application-prod.properties`
- **Deployment**: Kubernetes
- **Features**:
  - Optimized logging
  - Swagger UI disabled
  - SSL enabled
  - Compression enabled
  - Security hardened

### 4. Test Environment (`test`)
- **Purpose**: Automated testing
- **Database**: H2 in-memory database
- **Configuration**: `application-test.properties`
- **Docker Compose**: `docker-compose.test.yml`
- **Features**:
  - Fast test execution
  - TestContainers support
  - Swagger UI disabled
  - Debug logging enabled

## Quick Start

### Development Environment
```bash
# Start development environment
./manage-environments.sh dev

# Access services
# User Service: http://localhost:8081/api/v1
# Swagger UI: http://localhost:8081/api/v1/swagger-ui.html
# Health Check: http://localhost:8081/api/v1/actuator/health
```

### Staging Environment
```bash
# Start staging environment
IMAGE_TAG=v1.0.0 ./manage-environments.sh staging

# Access services
# User Service: http://localhost:8081/api/v1
# Swagger UI: http://localhost:8081/api/v1/swagger-ui.html
```

### Test Environment
```bash
# Run tests
./manage-environments.sh test

# This will:
# 1. Start test environment
# 2. Run unit tests
# 3. Run integration tests
# 4. Run TestContainers tests
# 5. Generate coverage report
```

## Environment Management

### Using the Management Script
```bash
# Show help
./manage-environments.sh help

# Check status
./manage-environments.sh status

# Stop all environments
./manage-environments.sh stop

# Clean up
./manage-environments.sh cleanup
```

### Manual Commands

#### Development
```bash
# Start development environment
docker-compose -f docker-compose.dev.yml up -d --build

# Stop development environment
docker-compose -f docker-compose.dev.yml down

# View logs
docker-compose -f docker-compose.dev.yml logs -f user-service
```

#### Staging
```bash
# Start staging environment
IMAGE_TAG=v1.0.0 docker-compose -f docker-compose.staging.yml up -d

# Stop staging environment
docker-compose -f docker-compose.staging.yml down
```

#### Testing
```bash
# Start test environment
docker-compose -f docker-compose.test.yml up -d --build

# Run tests
mvn test -Dspring.profiles.active=test

# Stop test environment
docker-compose -f docker-compose.test.yml down
```

## Configuration Management

### Environment Variables

#### Development
```bash
export SPRING_PROFILES_ACTIVE=dev
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/telco_dev
export SPRING_DATASOURCE_USERNAME=telco_dev
export SPRING_DATASOURCE_PASSWORD=telco_dev
```

#### Staging
```bash
export SPRING_PROFILES_ACTIVE=staging
export SPRING_DATASOURCE_URL=jdbc:postgresql://staging-db:5432/telco_staging
export SPRING_DATASOURCE_USERNAME=telco_staging
export SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
export IMAGE_TAG=v1.0.0
```

#### Production
```bash
export SPRING_PROFILES_ACTIVE=prod
export SPRING_DATASOURCE_URL=${PROD_DATABASE_URL}
export SPRING_DATASOURCE_USERNAME=${PROD_DATABASE_USERNAME}
export SPRING_DATASOURCE_PASSWORD=${PROD_DATABASE_PASSWORD}
```

### Database Configuration

#### Development Database
- **Host**: localhost:5432
- **Database**: telco_dev
- **Username**: telco_dev
- **Password**: telco_dev

#### Staging Database
- **Host**: staging-db:5432
- **Database**: telco_staging
- **Username**: telco_staging
- **Password**: ${DB_PASSWORD}

#### Production Database
- **Host**: ${PROD_DATABASE_HOST}
- **Database**: ${PROD_DATABASE_NAME}
- **Username**: ${PROD_DATABASE_USERNAME}
- **Password**: ${PROD_DATABASE_PASSWORD}

## Testing Strategy

### Test Types

#### 1. Unit Tests
- **Location**: `src/test/java/com/telco/userservice/service/`
- **Configuration**: `application-test.properties`
- **Database**: H2 in-memory
- **Command**: `mvn test -Dspring.profiles.active=test`

#### 2. Integration Tests
- **Location**: `src/test/java/com/telco/userservice/web/`
- **Configuration**: `application-test.properties`
- **Database**: H2 in-memory
- **Command**: `mvn test -Dtest=*IntegrationTest`

#### 3. TestContainers Tests
- **Location**: `src/test/java/com/telco/userservice/TestContainersIntegrationTest.java`
- **Configuration**: Real PostgreSQL container
- **Command**: `mvn test -Dtest=*TestContainers*`

#### 4. End-to-End Tests
- **Location**: `src/test/java/com/telco/userservice/`
- **Configuration**: `docker-compose.test.yml`
- **Command**: `./manage-environments.sh test`

### Test Coverage

#### Coverage Reports
- **Location**: `target/site/jacoco/index.html`
- **Command**: `mvn jacoco:report`
- **Threshold**: 80% minimum

#### Coverage Types
- **Line Coverage**: 80%
- **Branch Coverage**: 70%
- **Method Coverage**: 80%
- **Class Coverage**: 90%

## Monitoring and Health Checks

### Health Check Endpoints

#### User Service
- **Health**: `GET /api/v1/actuator/health`
- **Info**: `GET /api/v1/actuator/info`
- **Metrics**: `GET /api/v1/actuator/metrics`
- **Prometheus**: `GET /api/v1/actuator/prometheus`

#### Alert Service
- **Health**: `GET /actuator/health`
- **Info**: `GET /actuator/info`

#### Notification Service
- **Health**: `GET /actuator/health`
- **Info**: `GET /actuator/info`

### Monitoring Tools

#### Development
- **Swagger UI**: http://localhost:8081/api/v1/swagger-ui.html
- **Health Dashboard**: http://localhost:8081/api/v1/actuator/health

#### Staging
- **Swagger UI**: http://localhost:8081/api/v1/swagger-ui.html
- **Health Dashboard**: http://localhost:8081/api/v1/actuator/health

#### Production
- **Health Dashboard**: https://api.telco.com/api/v1/actuator/health
- **Prometheus**: https://monitoring.telco.com/prometheus
- **Grafana**: https://monitoring.telco.com/grafana

## Troubleshooting

### Common Issues

#### 1. Database Connection Issues
```bash
# Check database status
docker-compose ps postgres

# Check database logs
docker-compose logs postgres

# Test database connection
docker exec -it telco-postgres-1 psql -U telco -d telco
```

#### 2. Service Health Issues
```bash
# Check service status
./manage-environments.sh status

# Check service logs
docker-compose logs user-service

# Test service health
curl http://localhost:8081/api/v1/actuator/health
```

#### 3. Test Failures
```bash
# Run specific test
mvn test -Dtest=UserServiceTest

# Run with debug
mvn test -Dtest=UserServiceTest -X

# Check test logs
mvn test -Dtest=UserServiceTest -Dlogging.level.com.telco.userservice=DEBUG
```

### Log Locations

#### Development
- **Console**: Standard output
- **File**: `./logs/user-service.log`

#### Staging
- **File**: `/app/logs/user-service.log`
- **Console**: Standard output

#### Production
- **File**: `/app/logs/user-service.log`
- **Console**: Standard output

## Best Practices

### Environment Management
1. **Always use environment-specific configurations**
2. **Never commit sensitive data to version control**
3. **Use environment variables for configuration**
4. **Test in staging before production deployment**

### Testing
1. **Write tests for all business logic**
2. **Use TestContainers for integration tests**
3. **Maintain high test coverage**
4. **Run tests in CI/CD pipeline**

### Monitoring
1. **Monitor health endpoints**
2. **Set up alerts for failures**
3. **Use structured logging**
4. **Monitor performance metrics**

### Security
1. **Use environment-specific secrets**
2. **Enable security in staging and production**
3. **Disable debug endpoints in production**
4. **Use HTTPS in production**

## Support

For issues and questions:
- **Development Team**: dev@telco.com
- **Documentation**: [Internal Wiki]
- **Issue Tracking**: [JIRA]
- **Slack**: #telco-dev
