package com.finmodel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Scenario {
    private List<Entity> initialEntities;
    private Map<Entity, List<Event>> eventTemplates;
    private Map<String, Entity> entityTemplates;
    private List<Event> latentEvents;
    private int numPeriods;
    private List<Object> externals; // Stub for ExternalSource[]

    public void initialize(Timeline timeline) {
        // Stub: create numPeriods TimePeriods and add to timeline
        for (int i = 0; i < numPeriods; i++) {
            TimePeriod period = TimePeriod.builder()
                    .start(new Date()) // Stub dates
                    .end(new Date())
                    .riskFreeRate(3.5)
                    .inflation(2.0)
                    .versionChains(Map.of())
                    .events(new ArrayList<>())
                    .build();
            timeline.addPeriod(period);
        }
    }

    public void registerLatentEvent(Event event) {
        if (latentEvents == null) {
            latentEvents = new ArrayList<>();
        }
        latentEvents.add(event);
    }

    public Entity getTemplate(String key) {
        return entityTemplates != null ? entityTemplates.get(key) : null;
    }
}