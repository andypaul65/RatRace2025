package com.finmodel;

import java.util.List;

/**
 * Demo showing how to use the component-based scenario system.
 */
public class ComponentDemo {

    public static void main(String[] args) {
        System.out.println("=== RatRace2025 Component-Based Scenario Demo ===\n");

        // Create a rental property component
        RentalProperty rentalProperty = RentalProperty.builder()
                .id("my_rental")
                .propertyValue(250000)
                .appreciationRate(0.03)
                .mortgageAmount(200000)
                .mortgageRate(0.045)
                .monthlyRent(2200)
                .ancillaryCosts(250)
                .vacancyRate(0.05)
                .build();

        // Validate the component
        try {
            rentalProperty.validate();
            System.out.println("✅ Component validation passed");
        } catch (ValidationException e) {
            System.out.println("❌ Component validation failed: " + e.getMessage());
            return;
        }

        // Display component description
        System.out.println("🏠 " + rentalProperty.describe());
        System.out.println();

        // Show entities created
        System.out.println("📊 Entities Created:");
        for (Entity entity : rentalProperty.getEntities()) {
            System.out.printf("  - %s (%s): $%.0f%n",
                    entity.getName(),
                    entity.getPrimaryCategory(),
                    entity.getInitialValue());
        }
        System.out.println();

        // Show events created
        System.out.println("⚡ Events Created:");
        for (Event event : rentalProperty.getEvents()) {
            String amount = "N/A";
            if (event instanceof RecurringEvent) {
                Object amt = ((RecurringEvent) event).getParams().get("amount");
                if (amt instanceof Number) {
                    amount = String.format("$%.0f", ((Number) amt).doubleValue());
                }
            }
            System.out.printf("  - %s: %s%n", event.getId(), amount);
        }
        System.out.println();

        // Create a scenario with the component
        FinanceModel model = FinanceModel.builder()
                .components(List.of(rentalProperty))
                .build();

        try {
            // This will fail due to insufficient funds for mortgage payments
            // since we didn't provide a funding account
            model.runSimulation();
            System.out.println("✅ Simulation completed successfully");

            // Show the dump
            System.out.println("📈 Simulation Results:");
            model.dumpToConsole();

        } catch (SimulationException e) {
            System.out.println("❌ Simulation failed as expected: " + e.getMessage());
            System.out.println("💡 This demonstrates immediate failure on business rule violations");
        }
    }
}