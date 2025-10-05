package com.finmodel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimePeriod {
    private java.util.Date start;
    private java.util.Date end;
    private double riskFreeRate;
    private double inflation;
    private Map<Entity, List<EntityVersion>> versionChains;
    private List<Event> events;

    public void addEvent(Event event) {
        if (events == null) {
            events = new ArrayList<>();
        }
        events.add(event);
    }

    public EntityVersion getFinalVersion(Entity entity) {
        if (versionChains == null || !versionChains.containsKey(entity)) {
            return null;
        }
        List<EntityVersion> versions = versionChains.get(entity);
        return versions.isEmpty() ? null : versions.get(versions.size() - 1);
    }

    public List<Flow> getAggregatedFlows(Entity entity) {
        // Stub: return empty list
        // In full implementation, collect flows from events
        return List.of();
    }

    public PeriodEntityAggregate getPeriodEntityAggregate(Entity entity) {
        EntityVersion finalVersion = getFinalVersion(entity);
        if (finalVersion == null) {
            return null;
        }
        List<Flow> netIntraFlows = getAggregatedFlows(entity);
        List<Flow> interFlows = List.of(); // Stub
        return new PeriodEntityAggregate(finalVersion, netIntraFlows, interFlows);
    }
}