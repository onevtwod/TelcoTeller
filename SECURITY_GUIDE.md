# Security Implementation Guide

## Overview

This guide outlines the comprehensive security measures implemented in the Telco system to protect against common threats and ensure data privacy.

## Security Features Implemented

### 1. Rate Limiting ✅

**Purpose**: Prevent API abuse and DDoS attacks

**Implementation**:
- **Bucket4j**: Token bucket algorithm for rate limiting
- **Rate**: 100 requests per minute per IP
- **Burst**: 20 requests burst capacity
- **Headers**: X-RateLimit-Limit, X-RateLimit-Remaining, X-RateLimit-Reset

**Configuration**:
```properties
# Rate limiting settings
rate.limit.requests.per.minute=100
rate.limit.burst.capacity=20
rate.limit.time.window=60
```

**Usage**:
```java
@Autowired
private RateLimitingConfig rateLimitingConfig;

// Check if request is rate limited
if (rateLimitingConfig.isRateLimited(clientIp)) {
    // Handle rate limit exceeded
}
```

### 2. Row Level Security (RLS) ✅

**Purpose**: Ensure users can only access their own data

**Implementation**:
- **PostgreSQL RLS**: Enabled on all user tables
- **Policies**: Users can only see/modify their own data
- **Context Functions**: `get_current_user_id()`, `set_user_context()`

**Database Policies**:
```sql
-- Enable RLS
ALTER TABLE users ENABLE ROW LEVEL SECURITY;

-- Create policy
CREATE POLICY user_own_data_policy ON users
    FOR ALL TO PUBLIC
    USING (user_id = get_current_user_id())
    WITH CHECK (user_id = get_current_user_id());
```

**Application Usage**:
```java
// Set user context before database operations
@Autowired
private JdbcTemplate jdbcTemplate;

public void setUserContext(String userId) {
    jdbcTemplate.execute("SELECT set_user_context('" + userId + "')");
}
```

### 3. Secret Manager Integration ✅

**Purpose**: Secure storage and management of sensitive data

**Implementation**:
- **Environment Variables**: Primary secret storage
- **Fallback**: System properties for development
- **Integration Ready**: AWS Secrets Manager, Azure Key Vault, HashiCorp Vault

**Supported Secrets**:
- Database passwords
- JWT secrets
- API keys
- Encryption keys
- External service credentials

**Usage**:
```java
@Autowired
private SecretManagerConfig.SecretManagerService secretManager;

// Get secrets
String dbPassword = secretManager.getDatabasePassword();
String jwtSecret = secretManager.getJwtSecret();
String apiKey = secretManager.getApiKey();
```

### 4. CAPTCHA Integration ✅

**Purpose**: Prevent bot attacks and automated abuse

**Implementation**:
- **reCAPTCHA v3**: Invisible CAPTCHA with score-based verification
- **Score Threshold**: 0.5 minimum score
- **Environment Aware**: Disabled in development, enabled in production

**Configuration**:
```properties
# CAPTCHA settings
captcha.site.key=your-recaptcha-site-key
captcha.secret.key=your-recaptcha-secret-key
captcha.minimum.score=0.5
captcha.enabled=true
```

**Usage**:
```java
@Autowired
private CaptchaConfig.CaptchaService captchaService;

// Verify CAPTCHA
boolean isValid = captchaService.verifyCaptcha(captchaToken, clientIp);
if (!isValid) {
    // Handle invalid CAPTCHA
}
```

## Security Configuration

### Environment Variables

**Required Secrets** (set in environment variables):
```bash
# Database
DB_PASSWORD=your-secure-database-password

# JWT
JWT_SECRET=your-super-secret-jwt-key-at-least-256-bits-long

# Encryption
ENCRYPTION_KEY=your-encryption-key-32-characters-long

# API Keys
API_KEY=your-api-key
SMS_API_KEY=your-sms-provider-api-key

# CAPTCHA
CAPTCHA_SITE_KEY=your-recaptcha-site-key
CAPTCHA_SECRET_KEY=your-recaptcha-secret-key
```

### Security Headers

**Implemented Headers**:
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `X-XSS-Protection: 1; mode=block`
- `Strict-Transport-Security: max-age=31536000; includeSubDomains`
- `Content-Security-Policy: default-src 'self'`

### CORS Configuration

**Development**:
- Allow all origins
- Allow all methods
- Allow all headers

**Production**:
- Restricted origins
- Limited methods
- Specific headers only

## Security Best Practices

### 1. Authentication & Authorization

**JWT Implementation**:
```java
// Generate JWT token
String token = Jwts.builder()
    .setSubject(userId)
    .setExpiration(new Date(System.currentTimeMillis() + expiration))
    .signWith(SignatureAlgorithm.HS512, secret)
    .compact();

// Validate JWT token
Claims claims = Jwts.parser()
    .setSigningKey(secret)
    .parseClaimsJws(token)
    .getBody();
```

