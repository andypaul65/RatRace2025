package com.finmodel.mvp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import org.ajp.mvp.server.ServiceRegistry;
import com.finmodel.AuditLog;

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
        // TODO: Register additional services as needed (e.g., custom tabs, controllers)
        // TODO: Ensure service namespace "ratrace" aligns with MVP conventions
        serviceRegistry.registerService("ratrace", ratRaceSystemStateService);

        // Log registration for debugging
        AuditLog.getInstance().log("Registered RatRaceSystemStateService with MVP ServiceRegistry");
    }
}