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
        return event.apply(this);
    }
}