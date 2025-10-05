package com.finmodel;

import lombok.Builder;
import lombok.Value;

import java.util.Date;
import java.util.Map;

@Value
@Builder
public class EntityVersion {
    Entity parent;
    Date date;
    int sequence;
    double balance;
    double rate;
    Map<String, Object> attributes;
    EntityVersion previous;

    public EntityVersion applyEvent(Event event) {
        // Stub implementation for Phase 1 - return this
        // In later phases, apply event logic
        return this;
    }
}