package com.finmodel;

import lombok.experimental.SuperBuilder;

import java.util.Collections;
import java.util.List;

@SuperBuilder
public class RecurringEvent extends Event {

    @Override
    public EntityVersion apply(EntityVersion from) {
        double amount = (double) getParams().getOrDefault("amount", 0.0);
        double newBalance = from.getBalance() + amount;
        return EntityVersion.builder()
                .parent(from.getParent())
                .date(from.getDate())
                .sequence(from.getSequence() + 1)
                .balance(newBalance)
                .rate(from.getRate())
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