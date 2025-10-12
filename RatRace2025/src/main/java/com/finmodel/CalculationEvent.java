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
        // Check if this is an investment returns calculation
        if ("investment_returns".equals(getType())) {
            return applyInvestmentReturns(from);
        }

        // Default behavior: calculate new rate based on balance
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

    private EntityVersion applyInvestmentReturns(EntityVersion from) {
        double currentBalance = from.getBalance();
        double expectedReturn = (Double) getParams().getOrDefault("expectedReturn", 0.07);
        double volatility = (Double) getParams().getOrDefault("volatility", 0.15);
        boolean inflationAffected = (Boolean) getParams().getOrDefault("inflationAffected", true);
        double inflationRate = (Double) getParams().getOrDefault("inflationRate", 0.02);

        // Calculate base return (annualized)
        double baseReturn = currentBalance * expectedReturn;

        // Add some randomness based on volatility (simplified)
        double randomFactor = 1.0 + (Math.random() - 0.5) * volatility;
        double adjustedReturn = baseReturn * randomFactor;

        // Apply inflation adjustment for inflation-affected investments
        double finalReturn;
        if (inflationAffected) {
            // Real return = nominal return - inflation
            finalReturn = adjustedReturn - (currentBalance * inflationRate);
        } else {
            // Crypto and similar assets: no inflation adjustment
            finalReturn = adjustedReturn;
        }

        // Calculate new balance and rate
        double newBalance = currentBalance + finalReturn;
        double newRate = finalReturn / currentBalance; // Rate of return for this period

        return EntityVersion.builder()
                .parent(from.getParent())
                .date(from.getDate())
                .sequence(from.getSequence() + 1)
                .balance(newBalance)
                .rate(newRate)
                .attributes(from.getAttributes())
                .previous(from)
                .build();
    }

    @Override
    public List<Flow> generateFlows() {
        // For investment returns, create a flow representing the return
        if ("investment_returns".equals(getType())) {
            // This would create flows for investment returns
            // For now, return empty as the balance change is handled in apply()
            return Collections.emptyList();
        }

        return Collections.emptyList();
    }

    @Override
    public List<Entity> createEntities() {
        // Stub: return empty list
        return Collections.emptyList();
    }
}