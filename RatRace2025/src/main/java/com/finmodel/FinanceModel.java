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
    private List<Person> people;

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
        if (people == null) {
            people = new ArrayList<>();
        }

        for (ScenarioComponent component : components) {
            // Handle Person components specially
            if (component instanceof Person) {
                Person person = (Person) component;
                people.add(person);
                // Person components are handled separately, continue to next component
                continue;
            }
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

            // Show sample financial reports for final period
            int finalPeriodIndex = timeline.getPeriods().size() - 1;
            System.out.println("\n=== SAMPLE FINANCIAL REPORTS (Final Period) ===");
            Map<String, Object> reports = getFormattedFinancialReports(finalPeriodIndex);

            System.out.println("Use getPeriodDetails(index) for complete period information");
            System.out.println("Use generateIncomeStatement(index) for Income Statement data");
            System.out.println("Use generateBalanceSheet(index) for Balance Sheet data");
            System.out.println("Use getFormattedFinancialReports(index) for formatted reports");
            System.out.println();

            // Show a brief version of the reports
            if (reports.containsKey("formattedIncomeStatement")) {
                String incomeReport = (String) reports.get("formattedIncomeStatement");
                // Show just the key lines
                String[] lines = incomeReport.split("\n");
                System.out.println("INCOME STATEMENT SUMMARY:");
                for (int i = 0; i < Math.min(lines.length, 8); i++) {
                    System.out.println("  " + lines[i]);
                }
                if (lines.length > 8) {
                    System.out.println("  ... (use formatted reports for full details)");
                }
            }
        }

        // Show person tax summary if people are defined
        if (people != null && !people.isEmpty()) {
            System.out.println("\n=== UK TAX SUMMARY ===");
            Map<String, Object> taxSummary = getPersonTaxSummary();

            if (!taxSummary.containsKey("error")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> personTaxes = (List<Map<String, Object>>) taxSummary.get("personTaxDetails");

                for (Map<String, Object> personTax : personTaxes) {
                    String personName = (String) personTax.get("personName");
                    double grossIncome = (Double) personTax.get("grossIncome");
                    double totalTax = (Double) personTax.get("totalTaxPaid");
                    double effectiveRate = (Double) personTax.get("effectiveTaxRate");

                    System.out.printf("%s: £%.0f gross, £%.0f tax, %.1f%% effective rate%n",
                        personName, grossIncome, totalTax, effectiveRate);
                }

                double totalGross = (Double) taxSummary.get("totalGrossIncome");
                double totalTax = (Double) taxSummary.get("totalTaxPaid");
                double avgEffectiveRate = (Double) taxSummary.get("averageEffectiveTaxRate");

                System.out.printf("Total: £%.0f gross income, £%.0f tax paid, %.1f%% average effective rate%n",
                    totalGross, totalTax, avgEffectiveRate);
            }
        }

        // Show scenario summary financial reports
        System.out.println("\n=== SCENARIO FINANCIAL SUMMARY ===");
        Map<String, Object> scenarioReports = getFormattedScenarioFinancialReports();

        System.out.println("Use getFormattedScenarioFinancialReports() for complete formatted reports");
        System.out.println("Use generateScenarioIncomeStatement() for scenario Income Statement data");
        System.out.println("Use generateScenarioBalanceSheet() for scenario Balance Sheet data");
        System.out.println();

        // Show brief scenario summary
        if (scenarioReports.containsKey("formattedScenarioIncomeStatement")) {
            String scenarioIncome = (String) scenarioReports.get("formattedScenarioIncomeStatement");
            // Show key lines from scenario summary
            String[] lines = scenarioIncome.split("\n");
            System.out.println("SCENARIO INCOME SUMMARY:");
            for (int i = 3; i < Math.min(lines.length, 12); i++) { // Skip header, show key data
                if (!lines[i].trim().isEmpty()) {
                    System.out.println("  " + lines[i]);
                }
            }
            if (lines.length > 12) {
                System.out.println("  ... (use formatted reports for full details)");
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

        // Add simplified financial reports
        Map<String, Object> financialReports = getFormattedFinancialReports(periodIndex);
        periodDetails.put("incomeStatement", financialReports.get("incomeStatement"));
        periodDetails.put("balanceSheet", financialReports.get("balanceSheet"));
        periodDetails.put("formattedIncomeStatement", financialReports.get("formattedIncomeStatement"));
        periodDetails.put("formattedBalanceSheet", financialReports.get("formattedBalanceSheet"));

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

    /**
     * Generate a simplified Income Statement for a specific period
     */
    public Map<String, Object> generateIncomeStatement(int periodIndex) {
        Map<String, Object> incomeStatement = new HashMap<>();

        if (timeline == null || timeline.getPeriods() == null ||
            periodIndex < 0 || periodIndex >= timeline.getPeriods().size()) {
            incomeStatement.put("error", "Invalid period index: " + periodIndex);
            return incomeStatement;
        }

        TimePeriod period = timeline.getPeriods().get(periodIndex);

        // Initialize income and expense categories
        Map<String, Object> revenues = new HashMap<>();
        Map<String, Object> expenses = new HashMap<>();

        double totalRevenue = 0.0;
        double totalExpenses = 0.0;

        if (scenario != null && scenario.getInitialEntities() != null) {
            for (Entity entity : scenario.getInitialEntities()) {
                PeriodEntityAggregate agg = period.getPeriodEntityAggregate(entity);
                if (agg != null) {
                    double balance = agg.getNetBalance();
                    String category = entity.getPrimaryCategory();
                    String detailedCategory = entity.getDetailedCategory();

                    if ("Income".equals(category)) {
                        // Income entities show inflows (positive amounts)
                        revenues.put(detailedCategory, balance);
                        totalRevenue += balance;
                    } else if ("Expense".equals(category)) {
                        // Expense entities show outflows (negative amounts, but display as positive)
                        expenses.put(detailedCategory, Math.abs(balance));
                        totalExpenses += Math.abs(balance);
                    }
                }
            }
        }

        // Calculate net income
        double netIncome = totalRevenue - totalExpenses;

        incomeStatement.put("periodIndex", periodIndex);
        incomeStatement.put("revenues", revenues);
        incomeStatement.put("totalRevenue", totalRevenue);
        incomeStatement.put("expenses", expenses);
        incomeStatement.put("totalExpenses", totalExpenses);
        incomeStatement.put("netIncome", netIncome);

        return incomeStatement;
    }

    /**
     * Generate a simplified Balance Sheet for a specific period
     */
    public Map<String, Object> generateBalanceSheet(int periodIndex) {
        Map<String, Object> balanceSheet = new HashMap<>();

        if (timeline == null || timeline.getPeriods() == null ||
            periodIndex < 0 || periodIndex >= timeline.getPeriods().size()) {
            balanceSheet.put("error", "Invalid period index: " + periodIndex);
            return balanceSheet;
        }

        TimePeriod period = timeline.getPeriods().get(periodIndex);

        // Initialize asset and liability categories
        Map<String, Object> assets = new HashMap<>();
        Map<String, Object> liabilities = new HashMap<>();

        double totalAssets = 0.0;
        double totalLiabilities = 0.0;

        if (scenario != null && scenario.getInitialEntities() != null) {
            for (Entity entity : scenario.getInitialEntities()) {
                PeriodEntityAggregate agg = period.getPeriodEntityAggregate(entity);
                if (agg != null) {
                    double balance = agg.getNetBalance();
                    String category = entity.getPrimaryCategory();
                    String detailedCategory = entity.getDetailedCategory();

                    if ("Asset".equals(category)) {
                        assets.put(detailedCategory + " - " + entity.getName(), balance);
                        totalAssets += balance;
                    } else if ("Liability".equals(category)) {
                        liabilities.put(detailedCategory + " - " + entity.getName(), Math.abs(balance));
                        totalLiabilities += Math.abs(balance);
                    }
                }
            }
        }

        // Calculate net worth
        double netWorth = totalAssets - totalLiabilities;

        balanceSheet.put("periodIndex", periodIndex);
        balanceSheet.put("assets", assets);
        balanceSheet.put("totalAssets", totalAssets);
        balanceSheet.put("liabilities", liabilities);
        balanceSheet.put("totalLiabilities", totalLiabilities);
        balanceSheet.put("netWorth", netWorth);

        return balanceSheet;
    }

    /**
     * Generate formatted financial reports for display
     */
    public Map<String, Object> getFormattedFinancialReports(int periodIndex) {
        Map<String, Object> reports = new HashMap<>();

        Map<String, Object> incomeStatement = generateIncomeStatement(periodIndex);
        Map<String, Object> balanceSheet = generateBalanceSheet(periodIndex);

        // Format Income Statement for display
        StringBuilder incomeReport = new StringBuilder();
        incomeReport.append("INCOME STATEMENT - Period ").append(periodIndex).append("\n");
        incomeReport.append("=".repeat(50)).append("\n");

        if (!incomeStatement.containsKey("error")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> revenues = (Map<String, Object>) incomeStatement.get("revenues");
            double totalRevenue = (Double) incomeStatement.get("totalRevenue");

            incomeReport.append("REVENUES:\n");
            revenues.forEach((category, amount) ->
                incomeReport.append(String.format("  %-25s $%10.2f\n", category, (Double) amount)));
            incomeReport.append(String.format("  %-25s $%10.2f\n", "TOTAL REVENUE", totalRevenue));
            incomeReport.append("\n");

            @SuppressWarnings("unchecked")
            Map<String, Object> expenses = (Map<String, Object>) incomeStatement.get("expenses");
            double totalExpenses = (Double) incomeStatement.get("totalExpenses");

            incomeReport.append("EXPENSES:\n");
            expenses.forEach((category, amount) ->
                incomeReport.append(String.format("  %-25s $%10.2f\n", category, (Double) amount)));
            incomeReport.append(String.format("  %-25s $%10.2f\n", "TOTAL EXPENSES", totalExpenses));
            incomeReport.append("\n");

            double netIncome = (Double) incomeStatement.get("netIncome");
            incomeReport.append(String.format("NET INCOME (LOSS): %25s $%10.2f\n", "", netIncome));
        }

        // Format Balance Sheet for display
        StringBuilder balanceReport = new StringBuilder();
        balanceReport.append("BALANCE SHEET - Period ").append(periodIndex).append("\n");
        balanceReport.append("=".repeat(50)).append("\n");

        if (!balanceSheet.containsKey("error")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> assets = (Map<String, Object>) balanceSheet.get("assets");
            double totalAssets = (Double) balanceSheet.get("totalAssets");

            balanceReport.append("ASSETS:\n");
            assets.forEach((category, amount) ->
                balanceReport.append(String.format("  %-35s $%10.2f\n", category, (Double) amount)));
            balanceReport.append(String.format("  %-35s $%10.2f\n", "TOTAL ASSETS", totalAssets));
            balanceReport.append("\n");

            @SuppressWarnings("unchecked")
            Map<String, Object> liabilities = (Map<String, Object>) balanceSheet.get("liabilities");
            double totalLiabilities = (Double) balanceSheet.get("totalLiabilities");

            balanceReport.append("LIABILITIES:\n");
            liabilities.forEach((category, amount) ->
                balanceReport.append(String.format("  %-35s $%10.2f\n", category, (Double) amount)));
            balanceReport.append(String.format("  %-35s $%10.2f\n", "TOTAL LIABILITIES", totalLiabilities));
            balanceReport.append("\n");

            double netWorth = (Double) balanceSheet.get("netWorth");
            balanceReport.append(String.format("NET WORTH: %40s $%10.2f\n", "", netWorth));
        }

        reports.put("incomeStatement", incomeStatement);
        reports.put("balanceSheet", balanceSheet);
        reports.put("formattedIncomeStatement", incomeReport.toString());
        reports.put("formattedBalanceSheet", balanceReport.toString());

        return reports;
    }

    /**
     * Generate a cumulative Income Statement for the entire scenario
     */
    public Map<String, Object> generateScenarioIncomeStatement() {
        Map<String, Object> scenarioIncomeStatement = new HashMap<>();

        if (timeline == null || timeline.getPeriods() == null || timeline.getPeriods().isEmpty()) {
            scenarioIncomeStatement.put("error", "No scenario data available");
            return scenarioIncomeStatement;
        }

        // Aggregate data across all periods
        Map<String, Double> totalRevenues = new HashMap<>();
        Map<String, Double> totalExpenses = new HashMap<>();
        java.util.concurrent.atomic.AtomicReference<Double> totalRevenue = new java.util.concurrent.atomic.AtomicReference<>(0.0);
        java.util.concurrent.atomic.AtomicReference<Double> totalExpensesAmount = new java.util.concurrent.atomic.AtomicReference<>(0.0);

        for (TimePeriod period : timeline.getPeriods()) {
            Map<String, Object> periodIncomeStatement = generateIncomeStatement(timeline.getPeriods().indexOf(period));

            if (!periodIncomeStatement.containsKey("error")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> revenues = (Map<String, Object>) periodIncomeStatement.get("revenues");
                @SuppressWarnings("unchecked")
                Map<String, Object> expenses = (Map<String, Object>) periodIncomeStatement.get("expenses");

                // Aggregate revenues
                revenues.forEach((category, amount) -> {
                    totalRevenues.put(category, totalRevenues.getOrDefault(category, 0.0) + (Double) amount);
                    totalRevenue.updateAndGet(v -> v + (Double) amount);
                });

                // Aggregate expenses
                expenses.forEach((category, amount) -> {
                    totalExpenses.put(category, totalExpenses.getOrDefault(category, 0.0) + (Double) amount);
                    totalExpensesAmount.updateAndGet(v -> v + (Double) amount);
                });
            }
        }

        // Calculate net income
        double netIncome = totalRevenue.get() - totalExpensesAmount.get();

        scenarioIncomeStatement.put("scenarioSummary", true);
        scenarioIncomeStatement.put("totalPeriods", timeline.getPeriods().size());
        scenarioIncomeStatement.put("revenues", totalRevenues);
        scenarioIncomeStatement.put("totalRevenue", totalRevenue.get());
        scenarioIncomeStatement.put("expenses", totalExpenses);
        scenarioIncomeStatement.put("totalExpenses", totalExpensesAmount.get());
        scenarioIncomeStatement.put("netIncome", totalRevenue.get() - totalExpensesAmount.get());

        // Calculate average monthly figures
        int totalMonths = timeline.getPeriods().size();
        if (totalMonths > 0) {
            scenarioIncomeStatement.put("averageMonthlyRevenue", totalRevenue.get() / totalMonths);
            scenarioIncomeStatement.put("averageMonthlyExpenses", totalExpensesAmount.get() / totalMonths);
            scenarioIncomeStatement.put("averageMonthlyNetIncome", (totalRevenue.get() - totalExpensesAmount.get()) / totalMonths);
        }

        return scenarioIncomeStatement;
    }

    /**
     * Generate a final Balance Sheet for the entire scenario (end state)
     */
    public Map<String, Object> generateScenarioBalanceSheet() {
        Map<String, Object> scenarioBalanceSheet = new HashMap<>();

        if (timeline == null || timeline.getPeriods() == null || timeline.getPeriods().isEmpty()) {
            scenarioBalanceSheet.put("error", "No scenario data available");
            return scenarioBalanceSheet;
        }

        // Use the final period's balance sheet
        int finalPeriodIndex = timeline.getPeriods().size() - 1;
        Map<String, Object> finalBalanceSheet = generateBalanceSheet(finalPeriodIndex);

        if (finalBalanceSheet.containsKey("error")) {
            return finalBalanceSheet; // Return error if final period has issues
        }

        // Add scenario summary information
        scenarioBalanceSheet.putAll(finalBalanceSheet);
        scenarioBalanceSheet.put("scenarioSummary", true);
        scenarioBalanceSheet.put("totalPeriods", timeline.getPeriods().size());
        scenarioBalanceSheet.put("finalPeriod", finalPeriodIndex);

        // Calculate initial vs final comparison
        if (timeline.getPeriods().size() > 1) {
            Map<String, Object> initialBalanceSheet = generateBalanceSheet(0);

            if (!initialBalanceSheet.containsKey("error")) {
                double initialNetWorth = (Double) initialBalanceSheet.get("netWorth");
                double finalNetWorth = (Double) finalBalanceSheet.get("netWorth");

                scenarioBalanceSheet.put("initialNetWorth", initialNetWorth);
                scenarioBalanceSheet.put("finalNetWorth", finalNetWorth);
                scenarioBalanceSheet.put("netWorthChange", finalNetWorth - initialNetWorth);
                scenarioBalanceSheet.put("netWorthChangePercent",
                    initialNetWorth != 0 ? ((finalNetWorth - initialNetWorth) / initialNetWorth) * 100.0 : 0.0);

                // Calculate annualized return on net worth
                int yearsElapsed = timeline.getPeriods().size() / 12; // Assuming monthly periods
                if (yearsElapsed > 0 && initialNetWorth > 0) {
                    double annualizedReturn = Math.pow((finalNetWorth / initialNetWorth), 1.0 / yearsElapsed) - 1;
                    scenarioBalanceSheet.put("annualizedNetWorthReturn", annualizedReturn * 100.0);
                }
            }
        }

        return scenarioBalanceSheet;
    }

    /**
     * Generate formatted scenario summary financial reports
     */
    public Map<String, Object> getFormattedScenarioFinancialReports() {
        Map<String, Object> reports = new HashMap<>();

        Map<String, Object> incomeStatement = generateScenarioIncomeStatement();
        Map<String, Object> balanceSheet = generateScenarioBalanceSheet();

        // Format Scenario Income Statement for display
        StringBuilder incomeReport = new StringBuilder();
        incomeReport.append("SCENARIO INCOME STATEMENT SUMMARY\n");
        incomeReport.append("=================================\n");
        incomeReport.append("Total Periods: ").append(timeline != null ? timeline.getPeriods().size() : 0).append("\n\n");

        if (!incomeStatement.containsKey("error")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> revenues = (Map<String, Object>) incomeStatement.get("revenues");
            double totalRevenue = (Double) incomeStatement.get("totalRevenue");

            incomeReport.append("TOTAL REVENUES:\n");
            revenues.forEach((category, amount) ->
                incomeReport.append(String.format("  %-25s $%10.2f\n", category, (Double) amount)));
            incomeReport.append(String.format("  %-25s $%10.2f\n", "TOTAL REVENUE", totalRevenue));

            if (incomeStatement.containsKey("averageMonthlyRevenue")) {
                double avgMonthly = (Double) incomeStatement.get("averageMonthlyRevenue");
                incomeReport.append(String.format("  %-25s $%10.2f\n", "AVERAGE MONTHLY", avgMonthly));
            }
            incomeReport.append("\n");

            @SuppressWarnings("unchecked")
            Map<String, Object> expenses = (Map<String, Object>) incomeStatement.get("expenses");
            double totalExpenses = (Double) incomeStatement.get("totalExpenses");

            incomeReport.append("TOTAL EXPENSES:\n");
            expenses.forEach((category, amount) ->
                incomeReport.append(String.format("  %-25s $%10.2f\n", category, (Double) amount)));
            incomeReport.append(String.format("  %-25s $%10.2f\n", "TOTAL EXPENSES", totalExpenses));

            if (incomeStatement.containsKey("averageMonthlyExpenses")) {
                double avgMonthly = (Double) incomeStatement.get("averageMonthlyExpenses");
                incomeReport.append(String.format("  %-25s $%10.2f\n", "AVERAGE MONTHLY", avgMonthly));
            }
            incomeReport.append("\n");

            double netIncome = (Double) incomeStatement.get("netIncome");
            incomeReport.append(String.format("NET INCOME (LOSS): %25s $%10.2f\n", "", netIncome));

            if (incomeStatement.containsKey("averageMonthlyNetIncome")) {
                double avgMonthly = (Double) incomeStatement.get("averageMonthlyNetIncome");
                incomeReport.append(String.format("AVERAGE MONTHLY NET INCOME: %15s $%10.2f\n", "", avgMonthly));
            }
        }

        // Format Scenario Balance Sheet for display
        StringBuilder balanceReport = new StringBuilder();
        balanceReport.append("SCENARIO BALANCE SHEET SUMMARY\n");
        balanceReport.append("==============================\n");
        balanceReport.append("Final Period: ").append(timeline != null ? timeline.getPeriods().size() - 1 : 0).append("\n");
        balanceReport.append("Total Periods: ").append(timeline != null ? timeline.getPeriods().size() : 0).append("\n\n");

        if (!balanceSheet.containsKey("error")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> assets = (Map<String, Object>) balanceSheet.get("assets");
            double totalAssets = (Double) balanceSheet.get("totalAssets");

            balanceReport.append("FINAL ASSETS:\n");
            assets.forEach((category, amount) ->
                balanceReport.append(String.format("  %-35s $%10.2f\n", category, (Double) amount)));
            balanceReport.append(String.format("  %-35s $%10.2f\n", "TOTAL ASSETS", totalAssets));
            balanceReport.append("\n");

            @SuppressWarnings("unchecked")
            Map<String, Object> liabilities = (Map<String, Object>) balanceSheet.get("liabilities");
            double totalLiabilities = (Double) balanceSheet.get("totalLiabilities");

            balanceReport.append("FINAL LIABILITIES:\n");
            liabilities.forEach((category, amount) ->
                balanceReport.append(String.format("  %-35s $%10.2f\n", category, (Double) amount)));
            balanceReport.append(String.format("  %-35s $%10.2f\n", "TOTAL LIABILITIES", totalLiabilities));
            balanceReport.append("\n");

            double netWorth = (Double) balanceSheet.get("netWorth");
            balanceReport.append(String.format("FINAL NET WORTH: %35s $%10.2f\n", "", netWorth));

            // Add scenario performance if available
            if (balanceSheet.containsKey("netWorthChange")) {
                double change = (Double) balanceSheet.get("netWorthChange");
                double changePercent = (Double) balanceSheet.get("netWorthChangePercent");
                balanceReport.append(String.format("NET WORTH CHANGE: %32s $%10.2f (%5.1f%%)\n", "", change, changePercent));

                if (balanceSheet.containsKey("annualizedNetWorthReturn")) {
                    double annualizedReturn = (Double) balanceSheet.get("annualizedNetWorthReturn");
                    balanceReport.append(String.format("ANNUALIZED NET WORTH RETURN: %18s %5.1f%%\n", "", annualizedReturn));
                }
            }
        }

        reports.put("scenarioIncomeStatement", incomeStatement);
        reports.put("scenarioBalanceSheet", balanceSheet);
        reports.put("formattedScenarioIncomeStatement", incomeReport.toString());
        reports.put("formattedScenarioBalanceSheet", balanceReport.toString());

        return reports;
    }

    /**
     * Get comprehensive scenario summary including financial reports
     */
    public Map<String, Object> getScenarioSummary() {
        Map<String, Object> summary = new HashMap<>();

        if (timeline == null || timeline.getPeriods() == null || timeline.getPeriods().isEmpty()) {
            summary.put("error", "No scenario data available");
            return summary;
        }

        // Basic scenario information
        summary.put("totalPeriods", timeline.getPeriods().size());
        summary.put("scenarioDuration", timeline.getPeriods().size() + " periods"); // Could be enhanced to show actual dates

        // Investment performance summary
        if (timeline.getPeriods().size() > 1) {
            summary.put("investmentSummary", generatePeriodInvestmentSummary(timeline.getPeriods().size() - 1));
        }

        // Financial reports
        Map<String, Object> financialReports = getFormattedScenarioFinancialReports();
        summary.putAll(financialReports);

        // Overall scenario metrics
        Map<String, Object> scenarioMetrics = new HashMap<>();

        // Calculate total returns across the scenario
        if (scenario != null && scenario.getInitialEntities() != null) {
            double initialTotalValue = 0.0;
            double finalTotalValue = 0.0;

            for (Entity entity : scenario.getInitialEntities()) {
                if ("Asset".equals(entity.getPrimaryCategory())) {
                    // For initial value, use the entity initial value
                    initialTotalValue += entity.getInitialValue();

                    // For final value, get from final period
                    PeriodEntityAggregate finalAgg = timeline.getPeriods().get(timeline.getPeriods().size() - 1)
                        .getPeriodEntityAggregate(entity);
                    if (finalAgg != null) {
                        finalTotalValue += finalAgg.getNetBalance();
                    }
                }
            }

            if (initialTotalValue > 0) {
                double totalReturn = finalTotalValue - initialTotalValue;
                double totalReturnPercent = (totalReturn / initialTotalValue) * 100.0;

                scenarioMetrics.put("initialTotalValue", initialTotalValue);
                scenarioMetrics.put("finalTotalValue", finalTotalValue);
                scenarioMetrics.put("totalReturn", totalReturn);
                scenarioMetrics.put("totalReturnPercent", totalReturnPercent);

                // Annualized return
                int yearsElapsed = timeline.getPeriods().size() / 12;
                if (yearsElapsed > 0) {
                    double annualizedReturn = Math.pow((finalTotalValue / initialTotalValue), 1.0 / yearsElapsed) - 1;
                    scenarioMetrics.put("annualizedReturn", annualizedReturn * 100.0);
                }
            }
        }

        summary.put("scenarioMetrics", scenarioMetrics);

        return summary;
    }

    /**
     * Get comprehensive person tax summary
     */
    public Map<String, Object> getPersonTaxSummary() {
        Map<String, Object> taxSummary = new HashMap<>();

        if (people == null || people.isEmpty()) {
            taxSummary.put("error", "No people defined in scenario");
            return taxSummary;
        }

        List<Map<String, Object>> personTaxDetails = new ArrayList<>();
        double totalGrossIncome = 0.0;
        double totalTaxPaid = 0.0;
        double totalNetIncome = 0.0;

        for (Person person : people) {
            Map<String, Object> personTax = new HashMap<>();
            personTax.put("personId", person.getId());
            personTax.put("personName", person.getName());
            personTax.put("grossIncome", person.getGrossIncome());
            personTax.put("taxableIncome", person.getTaxableIncome());
            personTax.put("incomeTax", person.getIncomeTax());
            personTax.put("nationalInsurance", person.getNationalInsurance());
            personTax.put("capitalGainsTax", person.getCapitalGainsTax());
            personTax.put("totalTaxPaid", person.getTotalTaxPaid());
            personTax.put("effectiveTaxRate", person.getEffectiveTaxRate());
            personTax.put("netIncome", person.getGrossIncome() - person.getTotalTaxPaid());

            personTaxDetails.add(personTax);

            totalGrossIncome += person.getGrossIncome();
            totalTaxPaid += person.getTotalTaxPaid();
            totalNetIncome += (person.getGrossIncome() - person.getTotalTaxPaid());
        }

        taxSummary.put("personTaxDetails", personTaxDetails);
        taxSummary.put("totalGrossIncome", totalGrossIncome);
        taxSummary.put("totalTaxPaid", totalTaxPaid);
        taxSummary.put("totalNetIncome", totalNetIncome);
        taxSummary.put("averageEffectiveTaxRate", totalGrossIncome > 0 ? (totalTaxPaid / totalGrossIncome) * 100.0 : 0.0);

        // Tax efficiency analysis
        Map<String, Object> taxEfficiency = calculateTaxEfficiencyAnalysis();
        taxSummary.put("taxEfficiency", taxEfficiency);

        return taxSummary;
    }

    /**
     * Calculate tax efficiency analysis across all people
     */
    private Map<String, Object> calculateTaxEfficiencyAnalysis() {
        Map<String, Object> efficiency = new HashMap<>();

        if (people == null || people.isEmpty()) {
            return efficiency;
        }

        double totalGrossIncome = 0.0;
        double totalTaxPaid = 0.0;
        double totalAllowancesUsed = 0.0;
        double totalAllowancesAvailable = 0.0;

        for (Person person : people) {
            totalGrossIncome += person.getGrossIncome();
            totalTaxPaid += person.getTotalTaxPaid();
            totalAllowancesUsed += Math.min(person.getGrossIncome(), person.getPersonalAllowance());
            totalAllowancesAvailable += person.getPersonalAllowance();
        }

        efficiency.put("overallEffectiveTaxRate", totalGrossIncome > 0 ? (totalTaxPaid / totalGrossIncome) * 100.0 : 0.0);
        efficiency.put("allowanceUtilizationRate", totalAllowancesAvailable > 0 ? (totalAllowancesUsed / totalAllowancesAvailable) * 100.0 : 0.0);
        efficiency.put("totalAllowancesUsed", totalAllowancesUsed);
        efficiency.put("totalAllowancesAvailable", totalAllowancesAvailable);
        efficiency.put("unusedAllowances", totalAllowancesAvailable - totalAllowancesUsed);

        // Identify tax optimization opportunities
        List<String> optimizationSuggestions = new ArrayList<>();
        if (totalAllowancesUsed < totalAllowancesAvailable * 0.8) {
            optimizationSuggestions.add("Consider increasing pension contributions to utilize unused personal allowances");
        }
        if (totalTaxPaid / totalGrossIncome > 0.3) {
            optimizationSuggestions.add("High effective tax rate - consider tax-efficient investment strategies");
        }

        efficiency.put("optimizationSuggestions", optimizationSuggestions);

        return efficiency;
    }

    /**
     * Get UK tax year information
     */
    public Map<String, Object> getUKTaxYearInfo(int taxYear) {
        Map<String, Object> taxYearInfo = new HashMap<>();
        taxYearInfo.put("taxYear", taxYear);
        taxYearInfo.put("taxYearString", taxYear + "/" + (taxYear + 1));
        taxYearInfo.put("startDate", "06 April " + taxYear);
        taxYearInfo.put("endDate", "05 April " + (taxYear + 1));
        taxYearInfo.put("personalAllowance", UKTaxCalculator.TaxRates.PERSONAL_ALLOWANCE);
        taxYearInfo.put("basicRateThreshold", UKTaxCalculator.TaxRates.BASIC_RATE_THRESHOLD);
        taxYearInfo.put("higherRateThreshold", UKTaxCalculator.TaxRates.HIGHER_RATE_THRESHOLD);

        return taxYearInfo;
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