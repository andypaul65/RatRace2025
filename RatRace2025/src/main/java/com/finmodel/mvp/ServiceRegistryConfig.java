package com.finmodel.mvp;

import com.example.services.ServiceRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Spring configuration to provide the MVP ServiceRegistry bean.
 * Since the MVP JAR only provides the interface, we need to implement it.
 */
@Configuration
public class ServiceRegistryConfig {

    @Bean
    public ServiceRegistry serviceRegistry() {
        return new ServiceRegistryImpl();
    }

    /**
     * Simple in-memory implementation of ServiceRegistry.
     */
    private static class ServiceRegistryImpl implements ServiceRegistry {

        private final Map<String, Object> services = new HashMap<>();

        @Override
        public void registerService(String serviceName, Object service) {
            services.put(serviceName, service);
        }

        @Override
        public void unregisterService(String serviceName) {
            services.remove(serviceName);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getService(String serviceName, Class<T> serviceClass) {
            Object service = services.get(serviceName);
            if (service != null && serviceClass.isInstance(service)) {
                return (T) service;
            }
            return null;
        }

        @Override
        public List<String> getRegisteredServiceNames() {
            return services.keySet().stream().collect(Collectors.toList());
        }
    }
}