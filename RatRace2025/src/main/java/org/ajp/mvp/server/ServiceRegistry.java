package org.ajp.mvp.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stub implementation of ServiceRegistry for development.
 */
public class ServiceRegistry {
    private final Map<String, Object> services = new HashMap<>();

    public void registerService(String serviceName, Object service) {
        services.put(serviceName, service);
    }

    public void unregisterService(String serviceName) {
        services.remove(serviceName);
    }

    @SuppressWarnings("unchecked")
    public <T> T getService(String serviceName, Class<T> serviceClass) {
        return (T) services.get(serviceName);
    }

    public List<String> getRegisteredServiceNames() {
        return List.copyOf(services.keySet());
    }
}