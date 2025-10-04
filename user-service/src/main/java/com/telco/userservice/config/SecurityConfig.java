package com.telco.userservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private SecretManagerConfig.SecretManagerService secretManagerService;

    @Autowired
    private RateLimitingFilter rateLimitingFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints
                        .requestMatchers("/api/v1/actuator/health", "/api/v1/actuator/info").permitAll()
                        .requestMatchers("/api/v1/swagger-ui/**", "/api/v1/api-docs/**").permitAll()
                        .requestMatchers("/api/v1/users/public/**").permitAll()

                        // Rate limiting for all endpoints
                        .requestMatchers("/api/v1/**").authenticated()

                        // Admin endpoints
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // All other requests need authentication
                        .anyRequest().authenticated())
                .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        if (secretManagerService.isDevelopment()) {
            // Allow all origins in development
            configuration.setAllowedOriginPatterns(Arrays.asList("*"));
            configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            configuration.setAllowedHeaders(Arrays.asList("*"));
            configuration.setAllowCredentials(true);
        } else {
            // Restrictive CORS for production
            configuration.setAllowedOrigins(Arrays.asList(
                    "https://telco.com",
                    "https://www.telco.com",
                    "https://staging.telco.com"));
            configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
            configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
            configuration.setAllowCredentials(true);
        }

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/v1/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
