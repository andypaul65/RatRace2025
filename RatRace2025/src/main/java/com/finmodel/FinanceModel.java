package com.finmodel;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceModel {
    private Scenario scenario;
    private Timeline timeline;
    private Set<Entity> dynamicEntities;
    private List<ScenarioComponent> components;

    public void loadFromJson(String file) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            this.scenario = mapper.readValue(new File(file), Scenario.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load from JSON", e);
        }
    }

    public void saveToJson(String file) {
        if (scenario == null) {
            return;
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new File(file), scenario);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save to JSON", e);
        }
    }

    public void runSimulation() throws SimulationException {
        if (scenario == null || timeline == null) {
            return;
        }

        // Incorporate components into scenario if present
        if (components != null && !components.isEmpty()) {
            incorporateComponents();
        }

        // Initialize timeline if not done
        if (timeline.getPeriods() == null || timeline.getPeriods().isEmpty()) {
            scenario.initialize(timeline);
        }
        // Create simulator
        Simulator simulator = Simulator.builder()
                .scenario(scenario)
                .processor(new DefaultEventProcessor())
                .timeline(timeline)
                .dynamicEntities(dynamicEntities)
                .build();
        // Set in timeline
        timeline.setSimulator(simulator);
        // Run
        try {
            simulator.playOut();
        } catch (SimulationException e) {
            // Re-throw to fail the scenario immediately
            throw e;
        }
    }

    private void incorporateComponents() throws ValidationException {
        if (scenario.getInitialEntities() == null) {
            scenario.setInitialEntities(new ArrayList<>());
        }
        if (scenario.getEventTemplates() == null) {
            scenario.setEventTemplates(new HashMap<>());
        }

        for (ScenarioComponent component : components) {
            // Validate component
            component.validate();

            // Add entities from component
            for (Entity entity : component.getEntities()) {
                scenario.getInitialEntities().add(entity);
            }

            // Add events from component with proper entity association
            Map<String, Entity> entityMap = new HashMap<>();
            String componentId = component.getId();

            // Create a map of entity IDs for this component
            for (Entity entity : component.getEntities()) {
                entityMap.put(entity.getId(), entity);
            }

            // Associate events with appropriate entities based on event ID patterns
            for (Event event : component.getEvents()) {
                Entity targetEntity = null;
                String eventId = event.getId();
                System.out.println("DEBUG: Associating event " + eventId + " with component " + componentId);

                // Match event to entity based on ID patterns
                if (eventId.contains("_mortgage")) {
                    targetEntity = entityMap.get(componentId + "_mortgage");
                    System.out.println("DEBUG: Matched mortgage event to entity: " + (targetEntity != null ? targetEntity.getId() : "null"));
                } else if (eventId.contains("_rent")) {
                    targetEntity = entityMap.get(componentId + "_rent_income");
                    System.out.println("DEBUG: Matched rent event to entity: " + (targetEntity != null ? targetEntity.getId() : "null"));
                } else if (eventId.contains("_ancillary_expenses_event")) {
                    targetEntity = entityMap.get(componentId + "_ancillary_expenses");
                    System.out.println("DEBUG: Matched ancillary expenses event to entity: " + (targetEntity != null ? targetEntity.getId() : "null"));
                } else if (eventId.contains("_tax")) {
                    targetEntity = entityMap.get(componentId + "_property_tax");
                    System.out.println("DEBUG: Matched tax event to entity: " + (targetEntity != null ? targetEntity.getId() : "null"));
                } else if (eventId.contains("_insurance")) {
                    targetEntity = entityMap.get(componentId + "_insurance");
                    System.out.println("DEBUG: Matched insurance event to entity: " + (targetEntity != null ? targetEntity.getId() : "null"));
                } else if (eventId.contains("_contribution")) {
                    targetEntity = entityMap.get(componentId + "_contributions");
                    System.out.println("DEBUG: Matched contribution event to entity: " + (targetEntity != null ? targetEntity.getId() : "null"));
                } else if (eventId.contains("_returns")) {
                    targetEntity = entityMap.get(componentId + "_account");
                    System.out.println("DEBUG: Matched returns event to account entity: " + (targetEntity != null ? targetEntity.getId() : "null"));
                } else {
                    // Default to property entity for appreciation and other events, or account for investments
                    targetEntity = entityMap.get(componentId + "_property");
                    if (targetEntity == null) {
                        targetEntity = entityMap.get(componentId + "_account");
                    }
                    System.out.println("DEBUG: Defaulted event to entity: " + (targetEntity != null ? targetEntity.getId() : "null"));
                }

                // If we found a matching entity, associate the event with it
                if (targetEntity != null) {
                    scenario.getEventTemplates()
                            .computeIfAbsent(targetEntity, k -> new ArrayList<>())
                            .add(event);
                    System.out.println("DEBUG: Associated event " + eventId + " with entity " + targetEntity.getId());
                } else {
                    // Fallback to first entity if no match found
                    Entity fallbackEntity = component.getEntities().get(0);
                    scenario.getEventTemplates()
                            .computeIfAbsent(fallbackEntity, k -> new ArrayList<>())
                            .add(event);
                    System.out.println("DEBUG: Fallback - associated event " + eventId + " with first entity " + fallbackEntity.getId());
                }
            }
        }
    }

    public Map<String, Object> buildSankeyData() {
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> nodes = new java.util.ArrayList<>();
        List<Map<String, Object>> links = new java.util.ArrayList<>();
        List<Map<String, Object>> periods = new java.util.ArrayList<>();

        // Calculate common scale (maximum balance across all periods and entities)
        double maxBalance = calculateMaxBalance();

        if (scenario != null && timeline != null && timeline.getPeriods() != null) {

            // Process each period for date-based columns
            for (int periodIndex = 0; periodIndex < timeline.getPeriods().size(); periodIndex++) {
                TimePeriod period = timeline.getPeriods().get(periodIndex);
                String periodId = "period_" + periodIndex;

                // Add period metadata
                Map<String, Object> periodData = new HashMap<>();
                periodData.put("id", periodId);
                periodData.put("index", periodIndex);
                periodData.put("startDate", period.getStart());
                periodData.put("endDate", period.getEnd());
                periodData.put("riskFreeRate", period.getRiskFreeRate());
                periodData.put("inflation", period.getInflation());
                periods.add(periodData);

                // Process asset groups first (higher level)
                if (scenario.getAssetGroups() != null) {
                    for (AssetGroup group : scenario.getAssetGroups()) {
                        processAssetGroupForSankey(group, period, periodId, nodes, links, maxBalance, periodIndex);
                    }
                }

                // Process individual entities
                if (scenario.getInitialEntities() != null) {
                    for (Entity entity : scenario.getInitialEntities()) {
                        processEntityForSankey(entity, period, periodId, nodes, links, maxBalance, periodIndex);
                    }
                }

                // Process dynamic entities
                if (dynamicEntities != null) {
                    for (Entity entity : dynamicEntities) {
                        processEntityForSankey(entity, period, periodId, nodes, links, maxBalance, periodIndex);
                    }
                }
            }
        }

        data.put("nodes", nodes);
        data.put("links", links);
        data.put("periods", periods);
        data.put("maxBalance", maxBalance);
        data.put("totalPeriods", timeline != null && timeline.getPeriods() != null ? timeline.getPeriods().size() : 0);
        return data;
    }

    private double calculateMaxBalance() {
        double maxBalance = 0.0;
        if (timeline != null && timeline.getPeriods() != null) {
            for (TimePeriod period : timeline.getPeriods()) {
                if (scenario.getInitialEntities() != null) {
                    for (Entity entity : scenario.getInitialEntities()) {
                        PeriodEntityAggregate agg = period.getPeriodEntityAggregate(entity);
                        if (agg != null) {
                            maxBalance = Math.max(maxBalance, Math.abs(agg.getNetBalance()));
                        }
                    }
                }
                if (dynamicEntities != null) {
                    for (Entity entity : dynamicEntities) {
                        PeriodEntityAggregate agg = period.getPeriodEntityAggregate(entity);
                        if (agg != null) {
                            maxBalance = Math.max(maxBalance, Math.abs(agg.getNetBalance()));
                        }
                    }
                }
            }
        }
        return maxBalance;
    }

    private void processAssetGroupForSankey(AssetGroup group, TimePeriod period, String periodId,
                                          List<Map<String, Object>> nodes, List<Map<String, Object>> links,
                                          double maxBalance, int periodIndex) {
        // Calculate current group balance (sum of all entities in group)
        double groupBalance = 0.0;
        List<Entity> allEntities = group.getAllEntities();
        for (Entity entity : allEntities) {
            PeriodEntityAggregate agg = period.getPeriodEntityAggregate(entity);
            if (agg != null) {
                groupBalance += agg.getNetBalance();
            }
        }

        // Add group node
        Map<String, Object> groupNode = group.toSankeyNode(groupBalance, periodId);
        groupNode.put("periodIndex", periodIndex);
        groupNode.put("normalizedHeight", maxBalance > 0 ? Math.abs(groupBalance) / maxBalance : 0);
        nodes.add(groupNode);
    }

    private void processEntityForSankey(Entity entity, TimePeriod period, String periodId,
                                      List<Map<String, Object>> nodes, List<Map<String, Object>> links,
                                      double maxBalance, int periodIndex) {
        PeriodEntityAggregate agg = period.getPeriodEntityAggregate(entity);
        if (agg != null && agg.getNetBalance() != 0) {
            // Add entity node
            Map<String, Object> node = agg.toSankeyNode(periodId);
            node.put("periodIndex", periodIndex);
            node.put("normalizedHeight", maxBalance > 0 ? Math.abs(agg.getNetBalance()) / maxBalance : 0);

            // Add ROI metrics for investment entities if we have multiple periods
            if (timeline.getPeriods().size() > 1 && isInvestmentEntity(entity)) {
                Map<String, Object> roiMetrics = calculateEntityROI(entity);
                if (!roiMetrics.isEmpty()) {
                    node.put("roiMetrics", roiMetrics);
                }
            }

            nodes.add(node);

            // Process flows for this entity
            if (agg.netIntraFlows() != null) {
                for (Flow flow : agg.netIntraFlows()) {
                    Map<String, Object> link = flow.toSankeyLink(periodId);
                    links.add(link);
                }
            }
            if (agg.interFlows() != null) {
                for (Flow flow : agg.interFlows()) {
                    Map<String, Object> link = flow.toSankeyLink(periodId);
                    links.add(link);
                }
            }
        }
    }

    private boolean isInvestmentEntity(Entity entity) {
        return "Asset".equals(entity.getPrimaryCategory()) &&
               ("Investment".equals(entity.getDetailedCategory()) ||
                "Cryptocurrency Asset".equals(entity.getDetailedCategory()) ||
                "Equity Investment".equals(entity.getDetailedCategory()) ||
                "Derivative Investment".equals(entity.getDetailedCategory()));
    }

    private Map<String, Object> calculateEntityROI(Entity entity) {
        Map<String, Object> roiData = new HashMap<>();

        if (timeline.getPeriods().size() < 2) {
            return roiData;
        }

        TimePeriod firstPeriod = timeline.getPeriods().get(0);
        TimePeriod finalPeriod = timeline.getPeriods().get(timeline.getPeriods().size() - 1);

        PeriodEntityAggregate firstAgg = firstPeriod.getPeriodEntityAggregate(entity);
        PeriodEntityAggregate finalAgg = finalPeriod.getPeriodEntityAggregate(entity);

        if (firstAgg != null && finalAgg != null) {
            double initialValue = firstAgg.getNetBalance();
            double finalValue = finalAgg.getNetBalance();

            if (initialValue > 0) {
                double totalReturn = finalValue - initialValue;
                double roi = (totalReturn / initialValue) * 100.0;

                int totalPeriods = timeline.getPeriods().size();
                double yearsElapsed = totalPeriods / 12.0; // Assuming monthly periods

                double annualizedROI = Math.pow(1 + (roi / 100.0), 1.0 / yearsElapsed) - 1;
                annualizedROI *= 100.0;

                roiData.put("initialValue", initialValue);
                roiData.put("finalValue", finalValue);
                roiData.put("totalROI", roi);
                roiData.put("annualizedROI", annualizedROI);
                roiData.put("timeframeYears", yearsElapsed);
            }
        }

        return roiData;
    }

    public void dumpToConsole() {
        System.out.println("Finance Model Dump");
        if (scenario == null || timeline == null || timeline.getPeriods() == null) {
            System.out.println("No data to dump");
            return;
        }

        // Calculate date range
        java.util.Date startDate = null;
        java.util.Date endDate = null;
        if (!timeline.getPeriods().isEmpty()) {
            startDate = timeline.getPeriods().get(0).getStart();
            endDate = timeline.getPeriods().get(timeline.getPeriods().size() - 1).getEnd();
        }

        System.out.printf("Timeline: %s to %s%n",
                startDate != null ? startDate.toString() : "N/A",
                endDate != null ? endDate.toString() : "N/A");
        System.out.printf("Total Entities: %d | Total Events: %d | Total Periods: %d%n",
                scenario.getInitialEntities() != null ? scenario.getInitialEntities().size() : 0,
                countTotalEvents(),
                timeline.getPeriods().size());

        // Component information
        if (components != null && !components.isEmpty()) {
            System.out.println("\n=== COMPONENTS ===");
            for (ScenarioComponent component : components) {
                System.out.println(component.describe());
            }
        }

        // Investment ROI Analysis
        if (timeline.getPeriods().size() > 1) {
            System.out.println("\n=== INVESTMENT ROI ANALYSIS ===");
            generateROIReport();
        }

        // Summary for the final period
        if (!timeline.getPeriods().isEmpty()) {
            TimePeriod finalPeriod = timeline.getPeriods().get(timeline.getPeriods().size() - 1);
            System.out.println("\n=== PERIOD SUMMARY (Final Period) ===");

            double totalAssets = 0.0;
            double totalLiabilities = 0.0;
            double totalIncome = 0.0;
            double totalExpenses = 0.0;

            if (scenario.getInitialEntities() != null) {
                System.out.println("Entity Details:");
                for (Entity entity : scenario.getInitialEntities()) {
                    PeriodEntityAggregate agg = finalPeriod.getPeriodEntityAggregate(entity);
                    if (agg != null) {
                        double balance = agg.getNetBalance();
                        System.out.printf("  - %s: $%.0f", entity.getId(), balance);

                        // Show changes if we have multiple periods
                        if (timeline.getPeriods().size() > 1) {
                            TimePeriod firstPeriod = timeline.getPeriods().get(0);
                            PeriodEntityAggregate firstAgg = firstPeriod.getPeriodEntityAggregate(entity);
                            if (firstAgg != null) {
                                double initialBalance = firstAgg.getNetBalance();
                                double change = balance - initialBalance;
                                System.out.printf(" (%.0f from initial $%.0f)", change, initialBalance);
                            }
                        }
                        System.out.println();

                        // Categorize for summary
                        String category = entity.getPrimaryCategory();
                        if ("Asset".equals(category)) {
                            totalAssets += balance;
                        } else if ("Liability".equals(category)) {
                            totalLiabilities += Math.abs(balance); // Liabilities are typically negative
                        } else if ("Income".equals(category)) {
                            totalIncome += balance;
                        } else if ("Expense".equals(category)) {
                            totalExpenses += Math.abs(balance); // Expenses are typically negative
                        }
                    }
                }
            }

            System.out.printf("Assets: $%.0f%n", totalAssets);
            System.out.printf("Liabilities: $%.0f%n", totalLiabilities);
            System.out.printf("Net Worth: $%.0f%n", totalAssets - totalLiabilities);

            // Cash flow analysis if we have income/expense data
            if (totalIncome > 0 || totalExpenses > 0) {
                System.out.println("\nCash Flow Summary:");
                System.out.printf("Income: $%.0f%n", totalIncome);
                System.out.printf("Expenses: $%.0f%n", totalExpenses);
                System.out.printf("Net Cash Flow: $%.0f%n", totalIncome - totalExpenses);
            }
        }

        // Simple Sankey ASCII representation
        System.out.println("\nSankey ASCII View:");
        System.out.println("[Periods] --> [Entities] --> [Flows]");
        System.out.println("Timeline shows " + timeline.getPeriods().size() + " periods of financial activity");
    }

    private void generateROIReport() {
        if (scenario.getInitialEntities() == null || timeline.getPeriods().size() < 2) {
            return;
        }

        TimePeriod firstPeriod = timeline.getPeriods().get(0);
        TimePeriod finalPeriod = timeline.getPeriods().get(timeline.getPeriods().size() - 1);

        int totalPeriods = timeline.getPeriods().size();
        double yearsElapsed = totalPeriods / 12.0; // Assuming monthly periods

        System.out.println("Investment Performance Comparison:");
        System.out.println("Timeframe: " + yearsElapsed + " years");

        for (Entity entity : scenario.getInitialEntities()) {
            // Only analyze investment assets
            if (!"Asset".equals(entity.getPrimaryCategory()) ||
                (!"Investment".equals(entity.getDetailedCategory()) &&
                 !"Cryptocurrency Asset".equals(entity.getDetailedCategory()) &&
                 !"Equity Investment".equals(entity.getDetailedCategory()) &&
                 !"Derivative Investment".equals(entity.getDetailedCategory()))) {
                continue;
            }

            PeriodEntityAggregate firstAgg = firstPeriod.getPeriodEntityAggregate(entity);
            PeriodEntityAggregate finalAgg = finalPeriod.getPeriodEntityAggregate(entity);

            if (firstAgg != null && finalAgg != null) {
                double initialValue = firstAgg.getNetBalance();
                double finalValue = finalAgg.getNetBalance();

                if (initialValue > 0) {
                    double totalReturn = finalValue - initialValue;
                    double roi = (totalReturn / initialValue) * 100.0;
                    double annualizedROI = Math.pow(1 + (roi / 100.0), 1.0 / yearsElapsed) - 1;
                    annualizedROI *= 100.0;

                    System.out.printf("  %s: $%.0f → $%.0f | Total ROI: %.1f%% | Annualized: %.1f%%%n",
                            entity.getName(), initialValue, finalValue, roi, annualizedROI);
                }
            }
        }
    }

    private int countTotalEvents() {
        if (scenario == null || scenario.getEventTemplates() == null) {
            return 0;
        }

        return scenario.getEventTemplates().values().stream()
                .mapToInt(List::size)
                .sum();
    }

    public void addDynamicEntity(Entity entity) {
        if (dynamicEntities != null) {
            dynamicEntities.add(entity);
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: FinanceModel <scenario.json>");
            return;
        }
        String file = args[0];
        FinanceModel model = new FinanceModel();
        try {
            model.loadFromJson(file);
            model.runSimulation();
            model.dumpToConsole();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}