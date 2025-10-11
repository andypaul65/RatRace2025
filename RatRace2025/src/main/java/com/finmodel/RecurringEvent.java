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
        double currentBalance = from.getBalance();
        String entityName = from.getParent().getName();

        // Check for insufficient funds based on entity type and transaction
        String category = from.getParent().getPrimaryCategory();

        if ("Asset".equals(category) || "Income".equals(category)) {
            // For assets and income: prevent negative balances from withdrawals
            if (amount < 0 && currentBalance + amount < 0) {
                throw new SimulationException(
                    String.format("Insufficient funds for %s: balance $%.2f, required $%.2f",
                        entityName, currentBalance, Math.abs(amount))
                );
            }
        } else if ("Liability".equals(category) || "Expense".equals(category)) {
            // For liabilities and expenses: allow positive additions (reductions) but prevent excessive negative amounts
            // This is a simplified check - in practice, liabilities have credit limits
            if (amount < 0 && Math.abs(currentBalance + amount) > Math.abs(currentBalance) * 2) {
                throw new SimulationException(
                    String.format("Excessive charge on %s: balance $%.2f, additional charge $%.2f",
                        entityName, currentBalance, Math.abs(amount))
                );
            }
        }

        double newBalance = currentBalance + amount;
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