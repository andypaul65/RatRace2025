package com.finmodel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.ajp.mvp.server.ServiceRegistry;

/**
 * Main Spring Boot application class for the RatRace2025 backend.
 * This provides a standalone entry point for development and testing.
 */
@SpringBootApplication
public class RatRaceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RatRaceApplication.class, args);
    }

    /**
     * Provide ServiceRegistry bean for MVP integration.
     * This is required for the MvpConfiguration to work.
     */
    @Bean
    public ServiceRegistry serviceRegistry() {
        return new ServiceRegistry();
    }
}