package com.finmodel;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Collections;
import java.util.List;

@SuperBuilder
@NoArgsConstructor
public class ConditionalEvent extends Event {

    @Override
    public EntityVersion apply(EntityVersion from) {
        // Stub condition check: if conditionScript is set and balance > 50
        boolean conditionMet = getConditionScript() != null &&
                (getConditionScript().contains("balance > 50") && from.getBalance() > 50.0);
        if (conditionMet) {
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
        return from; // No change if condition not met
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