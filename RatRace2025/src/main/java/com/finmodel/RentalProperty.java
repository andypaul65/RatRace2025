package com.finmodel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Scenario component representing a rental property investment.
 * Automatically creates property asset, mortgage liability, rental income, and associated expenses.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentalProperty implements ScenarioComponent {

    private String id;
    private String name;

    // Property details
    @Builder.Default
    private double propertyValue = 0.0;

    @Builder.Default
    private double appreciationRate = 0.03; // 3% annual appreciation

    // Mortgage details
    @Builder.Default
    private double mortgageAmount = 0.0;

    @Builder.Default
    private double mortgageRate = 0.045; // 4.5% annual rate

    @Builder.Default
    private int mortgageTermYears = 30;

    // Income details
    @Builder.Default
    private double monthlyRent = 0.0;

    @Builder.Default
    private double vacancyRate = 0.05; // 5% vacancy rate

    // Expense details
    @Builder.Default
    private double ancillaryCosts = 0.0; // Monthly maintenance/utilities

    @Builder.Default
    private double propertyTaxRate = 0.012; // 1.2% annual property tax

    @Builder.Default
    private double insuranceAnnual = 0.0; // Annual insurance cost

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name != null ? name : "Rental Property " + id;
    }

    @Override
    public List<Entity> getEntities() {
        List<Entity> entities = new ArrayList<>();

        // Property asset
        entities.add(Entity.builder()
                .id(id + "_property")
                .name(getName() + " Property")
                .primaryCategory("Asset")
                .detailedCategory("Real Estate")
                .initialValue(propertyValue)
                .build());

        // Mortgage liability (if applicable)
        if (mortgageAmount > 0) {
            entities.add(Entity.builder()
                    .id(id + "_mortgage")
                    .name(getName() + " Mortgage")
                    .primaryCategory("Liability")
                    .detailedCategory("Secured Debt")
                    .initialValue(-mortgageAmount) // Negative for liability
                    .build());
        }

        // Rental income entity
        entities.add(Entity.builder()
                .id(id + "_rent_income")
                .name(getName() + " Rental Income")
                .primaryCategory("Income")
                .detailedCategory("Rental Income")
                .initialValue(0.0)
                .build());

        // Ancillary expenses
        if (ancillaryCosts > 0) {
            entities.add(Entity.builder()
                    .id(id + "_ancillary_expenses")
                    .name(getName() + " Ancillary Expenses")
                    .primaryCategory("Expense")
                    .detailedCategory("Property Maintenance")
                    .initialValue(0.0)
                    .build());
        }

        // Property tax expense
        if (propertyTaxRate > 0) {
            entities.add(Entity.builder()
                    .id(id + "_property_tax")
                    .name(getName() + " Property Tax")
                    .primaryCategory("Expense")
                    .detailedCategory("Property Tax")
                    .initialValue(0.0)
                    .build());
        }

        // Insurance expense
        if (insuranceAnnual > 0) {
            entities.add(Entity.builder()
                    .id(id + "_insurance")
                    .name(getName() + " Insurance")
                    .primaryCategory("Expense")
                    .detailedCategory("Property Insurance")
                    .initialValue(0.0)
                    .build());
        }

        return entities;
    }

    @Override
    public List<Event> getEvents() {
        List<Event> events = new ArrayList<>();
        List<Entity> entities = getEntities();

        // Find our entities by ID for event creation
        Entity propertyEntity = entities.stream()
                .filter(e -> e.getId().equals(id + "_property"))
                .findFirst().orElse(null);

        Entity mortgageEntity = entities.stream()
                .filter(e -> e.getId().equals(id + "_mortgage"))
                .findFirst().orElse(null);

        Entity rentIncomeEntity = entities.stream()
                .filter(e -> e.getId().equals(id + "_rent_income"))
                .findFirst().orElse(null);

        Entity ancillaryExpenseEntity = entities.stream()
                .filter(e -> e.getId().equals(id + "_ancillary_expenses"))
                .findFirst().orElse(null);

        Entity propertyTaxEntity = entities.stream()
                .filter(e -> e.getId().equals(id + "_property_tax"))
                .findFirst().orElse(null);

        Entity insuranceEntity = entities.stream()
                .filter(e -> e.getId().equals(id + "_insurance"))
                .findFirst().orElse(null);

        // Property appreciation event (annual)
        if (propertyEntity != null && appreciationRate > 0) {
            events.add(RecurringEvent.builder()
                    .id(id + "_appreciation")
                    .type("recurring")
                    .params(java.util.Map.of(
                            "amount", propertyValue * appreciationRate, // Annual appreciation amount
                            "frequency", "ANNUAL"
                    ))
                    .isRecurring(true)
                    .build());
        }

        // Note: Mortgage payments should be applied from a checking/savings account
        // The component creates the mortgage entity but expects external payment setup
        // In a full implementation, this would be connected to a payment account

        // Rental income event (monthly, with vacancy adjustment)
        if (rentIncomeEntity != null && monthlyRent > 0) {
            double effectiveMonthlyRent = monthlyRent * (1.0 - vacancyRate);
            events.add(RecurringEvent.builder()
                    .id(id + "_rent_collection")
                    .type("recurring")
                    .params(java.util.Map.of(
                            "amount", effectiveMonthlyRent, // Positive for income
                            "frequency", "MONTHLY"
                    ))
                    .isRecurring(true)
                    .build());
        }

        // Ancillary expenses (monthly)
        if (ancillaryExpenseEntity != null && ancillaryCosts > 0) {
            events.add(RecurringEvent.builder()
                    .id(id + "_ancillary_expenses_event")
                    .type("recurring")
                    .params(java.util.Map.of(
                            "amount", -ancillaryCosts, // Negative for expense
                            "frequency", "MONTHLY"
                    ))
                    .isRecurring(true)
                    .build());
        }

        // Mortgage payment event (monthly) - simplified to just affect the mortgage balance
        // In reality, this would debit from a payment account
        if (mortgageEntity != null && mortgageAmount > 0) {
            double monthlyPayment = calculateMonthlyMortgagePayment();
            events.add(RecurringEvent.builder()
                    .id(id + "_mortgage_payment")
                    .type("recurring")
                    .params(java.util.Map.of(
                            "amount", monthlyPayment, // Positive to reduce mortgage balance (liability)
                            "frequency", "MONTHLY"
                    ))
                    .isRecurring(true)
                    .build());
        }

        // Property tax (annual)
        if (propertyTaxEntity != null && propertyTaxRate > 0) {
            double annualPropertyTax = propertyValue * propertyTaxRate;
            events.add(RecurringEvent.builder()
                    .id(id + "_property_tax")
                    .type("recurring")
                    .params(java.util.Map.of(
                            "amount", -annualPropertyTax, // Negative for expense
                            "frequency", "ANNUAL"
                    ))
                    .isRecurring(true)
                    .build());
        }

        // Insurance (annual)
        if (insuranceEntity != null && insuranceAnnual > 0) {
            events.add(RecurringEvent.builder()
                    .id(id + "_insurance")
                    .type("recurring")
                    .params(java.util.Map.of(
                            "amount", -insuranceAnnual, // Negative for expense
                            "frequency", "ANNUAL"
                    ))
                    .isRecurring(true)
                    .build());
        }

        return events;
    }

    @Override
    public void validate() throws ValidationException {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidationException("RentalProperty id cannot be null or empty");
        }

        if (propertyValue <= 0) {
            throw new ValidationException("Property value must be positive for " + id);
        }

        if (mortgageAmount > propertyValue) {
            throw new ValidationException("Mortgage amount cannot exceed property value for " + id);
        }

        if (mortgageRate < 0 || mortgageRate > 1.0) {
            throw new ValidationException("Mortgage rate must be between 0 and 1 (as decimal) for " + id);
        }

        if (appreciationRate < -0.5 || appreciationRate > 1.0) {
            throw new ValidationException("Appreciation rate must be reasonable (-50% to 100%) for " + id);
        }

        if (monthlyRent < 0) {
            throw new ValidationException("Monthly rent cannot be negative for " + id);
        }

        if (vacancyRate < 0 || vacancyRate > 1.0) {
            throw new ValidationException("Vacancy rate must be between 0 and 1 for " + id);
        }

        if (ancillaryCosts < 0) {
            throw new ValidationException("Ancillary costs cannot be negative for " + id);
        }

        if (propertyTaxRate < 0 || propertyTaxRate > 0.1) {
            throw new ValidationException("Property tax rate must be reasonable (0-10%) for " + id);
        }

        if (insuranceAnnual < 0) {
            throw new ValidationException("Insurance cost cannot be negative for " + id);
        }
    }

    @Override
    public String describe() {
        StringBuilder sb = new StringBuilder();
        sb.append("Rental Property [").append(id).append("]: ");
        sb.append(String.format("$%.0f property", propertyValue));

        if (mortgageAmount > 0) {
            sb.append(String.format(", $%.0f mortgage at %.1f%%",
                    mortgageAmount, mortgageRate * 100));
        }

        if (monthlyRent > 0) {
            sb.append(String.format(", $%.0f/month rent", monthlyRent));
            if (vacancyRate > 0) {
                sb.append(String.format(" (%.0f%% occupancy)", (1.0 - vacancyRate) * 100));
            }
        }

        if (appreciationRate > 0) {
            sb.append(String.format(", %.1f%% annual appreciation", appreciationRate * 100));
        }

        return sb.toString();
    }

    private double calculateMonthlyMortgagePayment() {
        if (mortgageAmount <= 0 || mortgageRate <= 0 || mortgageTermYears <= 0) {
            return 0.0;
        }

        double monthlyRate = mortgageRate / 12.0;
        int totalPayments = mortgageTermYears * 12;

        // Standard mortgage payment formula: P * [r(1+r)^n] / [(1+r)^n - 1]
        double payment = mortgageAmount *
                (monthlyRate * Math.pow(1 + monthlyRate, totalPayments)) /
                (Math.pow(1 + monthlyRate, totalPayments) - 1);

        return payment;
    }
}