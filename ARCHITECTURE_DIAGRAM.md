# Telco System Architecture

## System Overview

This document contains the Mermaid diagrams for the Telco microservices system architecture.

## High-Level Architecture

```mermaid
graph TB
    subgraph "Client Layer"
        WEB[Web Client]
        MOBILE[Mobile App]
        API_CLIENT[API Client]
    end

    subgraph "Load Balancer & Gateway"
        NGINX[Nginx Load Balancer]
    end

    subgraph "API Gateway & Security"
        RATE_LIMIT[Rate Limiting]
        CAPTCHA[CAPTCHA Validation]
        AUTH[JWT Authentication]
    end

    subgraph "Microservices Layer"
        USER_SVC[User Service<br/>Port: 8081]
        ALERT_SVC[Alert Trigger Service<br/>Port: 8082]
        NOTIF_SVC[Notification Service<br/>Port: 8083]
    end

    subgraph "Data Layer"
        POSTGRES[(PostgreSQL<br/>Database)]
        REDIS[(Redis<br/>Cache)]
    end

    subgraph "Message Queue"
        KAFKA[Apache Kafka]
        ZOOKEEPER[Zookeeper]
    end

    subgraph "Monitoring & Observability"
        PROMETHEUS[Prometheus<br/>Metrics]
        GRAFANA[Grafana<br/>Dashboards]
        LOKI[Loki<br/>Logs]
        JAEGER[Jaeger<br/>Tracing]
        ALERT_MGR[AlertManager<br/>Alerts]
    end

    subgraph "Infrastructure"
        K8S[Kubernetes Cluster]
        DOCKER[Docker Containers]
    end

    %% Client connections
    WEB --> NGINX
    MOBILE --> NGINX
    API_CLIENT --> NGINX

    %% Load balancer to services
    NGINX --> RATE_LIMIT
    RATE_LIMIT --> CAPTCHA
    CAPTCHA --> AUTH
    AUTH --> USER_SVC
    AUTH --> ALERT_SVC
    AUTH --> NOTIF_SVC

    %% Service interactions
    USER_SVC --> POSTGRES
    USER_SVC --> REDIS
    ALERT_SVC --> USER_SVC
    ALERT_SVC --> KAFKA
    NOTIF_SVC --> KAFKA

    %% Message queue
    KAFKA --> ZOOKEEPER

    %% Monitoring connections
    USER_SVC --> PROMETHEUS
    ALERT_SVC --> PROMETHEUS
    NOTIF_SVC --> PROMETHEUS
    PROMETHEUS --> GRAFANA
    PROMETHEUS --> ALERT_MGR
    USER_SVC --> LOKI
    ALERT_SVC --> LOKI
    NOTIF_SVC --> LOKI
    LOKI --> GRAFANA
    USER_SVC --> JAEGER
    ALERT_SVC --> JAEGER
    NOTIF_SVC --> JAEGER

    %% Infrastructure
    USER_SVC -.-> K8S
    ALERT_SVC -.-> K8S
    NOTIF_SVC -.-> K8S
    K8S -.-> DOCKER

    %% Styling
    classDef service fill:#e1f5fe,stroke:#01579b,stroke-width:2px
    classDef database fill:#f3e5f5,stroke:#4a148c,stroke-width:2px
    classDef monitoring fill:#e8f5e8,stroke:#1b5e20,stroke-width:2px
    classDef security fill:#fff3e0,stroke:#e65100,stroke-width:2px
    classDef infrastructure fill:#fce4ec,stroke:#880e4f,stroke-width:2px

    class USER_SVC,ALERT_SVC,NOTIF_SVC service
    class POSTGRES,REDIS,KAFKA,ZOOKEEPER database
    class PROMETHEUS,GRAFANA,LOKI,JAEGER,ALERT_MGR monitoring
    class RATE_LIMIT,CAPTCHA,AUTH security
    class NGINX,K8S,DOCKER infrastructure
```

## Detailed Service Architecture

