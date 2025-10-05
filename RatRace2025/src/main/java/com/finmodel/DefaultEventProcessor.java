package com.finmodel;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
public class DefaultEventProcessor implements EventProcessor {

    @Override
    public EntityVersion process(Event event, EntityVersion version) {
        return event.apply(version);
    }

    @Override
    public List<Flow> handleFlows(Event event) {
        return event.generateFlows();
    }

    @Override
    public List<Entity> handleCreation(Event event) {
        return event.createEntities();
    }
}