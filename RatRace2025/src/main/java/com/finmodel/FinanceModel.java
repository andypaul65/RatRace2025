package com.finmodel;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
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
        // Stub: build Sankey data from periods
        Map<String, Object> data = new HashMap<>();
        data.put("nodes", new java.util.ArrayList<>());
        data.put("links", new java.util.ArrayList<>());
        // In full impl, collect from PeriodEntityAggregates
        return data;
    }

    public void dumpToConsole() {
        // Stub: dump model state to console
        System.out.println("Finance Model Dump");
        System.out.println("Scenario: " + (scenario != null ? scenario.getNumPeriods() : "none"));
        System.out.println("Timeline periods: " + (timeline != null && timeline.getPeriods() != null ? timeline.getPeriods().size() : 0));
        System.out.println("Dynamic entities: " + (dynamicEntities != null ? dynamicEntities.size() : 0));
    }

    public void addDynamicEntity(Entity entity) {
        if (dynamicEntities != null) {
            dynamicEntities.add(entity);
        }
    }
}