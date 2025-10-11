package com.finmodel;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.IOException;
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

    public void runSimulation() {
        if (scenario == null || timeline == null) {
            return;
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
        simulator.playOut();
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

    public void dumpToConsole() {
        System.out.println("Finance Model Dump");
        if (scenario == null || timeline == null || timeline.getPeriods() == null) {
            System.out.println("No data to dump");
            return;
        }

        System.out.printf("Timeline: %d periods%n", timeline.getPeriods().size());
        System.out.printf("Dynamic entities: %d%n", dynamicEntities != null ? dynamicEntities.size() : 0);

        for (int i = 0; i < timeline.getPeriods().size(); i++) {
            TimePeriod period = timeline.getPeriods().get(i);
            System.out.printf("Period %d: %s | RiskFree: %.2f%% | Inflation: %.2f%%%n",
                    i + 1,
                    period.getStart() != null ? period.getStart().toString() : "N/A",
                    period.getRiskFreeRate(),
                    period.getInflation());

            if (scenario.getInitialEntities() != null) {
                for (Entity entity : scenario.getInitialEntities()) {
                    PeriodEntityAggregate agg = period.getPeriodEntityAggregate(entity);
                    if (agg != null) {
                        System.out.printf("  Entity %s: Balance %.2f, Rate %.2f%%%n",
                                entity.getId(), agg.getNetBalance(), agg.finalVersion().getRate());
                    }
                }
            }
        }

        // Simple Sankey ASCII stub
        System.out.println("Sankey ASCII View:");
        System.out.println("[Periods] --> [Entities] --> [Flows]");
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