package com.finmodel;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Collections;
import java.util.List;

@SuperBuilder
@NoArgsConstructor
public class RecurringEvent extends Event {

    @Override
    public EntityVersion apply(EntityVersion from) {
        double amount = (double) getParams().getOrDefault("amount", 0.0);

        // Check for insufficient funds on debit transactions (negative amounts)
        if (amount < 0 && from.getBalance() + amount < 0) {
            throw new SimulationException(
                String.format("Insufficient funds for %s: balance $%.2f, required $%.2f",
                    from.getParent().getName(), from.getBalance(), Math.abs(amount))
            );
        }

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