**Password Security**:
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12); // 12 rounds
}
```

### 2. Input Validation

**Bean Validation**:
```java
@Entity
public class User {
    @NotBlank(message = "User ID is required")
    @Size(min = 3, max = 50, message = "User ID must be between 3 and 50 characters")
    private String userId;
    
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phoneNumber;
    
    @Min(value = 0, message = "Usage cannot be negative")
    private Long currentUsage;
}
```

### 3. SQL Injection Prevention

**MyBatis Parameterized Queries**:
```xml
<select id="findById" parameterType="string" resultType="User">
    SELECT * FROM users WHERE user_id = #{userId}
</select>
```

**Never use string concatenation**:
```java
// ❌ BAD - SQL injection risk
String sql = "SELECT * FROM users WHERE user_id = '" + userId + "'";

// ✅ GOOD - Parameterized query
String sql = "SELECT * FROM users WHERE user_id = ?";
```

### 4. Data Encryption

**Sensitive Data Encryption**:
```java
@Autowired
private SecretManagerConfig.SecretManagerService secretManager;

public String encryptSensitiveData(String data) {
    String key = secretManager.getEncryptionKey();
    // Implement encryption logic
    return encryptedData;
}
```

### 5. Audit Logging

**Security Event Logging**:
```java
@EventListener
public void handleAuthenticationFailure(AuthenticationFailureEvent event) {
    log.warn("Authentication failed for user: {}, IP: {}, Reason: {}", 
        event.getAuthentication().getName(), 
        getClientIpAddress(), 
        event.getException().getMessage());
}
```

## Security Testing

### 1. Security Test Suite

**Rate Limiting Tests**:
```java
@Test
void testRateLimiting() {
    // Send 101 requests rapidly
    for (int i = 0; i < 101; i++) {
        mockMvc.perform(get("/api/v1/users"))
            .andExpect(status().is(i < 100 ? 200 : 429));
    }
}
```

**CAPTCHA Tests**:
```java
@Test
void testCaptchaValidation() {
    // Test with valid CAPTCHA
    mockMvc.perform(post("/api/v1/users")
        .param("captchaToken", "valid-token"))
        .andExpect(status().isOk());
    
    // Test with invalid CAPTCHA
    mockMvc.perform(post("/api/v1/users")
        .param("captchaToken", "invalid-token"))
        .andExpect(status().isBadRequest());
}
```

### 2. Security Scanning

**OWASP Dependency Check**:
```bash
mvn org.owasp:dependency-check-maven:check
```

**Trivy Security Scan**:
```bash
trivy image --exit-code 1 --severity HIGH,CRITICAL telco/user-service:latest
```

## Deployment Security

### 1. Production Checklist

- [ ] All secrets stored in environment variables
- [ ] SSL/TLS enabled
- [ ] Rate limiting configured
- [ ] CAPTCHA enabled
- [ ] RLS policies active
- [ ] Security headers configured
- [ ] CORS properly configured
- [ ] Audit logging enabled
- [ ] Monitoring and alerting setup

### 2. Environment Security

**Development**:
- Mock external services
- Disable CAPTCHA
- Relaxed CORS
- Debug logging enabled

**Staging**:
- Production-like security
- CAPTCHA enabled
- Restricted CORS
- Security testing

**Production**:
- Full security enabled
- SSL/TLS required
- Strict CORS
- Audit logging
- Monitoring

## Incident Response

### 1. Security Incident Procedure

1. **Detect**: Monitor logs and alerts
2. **Assess**: Determine severity and impact
3. **Contain**: Isolate affected systems
4. **Eradicate**: Remove threats
5. **Recover**: Restore normal operations
6. **Learn**: Post-incident review

### 2. Monitoring and Alerting

**Security Metrics**:
- Failed authentication attempts
- Rate limit violations
- CAPTCHA failures
- Unusual access patterns
- Database query anomalies

**Alert Thresholds**:
- 10 failed logins per minute
- 1000 requests per minute from single IP
- 50% CAPTCHA failure rate
- Unusual data access patterns

## Compliance

### 1. Data Protection

- **GDPR**: User data privacy and right to deletion
- **CCPA**: California Consumer Privacy Act compliance
- **SOC 2**: Security and availability controls

### 2. Security Standards

- **OWASP Top 10**: Protection against common vulnerabilities
- **NIST Cybersecurity Framework**: Risk management
- **ISO 27001**: Information security management

## Support

For security issues:
- **Security Team**: security@telco.com
- **Incident Response**: incident@telco.com
- **Emergency**: +1-800-SECURITY

## Updates

This security guide is regularly updated. Last updated: $(date)
