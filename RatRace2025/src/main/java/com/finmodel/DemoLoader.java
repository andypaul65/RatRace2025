package com.finmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DemoLoader {

    public static void main(String[] args) {
        System.out.println("RatRace2025 Demo: Monthly Income Simulation");
        System.out.println("=============================================");

        // Create demo scenario
        Entity savingsAccount = Entity.builder()
                .id("savings")
                .name("Savings Account")
                .baseProperties(Map.of("initialBalance", 0.0))
                .isTemplate(false)
                .build();

        // Monthly income event
        RecurringEvent monthlyIncome = RecurringEvent.builder()
                .id("monthly-deposit")
                .type("deposit")
                .params(Map.of("amount", 5000.0))
                .isRecurring(true)
                .conditionScript(null)
                .build();

        Scenario scenario = Scenario.builder()
                .initialEntities(new ArrayList<>(List.of(savingsAccount)))
                .latentEvents(List.of(monthlyIncome))
                .numPeriods(12) // 1 year
                .externals(List.of())
                .build();

        Timeline timeline = Timeline.builder().build();
        scenario.initialize(timeline); // Creates 12 periods

        // For demo, add the income event to each period to simulate monthly deposits
        for (TimePeriod period : timeline.getPeriods()) {
            period.addEvent(monthlyIncome);
        }

        // Create FinanceModel
        FinanceModel model = FinanceModel.builder()
                .scenario(scenario)
                .timeline(timeline)
                .dynamicEntities(new java.util.HashSet<>())
                .build();

        // Run simulation
        model.runSimulation();

        // Dump to console
        model.dumpToConsole();

        // Additional demo output
        System.out.println("\nDetailed Period Breakdown:");
        for (int i = 0; i < timeline.getPeriods().size(); i++) {
            TimePeriod period = timeline.getPeriods().get(i);
            System.out.printf("Month %2d: ", i + 1);
            for (Entity entity : scenario.getInitialEntities()) {
                PeriodEntityAggregate agg = period.getPeriodEntityAggregate(entity);
                if (agg != null) {
                    System.out.printf("%s Balance: $%.2f | ", entity.getName(), agg.getNetBalance());
                }
            }
            System.out.println();
        }

        // Get final balance
        TimePeriod lastPeriod = timeline.getPeriods().get(timeline.getPeriods().size() - 1);
        PeriodEntityAggregate finalAgg = lastPeriod.getPeriodEntityAggregate(scenario.getInitialEntities().get(0));
        double finalBalance = finalAgg != null ? finalAgg.getNetBalance() : 0.0;

        System.out.println("\nDemo completed. Account grew from $0.00 to $" + String.format("%.2f", finalBalance) +
                " over 12 months with $5,000 monthly deposits.");

        // Show audit log
        System.out.println("\nAudit Log:");
        AuditLog.getInstance().getAllLogs().forEach(System.out::println);
    }
}