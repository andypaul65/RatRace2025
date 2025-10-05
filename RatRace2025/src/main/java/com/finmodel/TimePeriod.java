package com.finmodel;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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
    private Cache<Entity, PeriodEntityAggregate> aggregateCache;

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
        // Check cache first if exists
        if (aggregateCache != null) {
            try {
                return aggregateCache.getIfPresent(entity);
            } catch (Exception e) {
                // Ignore
            }
        }

        EntityVersion finalVersion = getFinalVersion(entity);
        if (finalVersion == null) {
            return null;
        }
        List<Flow> netIntraFlows = getAggregatedFlows(entity);
        List<Flow> interFlows = List.of(); // Stub
        PeriodEntityAggregate agg = new PeriodEntityAggregate(finalVersion, netIntraFlows, interFlows);

        // Cache it
        if (aggregateCache == null) {
            aggregateCache = CacheBuilder.newBuilder()
                    .maximumSize(100)
                    .build();
        }
        aggregateCache.put(entity, agg);

        return agg;
    }
}