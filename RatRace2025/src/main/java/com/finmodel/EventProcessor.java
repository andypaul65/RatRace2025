package com.finmodel;

import java.util.List;

public interface EventProcessor {
    EntityVersion process(Event event, EntityVersion version);
    List<Flow> handleFlows(Event event);
    List<Entity> handleCreation(Event event);
}