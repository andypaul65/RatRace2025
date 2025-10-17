package com.finmodel.mvp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import com.example.services.ServiceRegistry;
import com.finmodel.AuditLog;

/**
 * Spring configuration for MVP backplane integration.
 * Registers RatRace-specific services with the MVP ServiceRegistry.
 *
 * Follows MVP framework patterns for service registration and namespace isolation.
 */
@Configuration
public class MvpConfiguration {

    @Autowired
    private ServiceRegistry serviceRegistry;

    @Autowired
    private RatRaceSystemStateService ratRaceSystemStateService;

    @PostConstruct
    public void registerServices() {
        // Register RatRace financial modeling service with MVP backplane
        // Namespace "ratrace" provides isolation for financial modeling operations
        serviceRegistry.registerService("ratrace", ratRaceSystemStateService);

        // Log successful registration for monitoring and debugging
        AuditLog.getInstance().log("MVP Service Registration: RatRaceSystemStateService registered with namespace 'ratrace'");
    }
}