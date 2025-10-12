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

                // Add period metadata with detailed context information
                Map<String, Object> periodData = new HashMap<>();
                periodData.put("id", periodId);
                periodData.put("index", periodIndex);
                periodData.put("startDate", period.getStart());
                periodData.put("endDate", period.getEnd());
                periodData.put("riskFreeRate", period.getRiskFreeRate());
                periodData.put("inflation", period.getInflation());

                // Add period summary statistics
                Map<String, Object> periodSummary = calculatePeriodSummary(period, periodIndex);
                periodData.put("summary", periodSummary);

                // Add investment performance for this period
                if (periodIndex > 0) {
                    Map<String, Object> investmentSummary = generatePeriodInvestmentSummary(periodIndex);
                    periodData.put("investmentSummary", investmentSummary);
                }

                // Add key metrics for quick access
                periodData.put("totalAssets", periodSummary.get("totalAssets"));
                periodData.put("totalLiabilities", periodSummary.get("totalLiabilities"));
                periodData.put("netWorth", periodSummary.get("netWorth"));
                periodData.put("netCashFlow", periodSummary.get("netCashFlow"));

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

        // Period-by-period summary for detailed analysis
        if (timeline.getPeriods().size() > 1) {
            System.out.println("\n=== PERIOD-BY-PERIOD ANALYSIS ===");
            System.out.println("Use getPeriodDetails(index) for detailed period information");
            System.out.println("Use comparePeriods(index1, index2) for period comparisons");
            System.out.println();

            System.out.println("Period Overview:");
            for (int i = 0; i < Math.min(timeline.getPeriods().size(), 10); i++) { // Show first 10 periods
                TimePeriod period = timeline.getPeriods().get(i);
                Map<String, Object> summary = calculatePeriodSummary(period, i);

                System.out.printf("Period %d: Assets=$%.0f, Net Worth=$%.0f, Cash Flow=$%.0f, Inflation=%.1f%%%n",
                    i,
                    summary.get("totalAssets"),
                    summary.get("netWorth"),
                    summary.get("netCashFlow"),
                    period.getInflation() * 100);
            }

            if (timeline.getPeriods().size() > 10) {
                System.out.println("... (" + (timeline.getPeriods().size() - 10) + " more periods)");
            }
        }

        // Simple Sankey ASCII representation
        System.out.println("\nSankey ASCII View:");
        System.out.println("[Periods] --> [Entities] --> [Flows]");
        System.out.println("Timeline shows " + timeline.getPeriods().size() + " periods of financial activity");
        System.out.println("Use buildSankeyData() for complete visualization data with period details");
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

    /**
     * Get detailed information for a specific time period for UI context pane
     */
    public Map<String, Object> getPeriodDetails(int periodIndex) {
        Map<String, Object> periodDetails = new HashMap<>();

        if (timeline == null || timeline.getPeriods() == null ||
            periodIndex < 0 || periodIndex >= timeline.getPeriods().size()) {
            periodDetails.put("error", "Invalid period index: " + periodIndex);
            return periodDetails;
        }

        TimePeriod period = timeline.getPeriods().get(periodIndex);

        // Basic period information
        periodDetails.put("periodIndex", periodIndex);
        periodDetails.put("startDate", period.getStart());
        periodDetails.put("endDate", period.getEnd());
        periodDetails.put("inflation", period.getInflation());
        periodDetails.put("riskFreeRate", period.getRiskFreeRate());

        // Entity balances and changes
        List<Map<String, Object>> entityBalances = new ArrayList<>();
        Map<String, Object> summary = new HashMap<>();
        double totalAssets = 0.0;
        double totalLiabilities = 0.0;
        double totalIncome = 0.0;
        double totalExpenses = 0.0;

        if (scenario != null && scenario.getInitialEntities() != null) {
            for (Entity entity : scenario.getInitialEntities()) {
                PeriodEntityAggregate agg = period.getPeriodEntityAggregate(entity);
                if (agg != null) {
                    Map<String, Object> entityInfo = new HashMap<>();
                    entityInfo.put("id", entity.getId());
                    entityInfo.put("name", entity.getName());
                    entityInfo.put("category", entity.getPrimaryCategory());
                    entityInfo.put("detailedCategory", entity.getDetailedCategory());
                    entityInfo.put("balance", agg.getNetBalance());

                    // Calculate change from previous period
                    if (periodIndex > 0) {
                        TimePeriod prevPeriod = timeline.getPeriods().get(periodIndex - 1);
                        PeriodEntityAggregate prevAgg = prevPeriod.getPeriodEntityAggregate(entity);
                        if (prevAgg != null) {
                            double change = agg.getNetBalance() - prevAgg.getNetBalance();
                            entityInfo.put("change", change);
                            entityInfo.put("changePercent", prevAgg.getNetBalance() != 0 ?
                                (change / prevAgg.getNetBalance()) * 100.0 : 0.0);
                        }
                    }

                    // Add ROI metrics for investment entities
                    if (isInvestmentEntity(entity)) {
                        Map<String, Object> periodRoi = calculatePeriodROI(entity, periodIndex);
                        if (!periodRoi.isEmpty()) {
                            entityInfo.put("periodROI", periodRoi);
                        }
                    }

                    entityBalances.add(entityInfo);

                    // Update summary totals
                    double balance = agg.getNetBalance();
                    String category = entity.getPrimaryCategory();
                    if ("Asset".equals(category)) {
                        totalAssets += balance;
                    } else if ("Liability".equals(category)) {
                        totalLiabilities += Math.abs(balance);
                    } else if ("Income".equals(category)) {
                        totalIncome += balance;
                    } else if ("Expense".equals(category)) {
                        totalExpenses += Math.abs(balance);
                    }
                }
            }
        }

        periodDetails.put("entityBalances", entityBalances);
        periodDetails.put("totalAssets", totalAssets);
        periodDetails.put("totalLiabilities", totalLiabilities);
        periodDetails.put("netWorth", totalAssets - totalLiabilities);
        periodDetails.put("totalIncome", totalIncome);
        periodDetails.put("totalExpenses", totalExpenses);
        periodDetails.put("netCashFlow", totalIncome - totalExpenses);

        // Period flows summary
        List<Map<String, Object>> periodFlows = new ArrayList<>();
        if (period.getEvents() != null) {
            for (Event event : period.getEvents()) {
                Map<String, Object> flowInfo = new HashMap<>();
                flowInfo.put("eventType", event.getType());
                flowInfo.put("eventId", event.getId());

                // Add flow amount if available
                if (event.getParams() != null && event.getParams().containsKey("amount")) {
                    flowInfo.put("amount", event.getParams().get("amount"));
                }

                periodFlows.add(flowInfo);
            }
        }
        periodDetails.put("periodFlows", periodFlows);

        // Investment performance summary for this period
        if (periodIndex > 0) {
            periodDetails.put("investmentSummary", generatePeriodInvestmentSummary(periodIndex));
        }

        return periodDetails;
    }

    private Map<String, Object> calculatePeriodROI(Entity entity, int periodIndex) {
        Map<String, Object> periodRoi = new HashMap<>();

        if (periodIndex < 1) {
            return periodRoi; // Need at least 2 periods for ROI calculation
        }

        TimePeriod currentPeriod = timeline.getPeriods().get(periodIndex);
        TimePeriod previousPeriod = timeline.getPeriods().get(periodIndex - 1);

        PeriodEntityAggregate currentAgg = currentPeriod.getPeriodEntityAggregate(entity);
        PeriodEntityAggregate previousAgg = previousPeriod.getPeriodEntityAggregate(entity);

        if (currentAgg != null && previousAgg != null) {
            double currentBalance = currentAgg.getNetBalance();
            double previousBalance = previousAgg.getNetBalance();

            if (previousBalance > 0) {
                double periodReturn = currentBalance - previousBalance;
                double periodROI = (periodReturn / previousBalance) * 100.0;

                periodRoi.put("periodReturn", periodReturn);
                periodRoi.put("periodROI", periodROI);
                periodRoi.put("previousBalance", previousBalance);
                periodRoi.put("currentBalance", currentBalance);
            }
        }

        return periodRoi;
    }

    private Map<String, Object> calculatePeriodSummary(TimePeriod period, int periodIndex) {
        Map<String, Object> summary = new HashMap<>();
        double totalAssets = 0.0;
        double totalLiabilities = 0.0;
        double totalIncome = 0.0;
        double totalExpenses = 0.0;
        int entityCount = 0;
        int eventCount = period.getEvents() != null ? period.getEvents().size() : 0;

        if (scenario != null && scenario.getInitialEntities() != null) {
            for (Entity entity : scenario.getInitialEntities()) {
                PeriodEntityAggregate agg = period.getPeriodEntityAggregate(entity);
                if (agg != null) {
                    entityCount++;
                    double balance = agg.getNetBalance();
                    String category = entity.getPrimaryCategory();

                    if ("Asset".equals(category)) {
                        totalAssets += balance;
                    } else if ("Liability".equals(category)) {
                        totalLiabilities += Math.abs(balance);
                    } else if ("Income".equals(category)) {
                        totalIncome += balance;
                    } else if ("Expense".equals(category)) {
                        totalExpenses += Math.abs(balance);
                    }
                }
            }
        }

        summary.put("entityCount", entityCount);
        summary.put("eventCount", eventCount);
        summary.put("totalAssets", totalAssets);
        summary.put("totalLiabilities", totalLiabilities);
        summary.put("netWorth", totalAssets - totalLiabilities);
        summary.put("totalIncome", totalIncome);
        summary.put("totalExpenses", totalExpenses);
        summary.put("netCashFlow", totalIncome - totalExpenses);

        // Calculate changes from previous period if applicable
        if (periodIndex > 0) {
            TimePeriod prevPeriod = timeline.getPeriods().get(periodIndex - 1);
            Map<String, Object> prevSummary = calculatePeriodSummary(prevPeriod, periodIndex - 1);

            summary.put("assetChange", totalAssets - (Double) prevSummary.get("totalAssets"));
            summary.put("liabilityChange", totalLiabilities - (Double) prevSummary.get("totalLiabilities"));
            summary.put("netWorthChange", (totalAssets - totalLiabilities) - (Double) prevSummary.get("netWorth"));
            summary.put("cashFlowChange", (totalIncome - totalExpenses) - (Double) prevSummary.get("netCashFlow"));
        }

        return summary;
    }

    private Map<String, Object> generatePeriodInvestmentSummary(int periodIndex) {
        Map<String, Object> summary = new HashMap<>();
        List<Map<String, Object>> investmentPerformance = new ArrayList<>();

        double totalInvestmentValue = 0.0;
        double totalInvestmentReturn = 0.0;

        if (scenario != null && scenario.getInitialEntities() != null) {
            for (Entity entity : scenario.getInitialEntities()) {
                if (isInvestmentEntity(entity)) {
                    TimePeriod period = timeline.getPeriods().get(periodIndex);
                    PeriodEntityAggregate agg = period.getPeriodEntityAggregate(entity);

                    if (agg != null) {
                        double balance = agg.getNetBalance();
                        totalInvestmentValue += balance;

                        Map<String, Object> perf = new HashMap<>();
                        perf.put("entityId", entity.getId());
                        perf.put("entityName", entity.getName());
                        perf.put("investmentType", entity.getDetailedCategory());
                        perf.put("currentValue", balance);

                        // Calculate period return if not first period
                        if (periodIndex > 0) {
                            Map<String, Object> periodRoi = calculatePeriodROI(entity, periodIndex);
                            if (!periodRoi.isEmpty()) {
                                perf.put("periodROI", periodRoi.get("periodROI"));
                                perf.put("periodReturn", periodRoi.get("periodReturn"));
                                totalInvestmentReturn += (Double) periodRoi.get("periodReturn");
                            }
                        }

                        investmentPerformance.add(perf);
                    }
                }
            }
        }

        summary.put("investmentPerformance", investmentPerformance);
        summary.put("totalInvestmentValue", totalInvestmentValue);
        summary.put("totalInvestmentReturn", totalInvestmentReturn);

        if (totalInvestmentValue > 0) {
            summary.put("portfolioReturnPercent", (totalInvestmentReturn / (totalInvestmentValue - totalInvestmentReturn)) * 100.0);
        }

        return summary;
    }

    /**
     * Get list of available periods with basic information
     */
    public List<Map<String, Object>> getAvailablePeriods() {
        List<Map<String, Object>> periods = new ArrayList<>();

        if (timeline == null || timeline.getPeriods() == null) {
            return periods;
        }

        for (int i = 0; i < timeline.getPeriods().size(); i++) {
            TimePeriod period = timeline.getPeriods().get(i);
            Map<String, Object> periodInfo = new HashMap<>();
            periodInfo.put("index", i);
            periodInfo.put("id", "period_" + i);
            periodInfo.put("startDate", period.getStart());
            periodInfo.put("endDate", period.getEnd());
            periodInfo.put("inflation", period.getInflation());
            periodInfo.put("riskFreeRate", period.getRiskFreeRate());

            // Add quick summary
            Map<String, Object> summary = calculatePeriodSummary(period, i);
            periodInfo.put("totalAssets", summary.get("totalAssets"));
            periodInfo.put("netWorth", summary.get("netWorth"));
            periodInfo.put("netCashFlow", summary.get("netCashFlow"));

            periods.add(periodInfo);
        }

        return periods;
    }

    /**
     * Compare two periods and return detailed comparison data
     */
    public Map<String, Object> comparePeriods(int periodIndex1, int periodIndex2) {
        Map<String, Object> comparison = new HashMap<>();

        if (timeline == null || timeline.getPeriods() == null ||
            periodIndex1 < 0 || periodIndex1 >= timeline.getPeriods().size() ||
            periodIndex2 < 0 || periodIndex2 >= timeline.getPeriods().size()) {
            comparison.put("error", "Invalid period indices");
            return comparison;
        }

        TimePeriod period1 = timeline.getPeriods().get(periodIndex1);
        TimePeriod period2 = timeline.getPeriods().get(periodIndex2);

        Map<String, Object> summary1 = calculatePeriodSummary(period1, periodIndex1);
        Map<String, Object> summary2 = calculatePeriodSummary(period2, periodIndex2);

        comparison.put("period1", Map.of(
            "index", periodIndex1,
            "summary", summary1
        ));
        comparison.put("period2", Map.of(
            "index", periodIndex2,
            "summary", summary2
        ));

        // Calculate differences
        Map<String, Object> differences = new HashMap<>();
        differences.put("assetDifference", (Double) summary2.get("totalAssets") - (Double) summary1.get("totalAssets"));
        differences.put("liabilityDifference", (Double) summary2.get("totalLiabilities") - (Double) summary1.get("totalLiabilities"));
        differences.put("netWorthDifference", (Double) summary2.get("netWorth") - (Double) summary1.get("netWorth"));
        differences.put("cashFlowDifference", (Double) summary2.get("netCashFlow") - (Double) summary1.get("netCashFlow"));

        comparison.put("differences", differences);

        // Investment performance comparison
        if (scenario != null && scenario.getInitialEntities() != null) {
            List<Map<String, Object>> investmentComparison = new ArrayList<>();

            for (Entity entity : scenario.getInitialEntities()) {
                if (isInvestmentEntity(entity)) {
                    Map<String, Object> entityComparison = new HashMap<>();
                    entityComparison.put("entityId", entity.getId());
                    entityComparison.put("entityName", entity.getName());

                    PeriodEntityAggregate agg1 = period1.getPeriodEntityAggregate(entity);
                    PeriodEntityAggregate agg2 = period2.getPeriodEntityAggregate(entity);

                    if (agg1 != null && agg2 != null) {
                        double balance1 = agg1.getNetBalance();
                        double balance2 = agg2.getNetBalance();

                        entityComparison.put("balancePeriod1", balance1);
                        entityComparison.put("balancePeriod2", balance2);
                        entityComparison.put("balanceDifference", balance2 - balance1);

                        if (balance1 > 0) {
                            entityComparison.put("growthPercent", ((balance2 - balance1) / balance1) * 100.0);
                        }
                    }

                    investmentComparison.add(entityComparison);
                }
            }

            comparison.put("investmentComparison", investmentComparison);
        }

        return comparison;
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