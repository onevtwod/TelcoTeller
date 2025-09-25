# Telco Real-Time Alert System

Microservices demo that sends data usage alerts to users using Java Spring Boot, PostgreSQL, Kafka, Docker, and Jenkins.

## Services
- user-service: REST API with PostgreSQL for user data
- alert-trigger-service: Scheduler that publishes alerts to Kafka `sms-alerts`
- notification-service: Kafka consumer that logs SMS sends

## Run with Docker
Prerequisites: Docker and Docker Compose v2

```bash
docker compose up -d --build
```

Endpoints:
- user-service: http://localhost:8081
- alert-trigger-service: http://localhost:8082
- notification-service: http://localhost:8083

## CI/CD
`Jenkinsfile` builds, tests, and dockerizes services on push.
