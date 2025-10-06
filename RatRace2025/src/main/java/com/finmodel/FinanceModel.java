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

        if (scenario != null && scenario.getInitialEntities() != null && timeline != null && timeline.getPeriods() != null) {
            for (TimePeriod period : timeline.getPeriods()) {
                for (Entity entity : scenario.getInitialEntities()) {
                    PeriodEntityAggregate agg = period.getPeriodEntityAggregate(entity);
                    if (agg != null && agg.getNetBalance() > 0) {
                        Map<String, Object> node = new HashMap<>();
                        node.put("id", entity.getId() + "_" + period.hashCode()); // Unique id
                        node.put("name", entity.getName());
                        node.put("balance", agg.getNetBalance());
                        node.put("rate", agg.finalVersion().getRate());
                        nodes.add(node);
                    }
                }
                // Stub links: connect entities within period
                if (scenario.getInitialEntities().size() > 1) {
                    for (int i = 0; i < scenario.getInitialEntities().size() - 1; i++) {
                        Map<String, Object> link = new HashMap<>();
                        link.put("source", scenario.getInitialEntities().get(i).getId() + "_" + period.hashCode());
                        link.put("target", scenario.getInitialEntities().get(i + 1).getId() + "_" + period.hashCode());
                        link.put("value", 100.0); // Stub value
                        links.add(link);
                    }
                }
            }
        }

        data.put("nodes", nodes);
        data.put("links", links);
        return data;
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