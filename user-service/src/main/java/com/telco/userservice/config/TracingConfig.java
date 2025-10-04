package com.telco.userservice.config;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class TracingConfig implements WebMvcConfigurer {

    @Autowired
    private Tracer tracer;

    @Bean
    public TracingInterceptor tracingInterceptor() {
        return new TracingInterceptor(tracer);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tracingInterceptor());
    }

    public static class TracingInterceptor implements org.springframework.web.servlet.HandlerInterceptor {

        private final Tracer tracer;

        public TracingInterceptor(Tracer tracer) {
            this.tracer = tracer;
        }

        @Override
        public boolean preHandle(HttpServletRequest request,
                HttpServletResponse response,
                Object handler) throws Exception {

            // Create span for each request
            Span span = tracer.nextSpan()
                    .name("http-request")
                    .tag("http.method", request.getMethod())
                    .tag("http.url", request.getRequestURL().toString())
                    .tag("http.user_agent", request.getHeader("User-Agent"))
                    .start();

            request.setAttribute("tracing.span", span);
            return true;
        }

        @Override
        public void afterCompletion(HttpServletRequest request,
                HttpServletResponse response,
                Object handler, Exception ex) throws Exception {

            Span span = tracer.currentSpan();
            if (span != null) {
                span.tag("http.status_code", String.valueOf(response.getStatus()));
                if (ex != null) {
                    span.tag("error", true);
                    span.tag("error.message", ex.getMessage());
                }
                span.end();
            }
        }
    }
}
