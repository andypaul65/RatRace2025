package org.ajp.mvp.server;

import org.springframework.stereotype.Service;

/**
 * Stub implementation of AbstractSystemStateService for development.
 * Replace with actual MVP server JAR when available.
 */
@Service
public abstract class AbstractSystemStateService implements SystemStateService {

    public abstract MessageDto processMessage(String namespace, MessageDto message);

    public MessageDto getDefaultState(String namespace) {
        return MessageDto.builder()
                .content("Default state")
                .namespace(namespace)
                .type("default_state")
                .build();
    }

    protected void storeMessage(String namespace, MessageDto message) {
        // Stub implementation - override in subclasses
    }
}