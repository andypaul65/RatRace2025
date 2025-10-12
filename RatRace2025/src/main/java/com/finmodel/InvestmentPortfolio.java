package com.finmodel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Scenario component representing an investment portfolio.
 * Supports different investment types (stocks, bonds, options, crypto) with varying return characteristics
 * and inflation handling.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestmentPortfolio implements ScenarioComponent {

    public enum InvestmentType {
        STOCKS("Equity Investment", true),           // Traditional stocks affected by inflation
        BONDS("Fixed Income Investment", true),      // Bonds affected by inflation
        OPTIONS("Derivative Investment", true),      // Options affected by inflation
        CRYPTO("Cryptocurrency Asset", false);       // Crypto NOT affected by inflation

        private final String category;
        private final boolean inflationAffected;

        InvestmentType(String category, boolean inflationAffected) {
            this.category = category;
            this.inflationAffected = inflationAffected;
        }

        public String getCategory() { return category; }
        public boolean isInflationAffected() { return inflationAffected; }
    }

    private String id;
    private String name;

    @Builder.Default
    private InvestmentType investmentType = InvestmentType.STOCKS;

    @Builder.Default
    private double initialValue = 0.0;

    @Builder.Default
    private double expectedReturn = 0.07; // 7% annual return

    @Builder.Default
    private double monthlyContribution = 0.0;

    @Builder.Default
    private double volatility = 0.15; // 15% annual volatility

    @Builder.Default
    private double inflationAdjustment = 0.02; // 2% inflation rate for inflation-affected investments

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name != null ? name : investmentType.name() + " Portfolio " + id;
    }

    @Override
    public List<Entity> getEntities() {
        List<Entity> entities = new ArrayList<>();

        // Main investment account
        entities.add(Entity.builder()
                .id(id + "_account")
                .name(getName() + " Account")
                .primaryCategory("Asset")
                .detailedCategory(investmentType.getCategory())
                .initialValue(initialValue)
                .build());

        // Contribution income entity (if contributions exist)
        if (monthlyContribution > 0) {
            entities.add(Entity.builder()
                    .id(id + "_contributions")
                    .name(getName() + " Contributions")
                    .primaryCategory("Income")
                    .detailedCategory("Investment Contributions")
                    .initialValue(0.0)
                    .build());
        }

        return entities;
    }

    @Override
    public List<Event> getEvents() {
        List<Event> events = new ArrayList<>();
        List<Entity> entities = getEntities();

        // Find our entities
        Entity accountEntity = entities.stream()
                .filter(e -> e.getId().equals(id + "_account"))
                .findFirst().orElse(null);

        Entity contributionEntity = entities.stream()
                .filter(e -> e.getId().equals(id + "_contributions"))
                .findFirst().orElse(null);

        // Monthly contributions (if applicable)
        if (contributionEntity != null && monthlyContribution > 0) {
            events.add(RecurringEvent.builder()
                    .id(id + "_contribution")
                    .type("recurring")
                    .params(java.util.Map.of(
                            "amount", monthlyContribution,
                            "frequency", "MONTHLY"
                    ))
                    .isRecurring(true)
                    .build());
        }

        // Investment returns calculation event (annual)
        if (accountEntity != null && expectedReturn > 0) {
            events.add(CalculationEvent.builder()
                    .id(id + "_returns")
                    .type("investment_returns")
                    .params(java.util.Map.of(
                            "expectedReturn", expectedReturn,
                            "volatility", volatility,
                            "inflationAffected", investmentType.isInflationAffected(),
                            "inflationRate", inflationAdjustment
                    ))
                    .build());
        }

        return events;
    }

    @Override
    public void validate() throws ValidationException {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidationException("InvestmentPortfolio id cannot be null or empty");
        }

        if (initialValue < 0) {
            throw new ValidationException("Initial value cannot be negative for " + id);
        }

        if (expectedReturn < -0.5 || expectedReturn > 2.0) {
            throw new ValidationException("Expected return must be reasonable (-50% to 200%) for " + id);
        }

        if (monthlyContribution < 0) {
            throw new ValidationException("Monthly contribution cannot be negative for " + id);
        }

        if (volatility < 0 || volatility > 1.0) {
            throw new ValidationException("Volatility must be between 0 and 1 for " + id);
        }

        if (inflationAdjustment < -0.1 || inflationAdjustment > 0.1) {
            throw new ValidationException("Inflation adjustment must be reasonable (-10% to 10%) for " + id);
        }
    }

    @Override
    public String describe() {
        StringBuilder sb = new StringBuilder();
        sb.append(investmentType.name()).append(" Portfolio [").append(id).append("]: ");
        sb.append(String.format("$%.0f initial", initialValue));

        if (monthlyContribution > 0) {
            sb.append(String.format(", $%.0f/month contributions", monthlyContribution));
        }

        if (expectedReturn > 0) {
            sb.append(String.format(", %.1f%% expected return", expectedReturn * 100));
            if (volatility > 0) {
                sb.append(String.format(" (±%.1f%% volatility)", volatility * 100));
            }
        }

        if (investmentType.isInflationAffected()) {
            sb.append(", inflation-adjusted");
        } else {
            sb.append(", inflation-immune");
        }

        return sb.toString();
    }
}