```mermaid
graph TB
    subgraph "User Service (Port 8081)"
        USER_API[REST API<br/>/api/v1/users]
        USER_CONTROLLER[UserController]
        USER_SERVICE[UserService]
        USER_MAPPER[UserMapper]
        USER_CACHE[Redis Cache]
        USER_DB[(PostgreSQL)]
    end

    subgraph "Alert Trigger Service (Port 8082)"
        ALERT_SCHEDULER[UsageCheckScheduler]
        ALERT_SERVICE[AlertService]
        ALERT_PRODUCER[Kafka Producer]
    end

    subgraph "Notification Service (Port 8083)"
        NOTIF_CONSUMER[AlertConsumer]
        NOTIF_SERVICE[NotificationService]
        NOTIF_SENDER[Email/SMS Sender]
    end

    subgraph "Security Layer"
        RATE_LIMIT_FILTER[RateLimitingFilter]
        SECURITY_CONFIG[SecurityConfig]
        JWT_FILTER[JWT Filter]
    end

    subgraph "Data Layer"
        POSTGRES[(PostgreSQL<br/>Primary DB)]
        REDIS[(Redis<br/>Cache & Sessions)]
        KAFKA[Kafka<br/>Message Queue]
    end

    subgraph "Monitoring Stack"
        ACTUATOR[Spring Actuator<br/>Health & Metrics]
        PROMETHEUS[Prometheus<br/>Metrics Collection]
        GRAFANA[Grafana<br/>Visualization]
        LOKI[Loki<br/>Log Aggregation]
        JAEGER[Jaeger<br/>Distributed Tracing]
    end

    %% User Service Flow
    USER_API --> USER_CONTROLLER
    USER_CONTROLLER --> RATE_LIMIT_FILTER
    RATE_LIMIT_FILTER --> SECURITY_CONFIG
    SECURITY_CONFIG --> JWT_FILTER
    JWT_FILTER --> USER_SERVICE
    USER_SERVICE --> USER_CACHE
    USER_SERVICE --> USER_MAPPER
    USER_MAPPER --> USER_DB

    %% Alert Service Flow
    ALERT_SCHEDULER --> ALERT_SERVICE
    ALERT_SERVICE --> USER_SERVICE
    ALERT_SERVICE --> ALERT_PRODUCER
    ALERT_PRODUCER --> KAFKA

    %% Notification Service Flow
    KAFKA --> NOTIF_CONSUMER
    NOTIF_CONSUMER --> NOTIF_SERVICE
    NOTIF_SERVICE --> NOTIF_SENDER

    %% Monitoring Flow
    USER_SERVICE --> ACTUATOR
    ALERT_SERVICE --> ACTUATOR
    NOTIF_SERVICE --> ACTUATOR
    ACTUATOR --> PROMETHEUS
    PROMETHEUS --> GRAFANA
    USER_SERVICE --> LOKI
    ALERT_SERVICE --> LOKI
    NOTIF_SERVICE --> LOKI
    LOKI --> GRAFANA
    USER_SERVICE --> JAEGER
    ALERT_SERVICE --> JAEGER
    NOTIF_SERVICE --> JAEGER

    %% Styling
    classDef service fill:#e3f2fd,stroke:#0277bd,stroke-width:2px
    classDef data fill:#f1f8e9,stroke:#33691e,stroke-width:2px
    classDef security fill:#fff8e1,stroke:#f57c00,stroke-width:2px
    classDef monitoring fill:#fce4ec,stroke:#c2185b,stroke-width:2px

    class USER_SERVICE,ALERT_SERVICE,NOTIF_SERVICE service
    class POSTGRES,REDIS,KAFKA data
    class RATE_LIMIT_FILTER,SECURITY_CONFIG,JWT_FILTER security
    class ACTUATOR,PROMETHEUS,GRAFANA,LOKI,JAEGER monitoring
```

## Data Flow Architecture

```mermaid
sequenceDiagram
    participant Client
    participant Nginx
    participant UserService
    participant AlertService
    participant NotificationService
    participant PostgreSQL
    participant Redis
    participant Kafka
    participant Monitoring

    Client->>Nginx: HTTP Request
    Nginx->>UserService: Forward Request
    UserService->>Redis: Check Cache
    alt Cache Hit
        Redis-->>UserService: Return Cached Data
    else Cache Miss
        UserService->>PostgreSQL: Query Database
        PostgreSQL-->>UserService: Return Data
        UserService->>Redis: Store in Cache
    end
    UserService-->>Nginx: Return Response
    Nginx-->>Client: HTTP Response

    Note over UserService,AlertService: Background Process
    AlertService->>UserService: Check User Thresholds
    UserService-->>AlertService: Return Users Above Threshold
    AlertService->>Kafka: Send Alert Message
    Kafka->>NotificationService: Consume Alert
    NotificationService->>Client: Send Notification

    Note over UserService,Monitoring: Monitoring
    UserService->>Monitoring: Send Metrics
    UserService->>Monitoring: Send Logs
    UserService->>Monitoring: Send Traces
```

