package com.telco.alerttriggerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AlertTriggerServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AlertTriggerServiceApplication.class, args);
    }
}
