package com.finmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    private Map<Entity, List<Event>> eventTemplates;
    private Map<String, Entity> entityTemplates;
    private List<Event> latentEvents;
    private int numPeriods;
    private List<Object> externals; // Stub for ExternalSource[]
    private List<AssetGroup> assetGroups;

    public void initialize(Timeline timeline) {
        // Stub: create numPeriods TimePeriods and add to timeline
        for (int i = 0; i < numPeriods; i++) {
            TimePeriod period = TimePeriod.builder()
                    .start(new Date()) // Stub dates
                    .end(new Date())
                    .riskFreeRate(3.5)
                    .inflation(2.0)
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