## Deployment Architecture

```mermaid
graph TB
    subgraph "Kubernetes Cluster"
        subgraph "Namespace: telco-system"
            subgraph "User Service Pods"
                USER_POD1[User Service Pod 1]
                USER_POD2[User Service Pod 2]
            end
            
            subgraph "Alert Service Pods"
                ALERT_POD1[Alert Service Pod 1]
                ALERT_POD2[Alert Service Pod 2]
            end
            
            subgraph "Notification Service Pods"
                NOTIF_POD1[Notification Service Pod 1]
                NOTIF_POD2[Notification Service Pod 2]
            end
            
            subgraph "Infrastructure Pods"
                POSTGRES_POD[PostgreSQL Pod]
                REDIS_POD[Redis Pod]
                KAFKA_POD[Kafka Pod]
                ZOOKEEPER_POD[Zookeeper Pod]
            end
            
            subgraph "Monitoring Pods"
                PROMETHEUS_POD[Prometheus Pod]
                GRAFANA_POD[Grafana Pod]
                LOKI_POD[Loki Pod]
                JAEGER_POD[Jaeger Pod]
                ALERTMGR_POD[AlertManager Pod]
            end
        end
        
        subgraph "Ingress Controller"
            NGINX_INGRESS[Nginx Ingress]
        end
        
        subgraph "Service Mesh"
            ISTIO[Istio Service Mesh]
        end
    end

    subgraph "External Services"
        DNS[DNS Provider]
        SSL[SSL Certificates]
        CLOUD[Cloud Provider]
    end

    %% Connections
    DNS --> NGINX_INGRESS
    NGINX_INGRESS --> ISTIO
    ISTIO --> USER_POD1
    ISTIO --> USER_POD2
    ISTIO --> ALERT_POD1
    ISTIO --> ALERT_POD2
    ISTIO --> NOTIF_POD1
    ISTIO --> NOTIF_POD2

    %% Internal connections
    USER_POD1 --> POSTGRES_POD
    USER_POD2 --> POSTGRES_POD
    USER_POD1 --> REDIS_POD
    USER_POD2 --> REDIS_POD
    ALERT_POD1 --> KAFKA_POD
    ALERT_POD2 --> KAFKA_POD
    NOTIF_POD1 --> KAFKA_POD
    NOTIF_POD2 --> KAFKA_POD
    KAFKA_POD --> ZOOKEEPER_POD

    %% Monitoring connections
    USER_POD1 --> PROMETHEUS_POD
    USER_POD2 --> PROMETHEUS_POD
    ALERT_POD1 --> PROMETHEUS_POD
    ALERT_POD2 --> PROMETHEUS_POD
    NOTIF_POD1 --> PROMETHEUS_POD
    NOTIF_POD2 --> PROMETHEUS_POD
    PROMETHEUS_POD --> GRAFANA_POD
    PROMETHEUS_POD --> ALERTMGR_POD

    %% Styling
    classDef pod fill:#e8f5e8,stroke:#2e7d32,stroke-width:2px
    classDef infrastructure fill:#e3f2fd,stroke:#1565c0,stroke-width:2px
    classDef external fill:#fff3e0,stroke:#ef6c00,stroke-width:2px

    class USER_POD1,USER_POD2,ALERT_POD1,ALERT_POD2,NOTIF_POD1,NOTIF_POD2 pod
    class POSTGRES_POD,REDIS_POD,KAFKA_POD,ZOOKEEPER_POD,PROMETHEUS_POD,GRAFANA_POD,LOKI_POD,JAEGER_POD,ALERTMGR_POD infrastructure
    class DNS,SSL,CLOUD external
```

## Security Architecture

