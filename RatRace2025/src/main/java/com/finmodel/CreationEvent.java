package com.finmodel;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Collections;
import java.util.List;

@SuperBuilder
@NoArgsConstructor
public class CreationEvent extends Event {

    @Override
    public EntityVersion apply(EntityVersion from) {
        return from; // No change
    }

    @Override
    public List<Flow> generateFlows() {
        return Collections.emptyList();
    }

    @Override
    public List<Entity> createEntities() {
        // Create a new entity
        return List.of(Entity.builder().id("dynamic-" + System.nanoTime()).name("Dynamic Entity").build());
    }
}