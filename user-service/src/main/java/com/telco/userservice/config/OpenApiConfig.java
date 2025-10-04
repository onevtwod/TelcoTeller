package com.telco.userservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class OpenApiConfig {

        @Value("${spring.application.name:user-service}")
        private String applicationName;

        @Value("${info.app.version:1.0.0}")
        private String applicationVersion;

        @Value("${info.app.environment:development}")
        private String environment;

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Telco User Service API")
                                                .description("## Telco User Service API Documentation\n\n" +
                                                                "This API provides comprehensive user management functionality for the Telco system, including:\n\n"
                                                                +
                                                                "- **User Management**: Create, read, update, and delete user records\n"
                                                                +
                                                                "- **Usage Tracking**: Monitor data usage and calculate usage percentages\n"
                                                                +
                                                                "- **Alert Integration**: Identify users above usage thresholds\n"
                                                                +
                                                                "- **Data Validation**: Comprehensive input validation and error handling\n\n"
                                                                +
                                                                "### Key Features\n" +
                                                                "- RESTful API design with proper HTTP status codes\n" +
                                                                "- Comprehensive input validation\n" +
                                                                "- Global exception handling\n" +
                                                                "- OpenAPI 3.0 documentation\n" +
                                                                "- Health check endpoints\n" +
                                                                "- Metrics and monitoring support\n\n" +
                                                                "### Authentication\n" +
                                                                "Currently, this API does not require authentication for development purposes.\n"
                                                                +
                                                                "In production, implement proper authentication and authorization.\n\n"
                                                                +
                                                                "### Rate Limiting\n" +
                                                                "API requests are rate-limited to prevent abuse. Please respect the rate limits.\n\n"
                                                                +
                                                                "### Support\n" +
                                                                "For support and questions, please contact the development team.")
                                                .version(applicationVersion)
                                                .contact(new Contact()
                                                                .name("Telco Development Team")
                                                                .email("dev@telco.com")
                                                                .url("https://telco.com/contact"))
                                                .license(new License()
                                                                .name("MIT License")
                                                                .url("https://opensource.org/licenses/MIT")))
                                .servers(Arrays.asList(
                                                new Server()
                                                                .url("http://localhost:8081/api/v1")
                                                                .description("Development Server"),
                                                new Server()
                                                                .url("https://staging-api.telco.com/api/v1")
                                                                .description("Staging Server"),
                                                new Server()
                                                                .url("https://api.telco.com/api/v1")
                                                                .description("Production Server")));
        }
}