```mermaid
graph TB
    subgraph "External Threats"
        BOT[Bots & Crawlers]
        DDOS[DDoS Attacks]
        INJECTION[SQL Injection]
        XSS[XSS Attacks]
        CSRF[CSRF Attacks]
    end

    subgraph "Security Layers"
        WAF[Web Application Firewall]
        RATE_LIMIT[Rate Limiting<br/>Bucket4j]
        CAPTCHA[CAPTCHA v3<br/>reCAPTCHA]
        JWT[JWT Authentication]
        CORS[CORS Protection]
        HEADERS[Security Headers]
    end

    subgraph "Application Security"
        VALIDATION[Input Validation<br/>Bean Validation]
        ENCRYPTION[Data Encryption<br/>AES-256]
        RLS[Row Level Security<br/>PostgreSQL RLS]
        SECRETS[Secret Management<br/>Environment Variables]
    end

    subgraph "Infrastructure Security"
        NETWORK[Network Isolation]
        SSL[SSL/TLS Encryption]
        CONTAINER[Container Security]
        K8S_SEC[Kubernetes Security]
    end

    subgraph "Monitoring & Detection"
        LOGS[Security Logging]
        ALERTS[Security Alerts]
        AUDIT[Audit Trails]
        SCANNING[Vulnerability Scanning]
    end

    %% Threat mitigation
    BOT --> CAPTCHA
    DDOS --> RATE_LIMIT
    INJECTION --> VALIDATION
    XSS --> HEADERS
    CSRF --> CORS

    %% Security flow
    WAF --> RATE_LIMIT
    RATE_LIMIT --> CAPTCHA
    CAPTCHA --> JWT
    JWT --> VALIDATION
    VALIDATION --> ENCRYPTION
    ENCRYPTION --> RLS

    %% Infrastructure security
    NETWORK --> SSL
    SSL --> CONTAINER
    CONTAINER --> K8S_SEC

    %% Monitoring
    VALIDATION --> LOGS
    LOGS --> ALERTS
    ALERTS --> AUDIT
    AUDIT --> SCANNING

    %% Styling
    classDef threat fill:#ffebee,stroke:#c62828,stroke-width:2px
    classDef security fill:#e8f5e8,stroke:#2e7d32,stroke-width:2px
    classDef infrastructure fill:#e3f2fd,stroke:#1565c0,stroke-width:2px
    classDef monitoring fill:#fff3e0,stroke:#ef6c00,stroke-width:2px

    class BOT,DDOS,INJECTION,XSS,CSRF threat
    class WAF,RATE_LIMIT,CAPTCHA,JWT,CORS,HEADERS,VALIDATION,ENCRYPTION,RLS,SECRETS security
    class NETWORK,SSL,CONTAINER,K8S_SEC infrastructure
    class LOGS,ALERTS,AUDIT,SCANNING monitoring
```

## Technology Stack

### Backend Services
- **Java 21** - Programming Language
- **Spring Boot 3.2.0** - Framework
- **MyBatis** - ORM
- **Flyway** - Database Migrations
- **PostgreSQL 15** - Primary Database
- **Redis 7** - Caching & Sessions

### Message Queue
- **Apache Kafka 7.6.1** - Message Broker
- **Zookeeper** - Kafka Coordination

### Security
- **Spring Security 6** - Authentication & Authorization
- **JWT** - Token-based Authentication
- **BCrypt** - Password Hashing
- **Bucket4j** - Rate Limiting
- **reCAPTCHA v3** - Bot Protection

### Monitoring & Observability
- **Prometheus** - Metrics Collection
- **Grafana** - Visualization & Dashboards
- **Loki** - Log Aggregation
- **Jaeger** - Distributed Tracing
- **AlertManager** - Alert Management

### Infrastructure
- **Docker** - Containerization
- **Kubernetes** - Container Orchestration
- **Nginx** - Load Balancer & Reverse Proxy
- **Helm** - Package Management

### CI/CD
- **Jenkins** - Continuous Integration
- **Maven** - Build Tool
- **SonarQube** - Code Quality
- **OWASP Dependency Check** - Security Scanning
- **Trivy** - Container Security Scanning

## Performance Characteristics

- **Throughput**: 10,000+ requests/second
- **Latency**: < 100ms (95th percentile)
- **Availability**: 99.9% uptime
- **Scalability**: Auto-scaling based on CPU/Memory
- **Caching**: Redis with 10-minute TTL
- **Database**: Connection pooling with 50 max connections
