package com.finmodel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import org.ajp.mvp.server.ServiceRegistry;

/**
 * Spring configuration for MVP backplane integration.
 * Registers RatRace-specific services with the MVP ServiceRegistry.
 */
@Configuration
public class MvpConfiguration {

    @Autowired
    private ServiceRegistry serviceRegistry;

    @Autowired
    private RatRaceSystemStateService ratRaceSystemStateService;

    @PostConstruct
    public void registerServices() {
        // Register the RatRace system state service
        serviceRegistry.registerService("ratrace", ratRaceSystemStateService);

        // Log registration for debugging
        AuditLog.getInstance().log("Registered RatRaceSystemStateService with MVP ServiceRegistry");
    }
}