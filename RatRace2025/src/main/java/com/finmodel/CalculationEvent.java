package com.finmodel;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Collections;
import java.util.List;

@SuperBuilder
@NoArgsConstructor
public class CalculationEvent extends Event {

    @Override
    public EntityVersion apply(EntityVersion from) {
        // Example: calculate new rate based on balance
        double newRate = from.getBalance() > 1000 ? 5.0 : 3.0;
        return EntityVersion.builder()
                .parent(from.getParent())
                .date(from.getDate())
                .sequence(from.getSequence() + 1)
                .balance(from.getBalance())
                .rate(newRate)
                .attributes(from.getAttributes())
                .previous(from)
                .build();
    }

    @Override
    public List<Flow> generateFlows() {
        // Stub: return empty list
        return Collections.emptyList();
    }

    @Override
    public List<Entity> createEntities() {
        // Stub: return empty list
        return Collections.emptyList();
    }
}